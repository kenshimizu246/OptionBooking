#!/bin/bash

KS="keys"

if [ -f keys ] ; then
	TS=`date +%Y%m%d%H%M%S`
	FL="$KS.$TS"
	echo "file exist! $FL"
	mv $KS $FL
fi

keytool -genkey -keystore $KS -alias tamageta < keystore.txt 


