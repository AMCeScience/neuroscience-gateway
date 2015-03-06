#!/bin/bash

# Download from xnat using curl
# curl --remote-name --user <USERNAME>:<PASSWORD> --request GET "<URI>"
#curl --remote-name --user nsgateway:nsgateway --request GET "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00269/reconstructions/Recon004/out/resources/584/files/error3.log"
curl -f --user $3:$4 -o "$2" --request GET "$1"



