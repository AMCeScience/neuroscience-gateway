#!/bin/bash

export X509_USER_PROXY=/home/guse/robot/robot_proxy

echo "trying to remove the file $2 in case it already exists"
lcg-del -v --vo vlemed -a $2


echo "Upload to grid (LFC + SRM)"
# lcg-cr -d <SE ELEMENT> -l <TARGET LFN> <LOCAL FILE NAME>
#lcg-cr  -l lfn://grid/vlemed/mark/test/ids ids
lcg-cr -d srm.grid.sara.nl -v -l $2 $1

