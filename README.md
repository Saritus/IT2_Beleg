# IT2 Beleg

## Information

`Author: Sebastian Mischke`

[Aufgabenstellung][aufgabe]

## Project

Bei dem Projekt handelt es sich um eine Client-Server-Anwendung, die mittels des
[Real-Time-Streaming-Protokolls (RTSP)][rtsp] eines Videostream überträgt. Dabei
werden die eigentlichen Videodaten mittels des [Real-Time-Protokolls (RTP)][rtp]
übertragen. Um den Einfluss von verlorengegangenen Paketen zu verringern, wird
als Ausfallschutz eine [Forward-Error-Correction (FEC)][fec] eingesetzt.

![classdiagram]

## Server

Die Aufgabe des Servers ist es, aus den Frames einer `.mjpeg`-Datei sowohl
RTP-Pakete als auch FEC-Pakete zu erzeugen und diese mittels eines
Datagram-Sockets an den Client zu schicken.

## Client

Der Client ist die grafische Bedienoberfläche des Nutzers. Er kann eine
Verbindung mit dem Server aufbauen, diesem Befehle wie z.B. `Play` oder `Pause`
schicken und empfangene JPEG-Bilder anzeigen. Zusätzlich zeigt er dem Nutzer
Statistiken über die bisherigen Übertragungen an.

![interface_client]

## RTPpacket

```java
header[0] = (byte) ((Version << 6) | (Padding << 5) | (Extension << 4) | CC); // |VVPX|CCCC|
header[1] = (byte) ((Marker << 7) | (PayloadType & 0x7F)); // |M<--|PT->|
header[2] = (byte) (SequenceNumber >> 8); // SeqNum: Highbyte-Teil
header[3] = (byte) (SequenceNumber & 0x00FF); // Lowbyte-Teil
header[4] = (byte) (TimeStamp >> 24); // [xxxxxxxx|--------|--------|--------]
header[5] = (byte) ((TimeStamp >> 16) & 0x000000FF); // [--------|xxxxxxxx|--------|--------]
header[6] = (byte) ((TimeStamp >> 8) & 0x000000FF); // [--------|--------|xxxxxxxx|--------]
header[7] = (byte) (TimeStamp & 0x000000FF); // [--------|--------|--------|xxxxxxxx]
header[8] = (byte) (Ssrc >> 24); // Ssrc same as Timestamp
header[9] = (byte) ((Ssrc >> 16) & 0x000000FF);
header[10] = (byte) ((Ssrc >> 8) & 0x000000FF);
header[11] = (byte) (Ssrc & 0x000000FF);
```

## FECpacket

Das FECpacket beinhaltet die Funktionen für die Forward-Error-Correction (FEC).
Zum einen erlaubt es dem Server ein RTP-Paket zu erstellen, welches die
Informationen eines FEC-Paketes besitzt, zum anderen bietet es dem Client die
Möglichkeit, ein verloren gegangenes RTP-Paket wiederherzustellen.

#### Serverseite

Jedes mal, wenn der Server einen neuen Frame zum versenden vorbereitet, werden
diese Bytes auch an das FECpacket gegeben. Das FECpacket verlängert zuerst,
falls notwendig, die Länge des Byte-Puffers und verknüpft dann die bekommen
Bytes des nächsten Frames und die bereits im FECpacket vorhandenen Bytes mittels
`XOR`.

Wenn das FECpacket soviele Frames bekommen hat, wie vorher in der FEC_group
festgelegt worden, dann wird ein neues UDP-Paket erstellt, welches die Daten des
FECpacket enthält. Dabei wird die FEC_group als erstes Byte vor das Daten-Array
gestellt. Anschließend wird das FECpacket resetet.

#### Clientseite

Wenn im Client ein RTP-Paket ankommt, wird zunächst geprüft, ob seine
Sequenznummer größer ist, als die Sequenznummer des letzten empfangenen Pakets.
Ist dies der Fall, wird es im FECpacket in einer ArrayList gespeichert.
Zusätzlich wird dessen Sequenznummer in einer anderen ArrayList hinzugefügt und
seine `Payload`-Daten werden mit den bereits im FECpacket vorhandenen Daten
mittels `XOR` verknüpft.

```java
void rcvdata(RTPpacket rtppacket) {
  if (rtppacket.getsequencenumber() > lastSqNr) {
    rtp_nrs.add(rtppacket.getsequencenumber());
    displayPackages.add(rtppacket);
    packages++;
    xordata(rtppacket);
    lastSqNr = rtppacket.getsequencenumber();
  }
}
```

Erhält der Client ein FEC-Paket, so wird zunächst das erste Byte des
Daten-Arrays als Größe der FEC_group genommen. Die restlichen Bytes werden mit
den bereits im FECpacket vorhandenen Daten mittels `XOR` verknüpt. Anschließend
wird geprüft, ob alle RTP-Pakete, die im "Zuständigkeitsbereich" des empfangenen
FEC-Pakets liegen, vorhanden sind. Sollte genau ein RTP-Paket fehlen, so wird
dieses aus den Daten, die sich momentan im FECpacket befinden, rekonstruiert und
an die richtige Stelle in der ArrayList eingesetzt. Zum Schluss wird das
FECpacket auf seine Startwerte zurückgesetzt.

```java
void rcvfec(RTPpacket rtp) {
  // get FEC_group from first data element
  FEC_group = rtp.payload[0];

  // data is payload without first element
  byte[] newdata = Arrays.copyOfRange(rtp.payload, 1, rtp.getpayload_length());
  data_size = rtp.getpayload_length() - 1;
  xordata(newdata, data_size);
  
  to_frame = rtp.getsequencenumber();

  // check if last packages are complete
  checkDisplaylist();

  // reset data
  reset();
}
```

[aufgabe]: Praktikum-Streaming.pdf

[classdiagram]: /doc/img/classdiagram.png

[interface_client]: /doc/img/ui_client.png

[rtsp]: http://www.ietf.org/rfc/rfc2326.txt

[rtp]: http://www.ietf.org/rfc/rfc3550.txt

[fec]: http://www.ietf.org/rfc/rfc5109.txt
