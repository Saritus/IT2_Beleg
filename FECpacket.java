class FECpacket {
	int FEC_group; // Anzahl an Medienpaketen für eine Gruppe

	// Sender
	void setdata(byte[] data, int data_length) { // nimmt Nutzerdaten entgegen

	}

	int getdata(byte[] data) {
		return 0; // holt FEC-Paket (L�nge -> l�ngstes Medienpaket)

	}

	// Empfänger
	// getrennte Puffer f�r Mediendaten und FEC
	// Puffergröße sollte Vielfaches der Gruppengröße sein
	void rcvdata(int nr, byte[] data) {
		// UDP-Payload , Nr. des Bildes bzw. RTP-SN

	}

	void rcvfec(int nr, byte[] data) { // FEC-Daten

	}

	byte[] getjpeg(int nr) {
		return null; // übergibt korrigiertes Paket oder Fehler (null)

	}
}