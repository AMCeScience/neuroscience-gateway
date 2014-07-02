#!/bin/bash

# Download from grid (LFC+SRM)
# lcg-cp <REMOTE URI> <LOCAL FILE NAME>
#lcg-cp lfn://grid/vlemed/mark/test/ids ids2
lcg-cp -v --connect-timeout 1000 --sendreceive-timeout 10000 --bdii-timeout 1000 --srm-timeout 1000 --vo vlemed $1 $2
