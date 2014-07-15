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


# usage of the Bash library:
# in the Bash script, add a line 
# 		. /path/libraryfile
#
# invoking Bash functions:
#	check_folder("/path/folder")
# 	check_folder($VARPATH)


# temporary download folder
# global variable for NSG scripts
TEMP_NSG="$HOME/.NSG_transfer"



# function check_folder receives a folder path,
# checks if the folder exists. If not, it creates it
function check_folder() {
	if [ ! -d "$1" ]; then
		mkdir $1
	fi
}
