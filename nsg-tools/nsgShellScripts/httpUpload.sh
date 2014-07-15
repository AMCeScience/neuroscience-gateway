# Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or 
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.


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

