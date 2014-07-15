neuroscience-gateway
====================

Introduction
------------
The NeuroScience Gateway is a Liferay portlet aimed to work together with gUSE/WSPGRADE. It provides access to distributed computing resources (grid, PBS, SGE, ...) to neuroscientists working with MRI scans.

Componentes
-----------
* amc-theme: Liferay theme for AMC
* ecat
* liferay-sites: predefined Liferay site for AMC NeuroScience Gateway 
* nsg-portlet: NeuroScience Gateway portlet source code
* nsg-tools: auxiliary scripts and debugging tools
* nsgdm-api: synchronisation tools for the data backend
* processingmanager-guse

Requirements
------------
* gUSE/WSPGRADE 3.6.1 or later
* UMD2/3 middleware: gLite, MyProxy, lcg_utils, ...
* XNAT 1.6
* MySQL 5.1
* cURL

Database creation
-----------------
The portlet requires a user and scheme on a MySQL server.

Building .war file
------------------
1. Get the latest source code 
`git clone https://github.com/AMCeScience/neuroscience-gateway.git neuro`
2. Generate portal-nsg.war file using Maven
```
cd neuro/nsg-portlet
mvn -s ../ebioinfra.settings.xml package
```
3. The resulting `portal-nsg.war` file can be found on `neuro/nsg-portlet/target`

Deploying the portlet
---------------------
1. Log in Liferay using and admin/privileged account
2. Go to Control Panel / Plugin Installation / Install more portlets / Upload file
3. Upload the `portal-nsg.war` file
4. Restart Liferay

Configuring Liferay
-------------------
1. Using a Liferay admin account and the control panel, create a `NSG Admin` role
2. Choose one of the Liferay sites and create a new page
3. Insert a `nsg-portal` porlet in the new page

Further documentation and contact info
--------------------------------------
Official NeuroScience Gateway documentation
https://neuro.ebioscience.amc.nl/portal/web/nsg/documentation

For further information and details, contac us at: support-nsg[at]ebioscience[dot]amc[dot]nl
