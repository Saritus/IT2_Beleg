# IT2_Beleg

## Project

![classdiagram]

## Server

## Client

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

`Author: Sebastian Mischke`
Last modification: {{ file.mtime }}

[classdiagram]: /doc/img/classdiagram.png
[interface_client]: /doc/img/ui_client.png
