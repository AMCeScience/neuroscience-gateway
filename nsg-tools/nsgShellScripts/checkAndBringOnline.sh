#!/bin/bash

export X509_USER_PROXY=/home/guse/robot/robot_proxy
export LFC_HOST=lfc.grid.sara.nl


lcg-ls $1
if [ $? != 0 ]; then
   exit 2
fi

#get the first replica and brign it online
srm-bring-online `lcg-lr --vo vlemed $1 | head -1`
