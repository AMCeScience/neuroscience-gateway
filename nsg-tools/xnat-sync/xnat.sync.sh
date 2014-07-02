#/bin/sh

# $1: User LiferayID as in the neuroscience catalogue
# $2: Action: UpdateCatalogue | UpdateScanDate | ComputeMatchingApplications | All

N_ARGS=1
ACTION="All"
JARPATH="/home/guse/nsgShellScripts/xnat-sync/syncOffLine-1.7.jar"

RESOURCEID="1"

if [ $# -ne $N_ARGS ]
then
	echo ""
	echo -e "\tUsage: `basename $0` {user_list.file}"
	echo -e "\t\tuser_list.file: contains an user per line, avoid blank lines!"
	echo ""
	exit 1
fi

FUSERLIST=$1

# checking user list file exists
if [ ! -f $FUSERLIST ]
then
	echo "File $FUSERLIST does not exist."
	exit 1 
fi


while read USERID
do
	/usr/bin/java -cp $JARPATH  nl.amc.biolab.Tools.SynchOffLine $USERID $RESOURCEID $ACTION

done < $FUSERLIST

