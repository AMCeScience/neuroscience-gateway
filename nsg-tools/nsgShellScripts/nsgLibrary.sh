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
