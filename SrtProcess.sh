#!/bin/bash

echo processing $1

RUNCMD="java -cp /home/diegoap/SrtProcess/jars/*:/home/diegoap/SrtProcess SrtProcess '$1'"
echo running $RUNCMD..
eval $RUNCMD

