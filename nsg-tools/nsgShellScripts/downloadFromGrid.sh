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

export X509_USER_PROXY=/home/guse/robot/robot_proxy

# Download from grid (LFC+SRM)
# lcg-cp <REMOTE URI> <LOCAL FILE NAME>
#lcg-cp lfn://grid/vlemed/mark/test/ids ids2
lcg-cp -v --connect-timeout 1000 --sendreceive-timeout 10000 --bdii-timeout 1000 --srm-timeout 1000 --vo vlemed $1 $2
