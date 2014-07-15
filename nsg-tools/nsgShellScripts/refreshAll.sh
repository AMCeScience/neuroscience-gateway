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

# usage: ./refreshAll.sh <portal context>
#
# default portal context for Liferay is "liferay-portal-6.1.0", but in some
# or our instances has been changed by "portal"

CONTEXT=$1

NSGPM_HOME="/home/guse/nsgShellScripts"

java -classpath $NSGPM_HOME/generic_nsgpm_client.jar nl.amc.biolab.nsg.ProcessingManagerClient http://localhost:8080/$CONTEXT/ProcessingManagerService?wsdl

