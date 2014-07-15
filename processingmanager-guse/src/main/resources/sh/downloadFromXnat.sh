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

# Download from xnat using curl
# curl --remote-name --user <USERNAME>:<PASSWORD> --request GET "<URI>"
#curl --remote-name --user nsgateway:nsgateway --request GET "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00269/reconstructions/Recon004/out/resources/584/files/error3.log"
curl --user $1:$2 -o "$4" --request GET "$3"

