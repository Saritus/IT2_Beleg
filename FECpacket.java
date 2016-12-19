
public class FECpacket {

	int FEC_group // Anzahl an Medienpaketen für eine Gruppe

	// size of the FEC header:
	static int HEADER_SIZE = 12;

	// Fields that compose the FEC header
	public int Version;
	public int Padding;
	public int Extension;
	public int CC;
	public int Marker;
	public int PayloadType;
	public int SequenceNumber;
	public int TimeStamp;
	public int Ssrc;

	// Bitstream of the FEC header
	public byte[] header;

	// size of the FEC payload
	public int payload_size;
	// Bitstream of the FEC payload
	public byte[] payload;

	// --------------------------
	// Constructor of an FECpacket object from header fields and payload
	// bitstream
	// --------------------------
	public FECpacket(int PType, int Framenb, int Time, byte[] data, int data_length) {
		// fill by default header fields:
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;

		// fill changing header fields:
		SequenceNumber = Framenb;
		TimeStamp = Time;
		PayloadType = PType;

		// build the header bistream:
		// --------------------------
		header = new byte[HEADER_SIZE];

		// fill the header array of byte with FEC header fields
		header[0] = (byte) ((Version << 6) | (Padding << 5) | (Extension << 4) | CC); // |VVPX|CCCC| --> [1000|0000]
		header[1] = (byte) (PayloadType & 0x7F); // |M<--|PT->| --> [0001|1010]
		header[2] = (byte) (SequenceNumber >> 8); // SeqNum: Highbyte-Teil
		header[3] = (byte) (SequenceNumber & 0x00FF); // Lowbyte-Teil
		header[4] = (byte) (TimeStamp >> 24); // TimeSt. Highbyte first
												// [xxxxxxxx|--------|--------|--------]
		header[5] = (byte) ((TimeStamp >> 16) & 0x000000FF); // [--------|xxxxxxxx|--------|--------]
		header[6] = (byte) ((TimeStamp >> 8) & 0x000000FF); // [--------|--------|xxxxxxxx|--------]
		header[7] = (byte) (TimeStamp & 0x000000FF); // [--------|--------|--------|xxxxxxxx]
		header[8] = (byte) (Ssrc >> 24); // Ssrc same as Timestamp
		header[9] = (byte) ((Ssrc >> 16) & 0x000000FF);
		header[10] = (byte) ((Ssrc >> 8) & 0x000000FF);
		header[11] = (byte) (Ssrc & 0x000000FF);

		// fill the payload bitstream:
		// --------------------------
		payload_size = data_length;
		payload = new byte[data_length];

		// fill payload array of byte from data (given in parameter of the
		// constructor)
		for (int i = 0; i < data_length; i++) {
			payload[i] = data[i];
		}

		// ! Do not forget to uncomment method printheader() below !

	}

	// --------------------------
	// Constructor of an FECpacket object from the packet bistream
	// --------------------------
	public FECpacket(byte[] packet, int packet_size) {
		// fill default fields:
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;

		// check if total packet size is lower than the header size
		if (packet_size >= HEADER_SIZE) {
			// get the header bitsream:
			header = new byte[HEADER_SIZE];
			for (int i = 0; i < HEADER_SIZE; i++)
				header[i] = packet[i];

			// get the payload bitstream:
			payload_size = packet_size - HEADER_SIZE;
			payload = new byte[payload_size];
			for (int i = HEADER_SIZE; i < packet_size; i++)
				payload[i - HEADER_SIZE] = packet[i];

			// interpret the changing fields of the header:
			PayloadType = header[1] & 127;
			SequenceNumber = unsigned_int(header[3]) + 256 * unsigned_int(header[2]);
			TimeStamp = unsigned_int(header[7]) + 256 * unsigned_int(header[6]) + 65536 * unsigned_int(header[5])
					+ 16777216 * unsigned_int(header[4]);
		}
	}

	// --------------------------
	// getpayload: return the payload bistream of the FECpacket and its size
	// --------------------------
	public int getpayload(byte[] data) {

		for (int i = 0; i < payload_size; i++)
			data[i] = payload[i];

		return (payload_size);
	}

	// --------------------------
	// getpayload_length: return the length of the payload
	// --------------------------
	public int getpayload_length() {
		return (payload_size);
	}

	// --------------------------
	// getlength: return the total length of the FEC packet
	// --------------------------
	public int getlength() {
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// getpacket: returns the packet bitstream and its length
	// --------------------------
	public int getpacket(byte[] packet) {
		// construct the packet = header + payload
		for (int i = 0; i < HEADER_SIZE; i++)
			packet[i] = header[i];
		for (int i = 0; i < payload_size; i++)
			packet[i + HEADER_SIZE] = payload[i];

		// return total size of the packet
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// gettimestamp
	// --------------------------

	public int gettimestamp() {
		return (TimeStamp);
	}

	// --------------------------
	// getsequencenumber
	// --------------------------
	public int getsequencenumber() {
		return (SequenceNumber);
	}

	// --------------------------
	// getpayloadtype
	// --------------------------
	public int getpayloadtype() {
		return (PayloadType);
	}

	// --------------------------
	// print headers without the SSRC
	// --------------------------
	public void printheader() {
		for (int i = 0; i < (HEADER_SIZE - 4); i++) {
			for (int j = 7; j >= 0; j--)
				if (((1 << j) & header[i]) != 0)
					System.out.print("1");
				else
					System.out.print("0");
			System.out.print(" ");
		}
		System.out.println();
	}

	// return the unsigned value of 8-bit integer nb
	static int unsigned_int(int nb) {
		if (nb >= 0)
			return (nb);
		else
			return (256 + nb);
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

}
