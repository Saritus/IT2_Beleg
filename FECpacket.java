
public class FECpacket {
	int FEC_group // Anzahl an Medienpaketen für eine Gruppe
	RTPpacket fecpacket;

	static int FEC_TYPE = 127;

	FECpacket(k) {
		FEC_group = k;
		fecpacket = new new RTPpacket(FEC_TYPE, imagenb, imagenb * FRAME_PERIOD, buf, image_length);
	}

	// Sender
	void setdata(byte[] data, int data_length) { // nimmt Nutzerdaten entgegen

	}

	int getdata(byte[] data) {
		return 0; // holt FEC-Paket (Länge -> längstes Medienpaket)

	}

	// Empfänger
	// getrennte Puffer für Mediendaten und FEC
	// Puffergröße sollte Vielfaches der Gruppengröße sein
	void rcvdata(int nr, byte[] data) {
		// UDP-Payload , Nr. des Bildes bzw. RTP-SN

	}

	void rcvfec(int nr, byte[] data) { // FEC-Daten

	}

	byte[] getjpeg(int nr) {
		return null; // Übergibt korrigiertes Paket oder Fehler (null)

	}

	// --------------------------
	// getpayload: return the payload bistream of the RTPpacket and its size
	// --------------------------
	public int getpayload(byte[] data) {
		return (fecpacket.getpayload(data));
	}

	// --------------------------
	// getpayload_length: return the length of the payload
	// --------------------------
	public int getpayload_length() {
		return (fecpacket.getpayload_length());
	}

	// --------------------------
	// getlength: return the total length of the RTP packet
	// --------------------------
	public int getlength() {
		return (fecpacket.getlength());
	}

	// --------------------------
	// getpacket: returns the packet bitstream and its length
	// --------------------------
	public int getpacket(byte[] packet) {
		return (fecpacket.getpacket(packet));
	}

	// --------------------------
	// gettimestamp
	// --------------------------

	public int gettimestamp() {
		return (fecpacket.gettimestamp());
	}

	// --------------------------
	// getsequencenumber
	// --------------------------
	public int getsequencenumber() {
		return (fecpacket.getsequencenumber());
	}

	// --------------------------
	// getpayloadtype
	// --------------------------
	public int getpayloadtype() {
		return (fecpacket.getpayloadtype());
	}

	// --------------------------
	// print headers without the SSRC
	// --------------------------
	public void printheader() {
		fecpacket.printheader();
	}
}
