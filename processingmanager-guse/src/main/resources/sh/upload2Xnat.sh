#!/bin/bash

# Upload to xnat using curl
# curl --user <USERNAME>:<PASSWORD> --request PUT "<URI> --form "<FILENAME>=@<FILENAME>"
#curl --user nsgateway:nsgateway --request PUT "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00269/reconstructions/Recon004/out/resources/584/files" --form "error8.log=@error8.log"
curl --user $1:$2 --request PUT "$4" --form "$3=@$3"


