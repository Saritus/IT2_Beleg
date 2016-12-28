import java.util.*;

public class FECpacket {
	int FEC_group; // Anzahl an Medienpaketen für eine Gruppe
	byte[] data;
	int data_size;
	int packages;
	int to_frame;
	List<Integer> rtp_nrs = new ArrayList<Integer>();
	List<RTPpacket> rtp_list = new ArrayList<RTPpacket>();

	static int FEC_TYPE = 127;
	static int FRAME_PERIOD = 40;

	FECpacket(int k) {
		FEC_group = k;
		packages = 0;

		data_size = 0;
		data = new byte[0];
		to_frame = 0;
	}

	FECpacket() {
		this(0);
	}

	// Sender

	void setGroupSize() {
		// TODO: push k at the beginning of data (already done?)
		// data = data >> 8 | k << data_size;
	}

	void setdata(byte[] data, int data_length) {
		this.data_size = data_length;
		for (int i = 0; i < data_size; i++) {
			this.data[i] = data[i];
		}
	}

	void xordata(byte[] data, int data_length) { // nimmt Nutzerdaten entgegen
		if (data_length > this.data_size) {

			// Create new data-array
			byte[] newdata = new byte[data_length];

			// Fill the new data-array with the old data
			for (int i = 0; i < this.data_size; i++) {
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

		return new RTPpacket(FEC_TYPE, imagenb, imagenb * FRAME_PERIOD, fecdata, data_size);
	}

	// Empfänger
	// getrennte Puffer für Mediendaten und FEC
	// Puffergröße sollte Vielfaches der Gruppengröße sein
	void rcvdata(RTPpacket rtppacket) {
		rtp_nrs.add(rtppacket.getsequencenumber());
		rtp_list.add(rtppacket);
		packages++;
		xordata(rtppacket);
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
		System.err.println("Kein fehlendes Packet gefunden");
		return -1;
	}

	byte[] get_missing_data() {
		return this.data;
	}

	void rcvfec(RTPpacket rtp) {
		// System.out.println(rtp.getsequencenumber());

		// get FEC_group from first data element
		FEC_group = rtp.payload[0];

		data_size = rtp.getpayload_length();

		// data is payload without first element
		// TODO: check payload length (it may loses one byte, which is bad)
		data = java.util.Arrays.copyOfRange(rtp.payload, 1, rtp.payload_size);

		to_frame = rtp.getsequencenumber();
	}

	byte[] getjpeg(int nr) {
		return null; // Übergibt korrigiertes Paket oder Fehler (null)

	}

	List<RTPpacket> get_rtp_packets() {

		List<RTPpacket> packetlist = new ArrayList<RTPpacket>();

		// Check for missing FEC-packets
		while ((rtp_list.size() > 0) && (rtp_list.get(0).getsequencenumber() <= this.to_frame - this.FEC_group)) {
			packetlist.add(rtp_list.get(0));
			rtp_list.remove(0);
		}

		if (rtp_list.size() == this.FEC_group) {
			// Got all packages
			System.out.println("Got all packages");
			while (rtp_list.size() > 0) {
				packetlist.add(rtp_list.get(0));
				rtp_list.remove(0);
			}

		} else if (rtp_list.size() < this.FEC_group - 1) {
			// Lost more than one package (not reversable)
			System.out.println("Lost too many packages");
			while (rtp_list.size() > 0) {
				packetlist.add(rtp_list.get(0));
				rtp_list.remove(0);
			}

		} else {
			// Lost exaclty one package (reversable)
			System.out.println("Lost exactly one packages");

			// get missing packages in RTPpackages
			int missingnr = get_missing_nr();
			byte[] missingdata = get_missing_data();

			// restore missing package
			RTPpacket missingpacket = new RTPpacket(26, missingnr, 0, missingdata, missingdata.length);

			// add the first packages to packetlist
			for (int i = this.to_frame - this.FEC_group + 1; i < missingnr; i++) {
				packetlist.add(rtp_list.get(0));
				rtp_list.remove(0);
			}

			// add missing package to packetlist
			// TODO: missingpacket seems to be not correct
			// packetlist.add(missingpacket);

			// add remaining packages to packetlist
			while (rtp_list.size() > 0) {
				packetlist.add(rtp_list.get(0));
				rtp_list.remove(0);
			}

		}

		return packetlist;
	}
}
