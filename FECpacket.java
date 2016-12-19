
public class FECpacket {
	int FEC_group; // Anzahl an Medienpaketen für eine Gruppe
	byte[] data;
	int data_size;
	int packages;
	int to_frame;

	static int FEC_TYPE = 127;
	static int FRAME_PERIOD = 40;

	FECpacket(int k) {
		FEC_group = k;
		packages = 0;

		data_size = 0;
		data = new byte[0];
		to_frame = 0;
	}

	FECpacket(RTPpacket rtp) {
		FEC_group = rtp.payload[0];
		packages = 0;

		data_size = rtp.getpayload_length();
		data = java.util.Arrays.copyOfRange(rtp.payload, 1, rtp.payload_size);
		to_frame = rtp.getsequencenumber();
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
	void rcvdata(int nr, byte[] data, int data_length) {
		// UDP-Payload , Nr. des Bildes bzw. FEC-SN

		// Save data-array to array buffer

		// If array buffer is full, check if every package is filled

		// Else fill the empty data array slots

	}

	void rcvdata(RTPpacket rtppacket) {
		rcvdata(rtppacket.SequenceNumber, rtppacket.payload, rtppacket.payload_size);
	}

	void rcvfec(int nr, byte[] data, int data_length) { // FEC-Daten
		// imagenb = nr;
		this.data_size = data_length;

	}

	byte[] getjpeg(int nr) {
		return null; // Übergibt korrigiertes Paket oder Fehler (null)

	}
}
