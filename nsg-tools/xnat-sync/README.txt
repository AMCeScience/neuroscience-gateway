The script 'xnat.sync.sh' is meant for off-line synchronization of the neuroscience catalogue with the XNAT data server.

The script will be automatically executed every night via a cron job, in order to perform the synchronization between the two data servers.
The synchronization is performed in four (04) phases:
- phase 1: synchronize the projects with their corresponding data sets.
- phase 2: assign the users access rights to the projects and the data sets
- phase 3: compute the acquisition date for all the data sets (scans)
- phase 4: compute the matching applications for each data set (scan)

This script makes use of the neuroscience data management API 'syncOffLine-X.X.jar', which takes two parameters, namely UserID, ResourceID and Action.

	UserID: user identifier as defined in the neuroscience catalogue
	Action: UpdateCatalogue | UpdateProjectsUsers | UpdateScanDate | ComputeMatchingApplications | All
	ResourceID: identificator of the data back-end. By default, value 1 = XNAT


