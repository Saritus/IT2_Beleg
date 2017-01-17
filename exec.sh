#!/bin/bash
cd /bin
java Server 1028 10&
java Client localhost 1028 movie.mjpeg
