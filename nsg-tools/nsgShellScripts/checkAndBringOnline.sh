#!/bin/bash

export X509_USER_PROXY=/home/guse/robot/robot_proxy

lcg-ls $1
if [ $? != 0 ]; then
   exit 404
fi

#get the first replica and brign it online
srm-bring-online `lcg-lr $1 | head -1`
