#!/bin/bash

#  Upload to grid (LFC + SRM)
# lcg-cr -d <SE ELEMENT> -l <TARGET LFN> <LOCAL FILE NAME>
#lcg-cr  -l lfn://grid/vlemed/mark/test/ids ids
lcg-cr -d tbn18.nikhef.nl -v -l $2 $1

