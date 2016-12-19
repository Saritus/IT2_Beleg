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
		// TODO:
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

		fecdata[0] = (byte) FEC_group;
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
	}

	int get_missing() {
		if (rtp_nrs.size() == FEC_group) {
			return -1;
		} else if (rtp_nrs.size() == FEC_group - 1) {
			// return missing
			return 0;

		} else {
			return -1;
		}
	}

	void rcvfec(RTPpacket rtp) {
		System.out.println(rtp.getsequencenumber());
		// TODO: rtp.payload.length is 0
		// FEC_group = rtp.payload[0];

		data_size = rtp.getpayload_length();
		// data = java.util.Arrays.copyOfRange(rtp.payload, 1,
		// rtp.payload_size);
		to_frame = rtp.getsequencenumber();
	}

	byte[] getjpeg(int nr) {
		return null; // Übergibt korrigiertes Paket oder Fehler (null)

	}

	List<RTPpacket> get_rtp_packets() {
		/*
		 * // Check for missing FEC-packets while ((RTPpackages.size() > 0) &&
		 * (RTPpackages.get(0).getsequencenumber() <= fec_packet.to_frame -
		 * fec_packet.FEC_group)) { displayPackages.add(RTPpackages.get(0));
		 * RTPpackages.remove(0); }
		 * 
		 * // get missing packages in RTPpackages for (int i = 0; i <
		 * RTPpackages.size(); i++) { // fec_packet }
		 * 
		 * // if missing==1, calculate missing package
		 * 
		 * // add rtppackages from RTPpackages to displayPackages
		 * 
		 * // reset RTPpackages
		 * 
		 */

		return rtp_list;
	}
}
