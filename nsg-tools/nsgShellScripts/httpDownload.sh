#!/bin/bash
src=$1
dst=$2
user=$3
pass=$4

# Download from xnat using curl
# curl --remote-name --user <USERNAME>:<PASSWORD> --request GET "<URI>"
# curl --remote-name --user nsgateway:nsgateway --request GET "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00269/reconstructions/Recon004/out/resources/584/files/error3.log"

if [ x$user = x ]; then
  curl -f -o "$dst" --request GET "$src"
else 
  curl -f --user $user:$pass -o "$dst" --request GET "$src"
fi


