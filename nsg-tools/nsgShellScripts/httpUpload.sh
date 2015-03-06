#!/bin/bash
src=$1
dst=$2
user=$3
pass=$4

# Upload using curl
# curl --user <USERNAME>:<PASSWORD> --request PUT "<URI> --form "<FILENAME>=@<FILENAME>"
# curl --user nsgateway:nsgateway --request PUT "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00269/reconstructions/Recon004/out/resources/584/files" --form "error8.log=@error8.log"
if [ x$user = x ]; then
  curl -f  --request PUT "$dst" --form "$src=@$src"
else 
  curl -f --user $user:$pass --request PUT "$dst" --form "$src=@$src"
fi

