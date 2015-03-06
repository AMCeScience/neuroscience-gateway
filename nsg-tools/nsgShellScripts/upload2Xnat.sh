#!/bin/bash

# UPDATE: v2 accepts input $4 with key-value map to allow passing of parameters
# EXAMPLE: base_string http://mri-neutrino.amc.nl:8080/xnatZ0/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00011 xnat_id test base_data_type DTI_low_b400 subject 014 scan_id 301 application_name DTIPreprocessing_V1_0 reconstruction_type ZIP

# check if all four arguments are provided
if [ "$#" != "4" ]; then
	echo "USAGE: ./upload2Xnat.sh username password filename keys-values"
	exit 1
fi

# store command line arguments
file=$1 # NOTE: this is the local file to upload to XNAT
input=$2 # NOTE: this is the dictionary input (with destination and parameters)
username=$3
password=$4

# DEBUG: get real password from command line standard input
#if [ "$password" == "-p" ]; then
#	read -es -p "Enter XNAT password for user ID $username: " password
#	echo # new line
#fi

# DEBUG: print command line arguments
echo "DEBUG: input parameters are:"
echo "DEBUG:   file=${file}"
echo "DEBUG:   input=${input}"
echo "DEBUG:   username=${username}"
echo "DEBUG:   password=*******" # ${password}"


# TODO: validate command line arguments?
# E.g., no empty username or password

# read input string into array; split on " " (i.e. single space)
IFS=" " read -a split_input <<< $input

# check number of elements (shoud be even!)
num_elements=${#split_input[@]}
if [ "$((num_elements%2))" != 0 ]; then
	echo "ERROR: key-value map has an odd number of elements!\n\tCheck if keys and values are properly aligned (interleaved),\n\tthat they are separated by a single space only,\n\tand that the keys and values do not contain a space" 1>&2
	exit 1
fi

# convert list into associated array (i.e. equivalent to map in Bash v4+)
declare -A key_value_map # NOTE: capital A creates associative arrays, small A only numerical index arrays
for ((i=0; i < num_elements; i=i+2)); do
	key=${split_input[((i+0))]}
	val=${split_input[((i+1))]}
	key_value_map[$key]=$val # RSLV: does not allow value of key to be "key" :S results in infinite recursion :)
done

# DEBUG: print key-value pairs
echo "DEBUG: key-value-map contains ${#key_value_map[@]} entries:"
for k in "${!key_value_map[@]}"; do
	echo "DEBUG:   $k -> ${key_value_map[$k]}"
done

# generate unique (i.e. random) reconstruction ID
N_TRIES=10;
while [ "$N_TRIES" -gt 0 ]; do
	# generate random reconstruction ID
	recon_id="${key_value_map[application_name]}_NSG$((0+RANDOM%100000))"
	echo "DEBUG: checking if random reconstruction ID $recon_id is unique..."
	
	# check if reconstruction placeholder already exists
	placeholder_uri="${key_value_map[base_string]}/reconstructions/$recon_id"
	echo "INFO: placeholder URI is $placeholder_uri"
	cmd="curl --silent --write-out %{http_code} --output /dev/null --user $username:$password --request GET $placeholder_uri"
	#echo "EXECUTE: $cmd" # NOTE: don't echo; contains user name and password!
	http_error_code=`$cmd`
	
	# check if reconstruction placeholder not exists (404 NOT FOUND) or exists (200 OK)
	if [ "$http_error_code" == "200" ]; then
		# exists, try again...
		echo "DEBUG: reconstruction placeholder already exists"
		N_TRIES=$((N_TRIES-1));
		echo "DEBUG: trying again with another random number ($N_TRIES tries left)";
	elif [ "$http_error_code" == "404" ]; then
		# found a unique random reconstruction ID
		echo "DEBUG: unique random reconstruction ID found"
		echo "INFO: unique random reconstruction ID is $recon_id"
		break;
	else
		# another HTTP error occured
		echo "ERROR: could not check if reconstruction ID is unique! XNAT returned HTTP $http_error_code" 1>&2
		exit 1
	fi
done

# check if unqiue reconstruction ID found
if [ "$N_TRIES" == "0" ]; then
	echo "ERROR: could not find a unique reconstruction ID! Run the script again, increase number of tries, or increase range of random number" 1>&2
	exit 1
fi

# create empty XNAT reconstruction placeholder
placeholder_uri="$placeholder_uri?type=${key_value_map[reconstruction_type]}&baseScanType=${key_value_map[base_data_type]}"
echo "INFO: placeholder creation URI is $placeholder_uri"

cmd="curl --silent --write-out %{http_code} --output /dev/null --user $username:$password --request PUT $placeholder_uri"
#echo "EXECUTE: $cmd" # NOTE: don't echo; contains user name and password!
http_error_code=`$cmd`

# check if reconstruction placeholder was succesfully created
if [ "$http_error_code" != "200" ]; then
	echo "ERROR: could not create reconstruction placeholder! XNAT returned HTTP $http_error_code" 1>&2
	exit 1
fi

# generate remote filename
filename="${key_value_map[xnat_subject_label]}.Recon.${key_value_map[xnat_scan_id]}.${key_value_map[reconstruction_type]}"
echo "INFO: remote filename is $filename"

# upload file to reconstruction placeholder
upload_uri="${key_value_map[base_string]}/reconstructions/$recon_id/resources/${key_value_map[application_name]}/files/$filename?format=${key_value_map[reconstruction_type]}" # &extract=true
echo "INFO: upload URI is $upload_uri"

cmd="curl --silent --write-out %{http_code} --output /dev/null --user $username:$password --request PUT $upload_uri --form file=@$file" # TODO: can't get quotes right around input filename! May only be a problem if the flename contains spaces or other special characters...

#echo "EXECUTE: $cmd" # NOTE: don't echo; contains user name and password!
http_error_code=`$cmd`

# check if file was succesfully uploaded
if [ "$http_error_code" != "200" ]; then
	echo "ERROR: could not upload file! XNAT returned HTTP $http_error_code" 1>&2
	
	# try to delete empty reconstruction
	cmd="curl --silent --write-out %{http_code} --output /dev/null --user $username:$password --request DELETE $placeholder_uri"
	#echo "EXECUTE: $cmd" # NOTE: don't echo; contains user name and password!
	http_error_code=`$cmd`
	
	# check if reconstruction placeholder was succesfully created
	if [ "$http_error_code" != "200" ]; then
		echo "WARNING: could not remove (presumably) empty reconstruction placeholder"
		echo "WARNING: XNAT returned HTTP $http_error_code"
	fi
	
	exit 1
fi

# clean up
unset username
unset password

# print 
echo "URI:${key_value_map[base_string]}/reconstructions/$recon_id/resources/${key_value_map[application_name]}/files/$filename"

# upload successful
exit 0

