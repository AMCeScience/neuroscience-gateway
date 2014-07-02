#!/bin/bash

# usage: ./refreshAll.sh <portal context>
#
# default portal context for Liferay is "liferay-portal-6.1.0", but in some
# or our instances has been changed by "portal"

CONTEXT=$1

NSGPM_HOME="/home/guse/nsgShellScripts"

java -classpath $NSGPM_HOME/generic_nsgpm_client.jar nl.amc.biolab.nsg.ProcessingManagerClient http://localhost:8080/$CONTEXT/ProcessingManagerService?wsdl

