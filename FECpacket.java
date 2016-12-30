import java.util.*;

public class FECpacket {
	int FEC_group; // Anzahl an Medienpaketen für eine Gruppe
	byte[] data;
	int data_size;
	int packages;
	int to_frame;
	List<Integer> rtp_nrs;
	List<RTPpacket> displayPackages = new ArrayList<RTPpacket>();

	static int FEC_TYPE = 127;
	static int FRAME_PERIOD = 40;

	FECpacket(int k) {
		reset();
		this.FEC_group = k;
	}

	FECpacket() {
		this(0);
	}

	void reset() {
		FEC_group = 0;
		packages = 0;
		data_size = 0;
		data = new byte[0];
		to_frame = 0;
		rtp_nrs = new ArrayList<>();
	}

	// Sender
	void setdata(byte[] data, int data_length) {
		this.data_size = data_length;
		for (int i = 0; i < data_size; i++) {
			this.data[i] = data[i];
		}
	}

	void xordata(byte[] data, int data_length) { // nimmt Nutzerdaten entgegen
		if (data_length > this.data.length) {

			// Create new data-array
			byte[] newdata = new byte[data_length];

			// Fill the new data-array with the old data
			for (int i = 0; i < this.data.length; i++) {
				newdata[i] = this.data[i];
			}

			// Set newdata as this.data
			this.data = newdata;
			this.data_size = data_length;
		}

		// XOR param-data-array with new data-array
		for (int i = 0; i < data_length; i++) {
			this.data[i] = (byte) (this.data[i] ^ data[i]);
		}
		packages++;
	}

	void xordata(RTPpacket rtppacket) {
		xordata(rtppacket.payload, rtppacket.payload_size);
	}

	int getdata(byte[] data) {
		for (int i = 0; i < data_size; i++) {
			data[i] = this.data[i];
		}

		return data_size; // holt FEC-Paket (Länge -> längstes Medienpaket)
	}

	RTPpacket createRTPpacket(int imagenb) {
		byte[] fecdata = new byte[data_size + 1];

		fecdata[0] = (byte) packages;
		System.arraycopy(data, 0, fecdata, 1, data_size);
		// sourcearray, sourceindex, targetarray, targetindex, length

		return new RTPpacket(FEC_TYPE, imagenb, imagenb * FRAME_PERIOD, fecdata, data_size + 1);
	}

	// Empfänger
	// getrennte Puffer für Mediendaten und FEC
	// Puffergröße sollte Vielfaches der Gruppengröße sein
	void rcvdata(RTPpacket rtppacket) {
		if ((displayPackages.isEmpty()) || (rtppacket.getsequencenumber() > displayPackages
				.get(displayPackages.size() - 1).getsequencenumber())) {
			rtp_nrs.add(rtppacket.getsequencenumber());
			displayPackages.add(rtppacket);
			packages++;
			xordata(rtppacket);
		}
	}

	int get_missing_nr() {
		int next = this.to_frame - this.FEC_group;
		for (int i = 0; i < rtp_nrs.size(); i++) {
			if (rtp_nrs.get(i) == next + 1) {
				next = rtp_nrs.get(i);
			} else {
				return next + 1;
			}
		}
		// letztes Paket fehlt

		// System.err.println("Kein fehlendes Packet gefunden");
		return this.to_frame;
	}

	byte[] get_missing_data() {
		return this.data;
	}

	void rcvfec(RTPpacket rtp) {
		// System.out.println(rtp.getsequencenumber());

		// get FEC_group from first data element
		FEC_group = rtp.payload[0];

		data_size = rtp.getpayload_length() - 1;

		// data is payload without first element
		byte[] newdata = Arrays.copyOfRange(rtp.payload, 1, rtp.getpayload_length());
		xordata(newdata, data_size);

		to_frame = rtp.getsequencenumber();

		// check if last packages are complete
		checkDisplaylist();

		// reset data
		reset();
	}

	private boolean checkDisplaylist() {
		if (rtp_nrs.size() == this.FEC_group) {
			// Got all packages
			return true;

		} else if (rtp_nrs.size() < this.FEC_group - 1) {
			// Lost more than one package (not reversable)
			return false;

		} else {
			// Lost exaclty one package (reversable)

			// get missing packages in RTPpackages
			int missingnr = get_missing_nr();
			byte[] missingdata = get_missing_data();

			// restore missing package
			RTPpacket missingpacket = new RTPpacket(26, missingnr, 0, missingdata, missingdata.length);

			// create empty temp list
			List<RTPpacket> tmp = new ArrayList<>();

			// remove bigger packages than missingpackage
			while ((displayPackages.size() > 0)
					&& (displayPackages.get(displayPackages.size() - 1).getsequencenumber() > missingnr)) {
				tmp.add(0, displayPackages.get(displayPackages.size() - 1));
				displayPackages.remove(displayPackages.size() - 1);
			}

			// add missingpacket at right position
			displayPackages.add(missingpacket);

			// add elements in tmp to displaypackages
			while (tmp.size() > 0) {
				displayPackages.add(tmp.get(0));
				tmp.remove(0);
			}

			return true;
		}
	}

	byte[] getjpeg(int nr) {
		if (displayPackages.size() > 0) {
			RTPpacket rtp_packet = displayPackages.get(0);

			// get the payload bitstream from the RTPpacket object
			int payload_length = rtp_packet.getpayload_length();
			byte[] payload = new byte[payload_length];
			rtp_packet.getpayload(payload);

			// remove the displayed package
			displayPackages.remove(0);

			return payload; // Return next image as bytearray
		} else {
			return null; // No image to show
			// TODO: this creates a bug, i think
		}
	}
}
