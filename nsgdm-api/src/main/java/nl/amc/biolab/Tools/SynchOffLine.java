/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 Created on : 26 November 2012, 16:59
 Copyright (C) 2013  Academic Medical Center of the University of Amsterdam
 Author: a.benabdelkader@amc.uva.nl

 */
package nl.amc.biolab.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import nl.amc.biolab.Tools.XNATRestClient;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Preference;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.Property;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.UserAuthentication;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

/**
 *
 * This class contains the list of required methods to synchronize the data in
 * the catalogue with the XNAT data server:
 */
public class SynchOffLine {

    /**
     * Create the test case
     *
     * @param AppRetrieveData
     */
    //static String TunnelHost="http://localhost:9898/xnatZ0";
    static String XnatHost = null;
    static String XnatProject = null;
    static String XnatExperiment = null;
    static String XnatUser = null;
    static String XnatMetadata = null;
    static Resource resource = null;
    static Long resourceId = null;

    final static String NO_USER = "User Does not exist in the NSG catalogue";
    final static String NO_PASSWORD = "User Password is not set for XNAT";
    final static String WRONG_PASSWORD = "Wrong user password for XNAT";
    static User user = new User();
    private static UserAuthentication userAuth = null;

    static Collection<Project> projects = new ArrayList<Project>();
    static PersistenceManager pm = PersistenceManager.instance();
    private static final Logger logger = LoggerFactory.getLogger(SynchOffLine.class);

    private static Resource initResource(Long resourceId) throws Exception {
        resource = (Resource) pm.get(Resource.class, resourceId);
        if (resource == null) {
            return null;
        }

        String Str = setProperties(resource.getName());
        if (Str != null) {
            return null;
        }

        return resource;

    }

    /**
     * This method updates the following (from xnat into the catalogue):<ul>
     * <li>list of users
     * <li>list of projects
     * <li>list of data sets (as DataElement)</ul>
     *
     * @param userID Id of the user for which to synchronize
     * @param resourceId Id of the resource from which to synchronize
     * @return String explaining the status or reflecting an error
     * @throws Exception
     */
    public static String updateCatalogue(Long userID, Long resourceId) throws Exception {

        updateUsers(userID, resourceId);
        userAuth = pm.getAuthentication(userID, resourceId);
        XNATRestClient arcGet = new XNATRestClient();

        // Retrieve projects from Xnat for active user
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", XnatProject + "?format=xml"};
        //System.out.println("Getting projects: " + XnatHost + XnatProject + "?format=xml");
        String xmlResult = arcGet.perform(arg);
        if (xmlResult.contains("Login attempt failed. Please try again.")) {
            //System.out.println("nsgdm-api: Login to XNAT failed for user '" + userLogin + "', wrong username or password!");
            return "nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!";

        }

        System.out.print("Updating projects: ");
        projects = getProjects(xmlResult);
        if (projects == null) {
            return "\tThere are no projects to process ...";
        }
        updateDBProjects(projects);

        System.out.print("\n\nUpdating projects Data: ");
        int total = 0;
        for (Project project : projects) {
            System.out.print("\n\t" + project.getName());
            List<String> subjects = getSubjects(project);
            if (subjects == null) {
                continue;
            }
            //System.out.print("\n\t");
            for (String subject : subjects) {
                Collection<Processing> experiments  = getExperiments(project, subject);
                if (experiments != null) {
                    //updateDBExperiments(experiments, user.getDbId(), project.getDbId());
                    for (Processing processing : experiments) {
                        Collection<DataElement> DataElements = getScans(subject, user, project.getXnatID(), processing);
                        if (DataElements != null) {
                            project.getDataElements().addAll(DataElements);
                        }

                        if (DataElements != null) {
                            total = total + DataElements.size();
                            updateDBData(DataElements, subject, project);
                        }
                    }
                }
            }
        }
        user = pm.getUser(userID);
        //updateUsersProjects(resourceId);
        updateUserProjects(resourceId);
        return "Catalogue successffuly updated: " + projects.size() + " projects, " + total + " scans";

    }

    private static Collection<Project> getProjects(String xml) throws Exception {

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        Document doc = db.parse(is);
        // I need to identify the indexes of the different attributes from the col tags
        // .......................
        int indexID = -1, indexName = -1, indexDesc = -1, indexFirst = -1, indexLast = -1;

        NodeList cols = doc.getElementsByTagName("column");
        for (int i = 0; i < cols.getLength(); i++) {
            //Element element = (Element) cols.item(i);
            cols.item(i).getTextContent();
            if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                indexID = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("name")) {
                indexName = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("description")) {
                indexDesc = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("pi_firstname")) {
                indexFirst = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("pi_lastname")) {
                indexLast = i;
            }
        }
        if (indexID == -1 || indexName == -1 || indexDesc == -1 || indexFirst == -1 || indexLast == -1) {
            System.out.println("\n\t-------->ERROR: project XML-Schema has changed at XNAT data server, cannot proceed further!");
            return null;
        }
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Project project = new Project();
            Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            project.setXnatID(((Element) cells.item(indexID)).getTextContent());
            project.setName(((Element) cells.item(indexName)).getTextContent());
            project.setDescription(((Element) cells.item(indexDesc)).getTextContent());
            project.setOwner(((Element) cells.item(indexFirst)).getTextContent() + " " + ((Element) cells.item(indexLast)).getTextContent());
            projects.add(project);

        }
        return projects;

    }

    private static List<String> getSubjects(Project project) throws Exception {

        // need to get all subjects for the project
        String xnatString = XnatProject + "/" + project.getXnatID() + "/subjects";

        String Sbjarg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString + "?format=xml"};
        //System.out.println("Getting subjects: " + XnatHost + xnatString + "?format=xml");
        XNATRestClient xnat = new XNATRestClient();
        List<String> subjects = new ArrayList<String>();

        String results = xnat.perform(Sbjarg);

        if (results.contains("<html><head>")) {
            return null;
        }

        subjects = getXnatSubjects(results);
		//System.out.print(")");

        return subjects;
    }

    private static Collection<Processing> getExperiments(Project project, String subject) throws Exception {

        String xnatString = XnatProject + "/" + project.getXnatID() + "/subjects";
        XNATRestClient xnat = new XNATRestClient();
	    //System.out.print(". ");
        //System.out.print("\n\tSubject: '" + subject + "' Experiments: ");
        String Prjarg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString + "/" + subject + "/experiments?format=xml"};
        //System.out.println("Getting experiments: " + XnatHost + xnatString + "/" + subject + "/experiments?format=xml");
        String results = xnat.perform(Prjarg);

        if (results.contains("<html><head>")) {
            return null;
        }

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(results));
        Document doc = db.parse(is);
        int indexName = -1, indexDesc = -1, indexDate = -1, indexProject = -1;
        NodeList cols = doc.getElementsByTagName("column");
        for (int i = 0; i < cols.getLength(); i++) {
            //Element element = (Element) cols.item(i);
            cols.item(i).getTextContent();
            if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                indexName = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("label")) {
                indexDesc = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("insert_date")) {
                indexDate = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("project")) {
                indexProject = i;
            }
        }
        if (indexName == -1 || indexDesc == -1 || indexDate == -1 || indexProject == -1) {
            System.out.print("\n\t\t ---> Warning: no data or experiment XML-Schema has changed at XNAT data server, for subject '" + subject + "' !\n\t");
            return null;
        }
        Collection<Processing> processings = new ArrayList<Processing>();
        NodeList rows = doc.getElementsByTagName("row");
        //System.out.print(0);
        String datestring = "";
        for (int i = 0; i < rows.getLength(); i++) {
            Processing processing = new Processing();
            Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            processing.setName(((Element) cells.item(indexName)).getTextContent());
            processing.setDescription(((Element) cells.item(indexDesc)).getTextContent());
            datestring = cells.item(indexDate).getTextContent();
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(datestring);
            processing.setDate(date);
            processing.setProject(getProjectByID(((Element) cells.item(indexProject)).getTextContent()));
            //processing.getUsers().add(user);
            //user.getProcessings().add(processing);
            processings.add(processing);

        }
        //System.out.println();
        return processings;
    }

    private static List<String> getXnatSubjects(String xml) throws Exception {

        List<String> subjects = new ArrayList<String>();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        Document doc = db.parse(is);
            // need to identify the indexes of the different attributes from the col tags
        // .......................
        int indexID = -1, indexLabel = -1, indexDate = -1, indexProject = -1;

        NodeList cols = doc.getElementsByTagName("column");
        for (int i = 0; i < cols.getLength(); i++) {
            //Element element = (Element) cols.item(i);
            cols.item(i).getTextContent();
            if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                indexID = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("project")) {
                indexProject = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("label")) {
                indexLabel = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("insert_date")) {
                indexDate = i;
            }
        }
        if (indexID == -1 || indexLabel == -1 || indexDate == -1 || indexProject == -1) {
            System.out.println("\nERROR: subject XML-Schema has changed at XNAT data server, cannot proceed further!");
            return null;
        }
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            subjects.add(((Element) cells.item(indexLabel)).getTextContent());
            //System.out.print(subjects.get(i)+", ");
        }
        return subjects;
    }

    private static Collection<UserAuthentication> updateUsers(Long userID, Long resourceId) throws Exception {
		//initEndPoint(endpoint);
/*    	if (endpoint==null) {
         System.out.println("no EndPoint initialization ...");
         return null;
         }
         */ XNATRestClient arcGet = new XNATRestClient();
		//user = getUser(userID);

        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", XnatUser + "?format=xml"};
        String xmlResult = arcGet.perform(arg);
        if (xmlResult.contains("Login attempt failed. Please try again.")) {
            System.out.println("nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!");
   	    	//return "nsgdm-api: Login to XNAT failed for user '" + user.getUserID() + "', wrong username or password!";

        }

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlResult));
        Document doc = db.parse(is);
        System.out.print("Updating " + resource.getName() + " users ");
        Collection<UserAuthentication> userAuths = new ArrayList<UserAuthentication>();
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            UserAuthentication userAuth = new UserAuthentication();
            Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            userAuth.setUserLogin(((Element) cells.item(1)).getTextContent());
            userAuth.setUser(pm.getUser(userAuth.getUserLogin()));
            userAuth.setResource(pm.getResource(resourceId));
            //user.setFirstName(((Element) cells.item(2)).getTextContent());
            //user.setLastName(((Element) cells.item(3)).getTextContent());
            //user.setEmail(((Element) cells.item(4)).getTextContent());
            userAuths.add(userAuth);

            //System.out.println("userAuth" + (i+1) + ": " + userAuth.getUserID() + "-" + userAuth.getUser() + "-" + userAuth.getResource().toString());
            System.out.print(". ");

        }

        System.out.println(" (" + userAuths.size() + ")");

        boolean found = false;
        Collection<UserAuthentication> DBusers = pm.executeQuery("from UserAuthentication where ResourceID=" + resourceId);
        for (UserAuthentication userAuth : userAuths) {
            for (UserAuthentication dbuser : DBusers) {
                found = false;
                if (dbuser.getUserLogin().equals(userAuth.getUserLogin())) {
                    found = true;
                    break;
                }
            }
            if (!found && userAuth.getUser() != null) {
                //System.out.println(user.getUserID() + " not found");
                DBusers.add(userAuth);
                pm.persist(userAuth);
            }

        }

        return DBusers;
    }

    private static UserAuthentication checkUser(Long userId, Long resourceId) throws RuntimeException {
        userAuth = pm.getAuthentication(userId, resourceId);
        if (userAuth == null) {
            logger.error("Credentials for User '" + userId + "' on Resource '" + resourceId + "' are not defined");
            throw new RuntimeException(NO_USER);
        }

        if (userAuth.getAuthentication() == null) {
            logger.error("user Password is not set for '" + userAuth.getUserLogin() + "' on Resource '" + resourceId + "'");
            throw new RuntimeException(NO_PASSWORD);
        }

        try {
            initResource(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        XNATRestClient arcGet = new XNATRestClient();

        String arg[] = {"-host", XnatHost, "-u", userAuth.getUserLogin(), "-p", decryptString(userAuth.getAuthentication()), "-m", "GET", "-remote", "/data/JSESSION"};
        //String arg[]={"-host",XnatHost, "-u", user.getUserID(), "-p", decryptString(user.getAuthentication()), "-m", "GET", "-remote", "/data/JSESSION"};
        String xmlResult = arcGet.perform(arg);
        System.out.println("JSESSIONID: " + xmlResult + "\tUser: " + user.getLiferayID() + "(" + user.getFirstName() + " " + user.getLastName() + ")");
        if (xmlResult.contains("Login attempt failed. Please try again.") || xmlResult.contains("Unknown Exception. Contact technical support.")) {
            logger.error("Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!");
            throw new RuntimeException(WRONG_PASSWORD);
        }

        if (xmlResult.length() == 32) {
            userAuth.setSession(xmlResult);
            pm.update(userAuth);
            return userAuth;
        }
        return null;

    }

    private static Collection<DataElement> getScans(String subject, User user, String projectName, Processing processing) throws Exception {
        Resource ep = (Resource) pm.get(Resource.class, resource.getDbId());
        String xnatString = XnatProject + "/" + projectName + "/subjects/" + subject + "/experiments/" + processing.getName() + "/scans?format=xml";
// TODO        String xnatString = XnatProject + "/" + projectName + "/subjects/" + subject + "/experiments/" + processing.getName() + "/scans/ALL/resources/?format=xml";
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        //Collection<DataElement> dataelements = new ArrayList<DataElement>();
        String results = xnat.perform(arg);
        if (results.contains("<html><head>")) {
            return null;
        }

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(results));
        Document doc = db.parse(is);
        // I need to identify the indexes of the different attributes from the col tags
        // .......................
        int indexID = -1, indexType = -1, indexURI = -1;

        NodeList cols = doc.getElementsByTagName("column");
        for (int i = 0; i < cols.getLength(); i++) {
            //Element element = (Element) cols.item(i);
            cols.item(i).getTextContent();
            if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                indexID = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("type")) {
                indexType = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("URI")) {
                indexURI = i;
            }
        }
        if (indexID == -1 || indexType == -1 || indexURI == -1) {
            System.out.println("\t ----------> ERROR: experiment scans XML-Schema has changed at XNAT data server, cannot proceed further!");
            return null;
        }
        String scanId = null, scanType = null, scanFormat = null, scanURI = null;

        String xnatString2 = XnatExperiment + "/" + processing.getName() + "/scans/";
        Collection<DataElement> DataElements = new ArrayList<DataElement>();
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            DataElement dataelement = null;
            DataElement de2 = null;
            DataElement de3 = null;
            Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            scanId = ((Element) cells.item(indexID)).getTextContent();
            scanType = ((Element) cells.item(indexType)).getTextContent();
            //scanFormat=((Element) cells.item(indexFormat)).getTextContent();
            scanURI = ((Element) cells.item(indexURI)).getTextContent();
            // TODO this IF statement limits the data sets to be considered based on the type of scans.
            //      It only considers the the scans used within the supported applications
            if (scanType.contains("MPRAGE") || scanType.contains("ADNI") || scanType.contains("T1")
                    || scanType.contains("DTI") || scanType.contains("DIFF") || scanType.contains("Diffusion")) {
                String formats[] = getFormats(xnatString2 + scanId + "/resources?format=json", user);
                for (int s = 0; s < formats.length; s++) {
                    scanFormat = formats[s];
                    System.out.print("\n\tscanFormat: '" + scanFormat + "'");
                    if (scanFormat == null) {
                        break;
                    }
                    if (scanFormat.equalsIgnoreCase("DICOM") /* && CheckDICOM(xnatString2 + scanId + "/resources/DICOM/files?format=xml", user) */) {
                        dataelement = new DataElement();
                        dataelement.setScanID(scanId);
                        dataelement.setURI(XnatHost + scanURI + "/resources/DICOM/files?format=zip");
                        dataelement.setType(scanType);
                        dataelement.setFormat(scanFormat);
                        dataelement.setSubject(subject);
                        dataelement.setName(subject + "." + scanType + "." + scanId + "." + scanFormat);
                        dataelement.setResource(ep);
                        dataelement.setSubject(subject);
                        DataElements.add(dataelement);
                        System.out.print("* ");
                    } else {
                        String niftiURI = CheckNIFTI2(xnatString2 + scanId + "/resources/NIFTI/files?format=json", user);
			        //String niftiURI = CheckNIFTI(xnatString2 + dataelement.getScanID() + "/resources/NIFTI/files?format=html", user );
                        //System.out.println ("niftiURI: " + niftiURI);
                        if (scanFormat.equalsIgnoreCase("NIFTI") && niftiURI != null) {
                            de2 = new DataElement();
                            de2.setScanID(scanId);
                            //dataelement.setURI(XnatHost + ((Element) cells.item(indexURI)).getTextContent()+ "/files?format=zip");
                            de2.setURI(XnatHost + niftiURI);
                            de2.setType(scanType);
                            de2.setFormat(scanFormat);
                            de2.setName(subject + "." + scanType + "." + scanId + "." + scanFormat);
                            de2.setResource(ep);
                            de2.setSubject(subject);
                            DataElements.add(de2);
                            System.out.print("* ");
                        } else {
			        //String parrecURI = CheckPARREC(xnatString2 + dataelement.getScanID() + "/resources/PARREC/files?format=json", user );
                            //String niftiURI = CheckNIFTI(xnatString2 + dataelement.getScanID() + "/resources/NIFTI/files?format=html", user );
                            //System.out.println ("niftiURI: " + niftiURI);
                            if (scanFormat.equalsIgnoreCase("PARREC") /* && CheckPARREC(xnatString2 + scanId + "/resources/PARREC/files?format=json", user) */) {
                                de3 = new DataElement();
                                de3.setScanID(scanId);
                                //dataelement.setURI(XnatHost + ((Element) cells.item(indexURI)).getTextContent()+ "/files?format=zip");
                                de3.setURI(XnatHost + scanURI + "/resources/PARREC/files?format=zip");
                                de3.setType(scanType);
                                de3.setFormat(scanFormat);
                                de3.setName(subject + "." + scanType + "." + scanId + "." + scanFormat);
                                de3.setResource(ep);
                                de3.setSubject(subject);
                                DataElements.add(de3);
                                System.out.print("* ");
                            }
                        }
                    }
                }
            }
        }
        //System.out.println();
        return DataElements;
    }

    
    private static Collection<DataElement> getScans2(String subject, User user, String projectName, Processing processing) throws Exception {
        Resource ep = (Resource) pm.get(Resource.class, resource.getDbId());
        String xnatString = XnatProject + "/" + projectName + "/subjects/" + subject + "/experiments/" + processing.getName() + "/scans/ALL/resources/?format=xml";
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        //Collection<DataElement> dataelements = new ArrayList<DataElement>();
        String results = xnat.perform(arg);
        if (results.contains("<html><head>")) {
            return null;
        }

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(results));
        Document doc = db.parse(is);
        // I need to identify the indexes of the different attributes from the col tags
        // .......................
        int indexType = -1, indexAbstractId=-1, indexFormat=-1, indexCategory=-1, indexId=-1;

        NodeList cols = doc.getElementsByTagName("column");
        for (int i = 0; i < cols.getLength(); i++) {
            //Element element = (Element) cols.item(i);
            cols.item(i).getTextContent();
            if (cols.item(i).getTextContent().equalsIgnoreCase("xnat_abstractresource_id")) {
                indexAbstractId = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("label")) {
                indexFormat = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("category")) {
                indexCategory = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("cat_id")) {
                indexId = i;
            }
            if (cols.item(i).getTextContent().equalsIgnoreCase("cat_desc")) {
                indexType = i;
            }
        }
        if (indexType == -1 || indexAbstractId == -1 || indexFormat == -1 || indexCategory == -1 || indexId == -1) {
            System.out.println("\t ----------> ERROR: experiment scans XML-Schema has changed at XNAT data server, cannot proceed further!");
            return null;
        }
        String scanId = null, scanType = null, scanFormat = null, scanURI = null;

        Collection<DataElement> DataElements = new ArrayList<DataElement>();
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            DataElement dataelement = null;
            DataElement de2 = null;
            DataElement de3 = null;
            Element element = (Element) rows.item(i);
//            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            if (!"scans".equalsIgnoreCase(cells.item(indexCategory).getTextContent())) // only interested in scans
                continue;
            scanId = ((Element) cells.item(indexId)).getTextContent();
            scanType = ((Element) cells.item(indexType)).getTextContent();
            scanFormat=((Element) cells.item(indexFormat)).getTextContent();
            String abstractId = cells.item(indexAbstractId).getTextContent();
            String baseUri = XnatProject + "/" + projectName + "/subjects/" + subject + "/experiments/" + processing.getName() + "/scans/" + scanId;
            if ("DICOM".equalsIgnoreCase(scanFormat)) {
                scanURI = baseUri + "/resources/DICOM/files?format=zip";
            } else if ("PARREC".equalsIgnoreCase(scanFormat)) {
                scanURI = baseUri + "/resources/PARREC/files?format=zip";
            } else if ("NIFTI".equalsIgnoreCase(scanFormat)) {
                scanURI = baseUri + "/resources/"+abstractId+"/files/"+scanId+".nii";
            }
            // TODO this IF statement limits the data sets to be considered based on the type of scans.
            //      It only considers the the scans used within the supported applications
            if (scanType.contains("MPRAGE") || scanType.contains("ADNI") || scanType.contains("T1")
                    || scanType.contains("DTI") || scanType.contains("DIFF") || scanType.contains("Diffusion")) {
                        dataelement = new DataElement();
                        dataelement.setScanID(scanId);
                        dataelement.setURI(scanURI);
                        dataelement.setType(scanType);
                        dataelement.setFormat(scanFormat);
                        dataelement.setSubject(subject);
                        dataelement.setName(subject + "." + scanType + "." + scanId + "." + scanFormat);
                        dataelement.setResource(ep);
                        DataElements.add(dataelement);
                        System.out.print("* ");
            }
        }
        //System.out.println();
        return DataElements;

    }

    private static boolean CheckDICOM(String xnatString, User user) {
        //System.out.println ("\n\n" + xnatString);
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        String results = xnat.perform(arg);
        if (results.contains(".dcm") || results.contains(".DCM")) {
            return true;
        }
        return false;
    }

    private static String CheckNIFTI(String xnatString, User user) {
        //System.out.println ("\n\n" + XnatHost + xnatString);
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        String results = xnat.perform(arg);
        if (results.contains(".nii'>") && results.contains("<a href='")) //if (results.contains("<a href="))
        {
            return results.substring(results.indexOf(".nii'>") + 6, results.indexOf("</a>"));
        }
        return null;
    }

    private static String CheckNIFTI2(String xnatString, User user) {
        System.out.println("\n\nURI: " + XnatHost + xnatString);
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        String results = xnat.perform(arg);
        System.out.println("\nresults:\n" + results);
        int idx = 0;
        if (results.contains("\"URI\":\"")) {
            idx = results.indexOf("\"URI\":\"") + 7;
            if (results.indexOf("\",\"", idx) > idx) {
                return results.substring(idx, results.indexOf("\",\"", idx));
            }
        }
        //if (results.contains("<a href="))
        return null;
    }

    private static boolean CheckPARREC(String xnatString, User user) {
        //System.out.println ("\n\n" + XnatHost + xnatString);
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        String results = xnat.perform(arg);
        int idx = 0;
        if (results.contains("\"Name\":\"")) {
            idx = results.indexOf("\"Name\":\"") + 8;
            if (results.indexOf("\",\"", idx) > idx) {
                return true;
            }
        }
        //if (results.contains("<a href="))
        return false;
    }

    private static String[] getFormats(String xnatString, User user) {
        //System.out.println ("\n\n" + XnatHost + xnatString);
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", xnatString};
        XNATRestClient xnat = new XNATRestClient();
        String results = xnat.perform(arg);
        int i = 0;
        String formats[] = new String[100];
        int idx1 = results.indexOf(",\"label\":\"", 0);
        int idx2 = results.indexOf("\",\"", idx1);
        while (idx1 > 0 && idx2 > 0 && i < 100) {
            //System.out.println ("idx1: " + idx1 + "\tidx2: " + idx2);
            formats[i++] = results.substring(idx1 + 10, idx2);
            idx1 = results.indexOf(",\"label\":\"", idx2);
            idx2 = results.indexOf("\",\"", idx1);
        }
        return formats;
    }

    /**
     * Matches the data elements to their corresponding applications
     *
     * @param userId user identifier
     * @param resourceId resource identifier
     * @return nothing
     * @throws Exception
     */
    public static String setMatchingApplications(Long userId, Long resourceId) throws Exception {
        resource = initResource(resourceId);
        if (resource == null) {
            return null;
        }
        System.out.print(pm.executeUpdate("update DataElement set Applications='#FS#' where (Type like 'T1%' OR Type like '%ADNI%'  OR Type like '%MP%RAGE%') and (not Format like '%PARREC%') and Applications=NULL"));
        System.out.println(" data sets matched FreeSurfer application");
        String metaDataURI = "";
	   	//PersistenceManager pm = new PersistenceManager();
        //User user = pm.init(userId);
        System.out.println("\t~~~~~~~~~~~~~ Updating matching applications for input data (scans) ~~~~~~~~~~~");
        List<Project> projects = (List<Project>) user.getProjects();
        //List<DataElement> dataElements = pm.executeQuery("from DataElement where Applications is NULL");
        if (projects.size() < 1) {
            return null;
        }
        XNATRestClient xnat = new XNATRestClient();
        for (Project project : projects) {
            for (DataElement data : project.getDataElements()) {
                try {
                    if (data.getApplications() == null || data.getApplications().length() < 2) {
    		    //xnat.setMatchingApplications(data, user);

                        if (data.getURI().contains("/data/") && data.getURI().contains("/scans/")) {
                            metaDataURI = data.getURI().subSequence(data.getURI().indexOf("/data/"), data.getURI().indexOf("/scans/") + 6) + "?columns=ID,series_description,frames,insert_date&quality=usable&format=xml";
                        } else {
                            continue;
                        }
        //System.out.println("\t" + data.getDbId() + "- " + XnatHost + metaDataURI);

                        //String arg[]={"-host",XnatHost, "-u", user.getUserID(), "-p", decryptString(user.getAuthentication()), "-m", "GET", "-remote", metaDataURI};
                        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
                        String xml = xnat.perform(arg);

                        if (xml.contains("<h3>The server encountered an unexpected condition which prevented it from fulfilling the request</h3>")) {
                            continue;
                        }

                        String Id, series, frames, applications = "";
                        //Date date2 = null;
                        boolean fs, dti1, dti2;
                        fs = dti1 = dti2 = false;
                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(xml));

                        try {
                            Document doc = db.parse(is);
	        // I need to identify the indexes of the different attributes from the col tags
                            // .......................
                            int indexId = -1, indexSeries = -1, indexFrames = -1, indexDate = -1;

                            NodeList cols = doc.getElementsByTagName("column");
                            for (int i = 0; i < cols.getLength(); i++) {
					//Element element1 = (Element) cols.item(i);
                                //cols.item(i).getTextContent();
                                if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                                    indexId = i;
                                }
                                if (cols.item(i).getTextContent().equalsIgnoreCase("series_description")) {
                                    indexSeries = i;
                                }
                                if (cols.item(i).getTextContent().equalsIgnoreCase("frames")) {
                                    indexFrames = i;
                                }
                                if (cols.item(i).getTextContent().equalsIgnoreCase("insert_date")) {
                                    indexDate = i;
                                }
                            }
                            if (indexId == -1 || indexSeries == -1 || indexFrames == -1 || indexDate == -1) {
                                System.out.println("\t\tERROR: scans metadata XML-Schema has changed at XNAT data server, cannot proceed further!");
                                continue;
                            }

                            NodeList rows = doc.getElementsByTagName("row");
                            for (int i = 0; i < rows.getLength(); i++) {
                                Element element = (Element) rows.item(i);
                                element.getAttribute("row");
                                NodeList cells = element.getElementsByTagName("cell");
                                Id = ((Element) cells.item(indexId)).getTextContent();
                                series = ((Element) cells.item(indexSeries)).getTextContent();
                                frames = ((Element) cells.item(indexFrames)).getTextContent();
	   	 		//}
                                //data.setDate(date2);
                                // Series Description
                                if (Id.equals(data.getScanID())) {
                                    if (series.contains("MPRAGE") || series.contains("ADNI") || series.contains("T1")) {
                                        fs = true;
                                    }
                                    if (series.contains("DTI") || series.contains("DIFF") || series.contains("Diffusion")) {
                                        dti1 = true;
                                    }
                                    if (Integer.parseInt(frames) > 125) {
                                        dti2 = true;
                                    }
                                }

                            }
                            
                            if (data.getFormat().equalsIgnoreCase("nifti")) dti1 = dti2 = false;
                            if (data.getFormat().equalsIgnoreCase("parrec")) fs = false;

                            /*	      if (date!=null) {
                             date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(date);
                             //System.out.println("\t\t" + date + " : \t" + date2 );
                             }
                             */
                            if (fs) {
                                applications = applications + "#FS#";
                                System.out.println("\t-----> Scan " + data.getDbId() + " is suitable for FreeSurfer");
                            }

                            if (dti1 && dti2) {
                                applications = applications + "#DTIp#";
                                System.out.println("\t-----> Scan " + data.getDbId() + " is suitable for DTI preprocessing");
                            }
                            if (applications.length() > 3) {
                                //pm.init();
                                DataElement de = (DataElement) pm.get(DataElement.class, data.getDbId());
                                de.setApplications(applications);
                                //de.setDate(date2);
                                pm.update(de);
                                //pm.shutdown();
                            }

                        } catch (Exception e) {
                            System.out.println("\t ---> Problem parsing xml metadata file for " + data.getDbId() + ": " + data.getName());
                            continue;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //pm.shutdown();

        return null;
    }

    /**
     * Updates the acquisition date for the scans data sets (data elements)
     *
     * @param userId user identifier
     * @param resourceId resource identifier
     * @return nothing
     * @throws Exception
     */
    public static String updateScansDate(Long userId, Long resourceId) throws Exception {
        String date = null;
        String time = null;
        //user = getUser(userID);
        initResource(resourceId);
        if (resource == null) {
            System.out.println("\t ----------> no EndPoint initialization for resource " + resourceId + " ...");
            return null;
        }

        XNATRestClient xnat = new XNATRestClient();
        System.out.print("\n\t~~~~~~~~~~~~~ Updating Scans' acquisition date ~~~~~~~~~~~");
        for (Project project : user.getProjects()) {
            System.out.println("\n\t Project: " + project.getName() + "\t");

            for (DataElement data : project.getDataElements()) {
                //System.out.println("\ndata.getDate(): " + data.getDate());
                if (data.getDate() == null) {
    			//continue;

                    String dataURI = data.getURI();
                    //System.out.println("Processing: " + getExperiment(dataURI));

                    String metaDataURI = XnatMetadata + "/projects/" + project.getXnatID() + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml&field=00080012&field=00080013";
                    //System.out.println("metaDataURI: " + XnatHost+metaDataURI);
                    String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
    	//String arg[]={"-host",XnatHost, "-m", "GET", "-remote", metaDataURI+";jsessionid="+user.getSession()};
                    //String arg[]={"-host",XnatHost, "-u", user.getUserID(), "-p", decryptString(user.getAuthentication()), "-m", "GET", "-remote", metaDataURI};
                    String xml = xnat.perform(arg);

                    if (xml.contains("<h3>The server encountered an unexpected condition which prevented it from fulfilling the request</h3>")) {
                        return null;
                    }

                    if (xml.indexOf("<html><head>") == 0) {
                        return null;
                    }

        //System.out.println(XnatHost + metaDataURI);
                    //System.out.println(xml + "\n" + xml.indexOf("<html><head>"));
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));
                    Document doc = db.parse(is);
        // I need to identify the indexes of the different attributes from the col tags
                    // .......................

                    int indexKey = -1, indexValue = -1;

                    NodeList cols = doc.getElementsByTagName("column");
                    for (int i = 0; i < cols.getLength(); i++) {
				//Element element1 = (Element) cols.item(i);
                        //cols.item(i).getTextContent();
                        if (cols.item(i).getTextContent().equalsIgnoreCase("tag1")) {
                            indexKey = i;
                        }
                        if (cols.item(i).getTextContent().equalsIgnoreCase("value")) {
                            indexValue = i;
                        }
                    }
                    if (indexKey == -1 || indexValue == -1) {
                        System.out.println("\t\tERROR: scans metadata XML-Schema has changed at XNAT data server, cannot proceed further!");
                        return null;
                    }

                    //date = null; time=null;
                    NodeList rows = doc.getElementsByTagName("row");
                    for (int i = 0; i < rows.getLength(); i++) {
                        //Property property = new Property();
                        Element element = (Element) rows.item(i);
                        element.getAttribute("row");
                        NodeList cells = element.getElementsByTagName("cell");
            //property.setKey(((Element) cells.item(indexKey)).getTextContent());
                        //property.setValue(((Element) cells.item(indexValue)).getTextContent());
                        if ((((Element) cells.item(indexKey)).getTextContent()).equalsIgnoreCase("(0008,0012)")) {
                            date = ((Element) cells.item(indexValue)).getTextContent();
                        }
                        if ((((Element) cells.item(indexKey)).getTextContent()).equalsIgnoreCase("(0008,0013)")) {
                            time = ((Element) cells.item(indexValue)).getTextContent();
                        }
                        if (date != null & time != null) {
                            break;
                        }
            //System.out.println("\t\t" + ((Element) cells.item(indexKey)).getTextContent() + " = " + ((Element) cells.item(indexValue)).getTextContent() );

            // get data element properties
                    }
                    //System.out.println("\t\tDate: " + date + "\ttime: " + time );
                    if (date != null && date.length() == 8) {
                        Date date2 = new SimpleDateFormat("yyyyMMddHHmmss").parse(date + time);
                        data.setDate(date2);
                        pm.update(data);
                        System.out.println("\t" + data.getName() + " - " + date + ":" + time + "\t" + date2.toString());
                        System.out.print(". ");
                    }
                }
            }
        }
        System.out.println();
        return null;
    }

    /**
     * Updates the size of the input/output data sets from XNAT (data elements)
     *
     * @param userId user identifier
     * @param resourceId resource identifier
     * @return nothing
     * @throws Exception
     */
    public static String updateDataSize(Long userId, Long resourceId) throws Exception {
        String size = null;
        //user = getUser(userID);
        initResource(resourceId);
        if (resource == null) {
            System.out.println("\t ----------> no EndPoint initialization for resource " + resourceId + " ...");
            return null;
        }

        XNATRestClient xnat = new XNATRestClient();
        System.out.print("\n\t~~~~~~~~~~~~~ Updating Scans' acquisition date ~~~~~~~~~~~");
        for (Project project : user.getProjects()) {
            System.out.println("\n\t Project: " + project.getName() + "\t");

            for (DataElement data : project.getDataElements()) {
                //System.out.println("\ndata.getDate(): " + data.getDate());
                if (data.getURI().contains("?format=zip")) {
                    continue;
                }
                if (data.getSize() == 0) {
	    			//continue;

                    System.out.println("URI: " + data.getURI());
                    if (data.getURI().lastIndexOf("/") + 1 < XnatHost.length()) {
                        continue;
                    }

                    System.out.println("URI: " + data.getURI());
                    String metaDataURI = data.getURI().substring(XnatHost.length(), data.getURI().lastIndexOf("/") + 1) + "?format=json";
                    System.out.println("metaDataURI: " + metaDataURI);

			    	//XnatMetadata + "/projects/" + project.getXnatID() + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml&field=00080012&field=00080013";
                    //System.out.println("metaDataURI: " + XnatHost+metaDataURI);
                    String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
			    	//String arg[]={"-host",XnatHost, "-m", "GET", "-remote", metaDataURI+";jsessionid="+user.getSession()};
                    //String arg[]={"-host",XnatHost, "-u", user.getUserID(), "-p", decryptString(user.getAuthentication()), "-m", "GET", "-remote", metaDataURI};
                    String xml = xnat.perform(arg);

                    if (xml.contains("<h3>The server encountered an unexpected condition which prevented it from fulfilling the request</h3>")) {
                        return null;
                    }

                    if (xml.indexOf("<html><head>") == 0) {
                        return null;
                    }

                    int idx = 0;
                    if (xml.contains("\"Size\":\"")) {
                        idx = xml.indexOf("\"Size\":\"") + 8;
                        if (xml.indexOf("\",\"", idx) > idx) {
                            size = xml.substring(idx, xml.indexOf("\"", idx));
                        }
                    }

                    //System.out.println(XnatHost + metaDataURI);
                    System.out.println("size: " + size);

                    if (size != null) {
                        data.setSize(Integer.parseInt(size));
                        pm.update(data);
                        System.out.println("\t" + data.getName() + " - " + size + ":" + Integer.parseInt(size));
                        System.out.print(". ");
                    }
                }
            }
        }
        System.out.println();
        return null;
    }

    private static Project getProjectByID(String pid) {
        for (Project prj : projects) {
            if (prj.getXnatID().equals(pid)) {
                return prj;
            }
        }
        return null;
    }

    private static String setProperties(String xnatEndPoint) {

        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(xnatEndPoint + ".properties");
        if (stream == null) {
            return "proprieties file for " + xnatEndPoint + " is missing (" + xnatEndPoint + ".properties)";
        };
        try {
            prop.load(stream);
            //set the properties value
            XnatHost = prop.getProperty("XnatHost");
            //TunnelHost=prop.getProperty("XnatHost");
            XnatProject = prop.getProperty("XnatProject");
            XnatExperiment = prop.getProperty("XnatExperiment");
            XnatUser = prop.getProperty("XnatUser");
            XnatMetadata = prop.getProperty("XnatMetadata");
            resourceId = Long.parseLong(prop.getProperty("eCATresourceId"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    private static String decryptString(Blob strb) {
        if (strb == null) {
            return null;
        }
        String str = null;
        try {
            byte[] bdata = strb.getBytes(1, (int) strb.length());
            str = new String(bdata);
            try {
                byte[] decodedBytes = new BASE64Decoder().decodeBuffer(str);
                return new String(decodedBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * main synchronization method, which call the different services based on
     * the user parameters
     *
     * @param userLiferayId: Id of the user for which to synchronize (LiferayId)
     * @param resourceId: Id of the resource from which to synchronize (number)
     * @param Action: synchronization action, one of the following:<ul>
     * <li>UpdateCatalogue: updates list of users, projects, and data sets
     * <li>UpdateScanDate: update the acquisition date for each scan
     * <li>ComputeMatchingApplications: compute the matching applications for
     * each data set
     * <li>UpdateUserProjects
     * <li>All: performs all update actions above. </ul>
     * @throws Exception
     * @HowToCompile From eclipse: export class as runnable jar, then edit the
     * jar by moving the files under resources to the root.
     */
    public static void main(String[] args) throws Exception {
	 //*
        //testMatchingApps(470L);
        //testRecon(16L, 1L);
        //if (1==1)
        //	 return;
        //*/
        if (args.length != 3) {
            System.out.println("wrong argument!...\nUsage: SynchOffLine userId resourceId Action\n\tUserID: user liferayId as in the neuroscience catalogue\n\tresourceId: ID of the resosource to synchronize\n\tAction: UpdateCatalogue | UpdateScanDate | ComputeMatchingApplications | updateUserProjects | All");
            return;
        }
        resourceId = Long.parseLong(args[1]);
        String action = args[2];
        user = pm.init(args[0]);
        pm.setUserPassword(1L, "Ammar", "Benabdelkader99", 1L);
        resource = initResource(resourceId);
        if (resource == null) {
            System.out.println("\t ----------> no EndPoint initialization for resource " + resourceId + " ...");
            return;
        }
        System.out.println("\t ----------> Synchronization process for  " + resource.getName() + " ...");

        userAuth = checkUser(user.getDbId(), resourceId); // this will set the JSESSION

        if (action.equalsIgnoreCase("UpdateCatalogue")) {
            updateCatalogue(user.getDbId(), resourceId);
        }
        if (action.equalsIgnoreCase("updateUserProjects")) {
            updateUserProjects(resourceId);
        }
        if (action.equalsIgnoreCase("ComputeMatchingApplications")) {
            setMatchingApplications(user.getDbId(), resourceId);
        }

        if (action.equalsIgnoreCase("UpdateScanDate")) {
            //for (Resource resource:res) {
            updateScansDate(user.getDbId(), resourceId); // resource.getDbId());
            //}
        }
        if (action.equalsIgnoreCase("All")) {
            //for (Resource resource:res) {
            updateCatalogue(user.getDbId(), resourceId); // resource.getDbId());
            updateScansDate(user.getDbId(), resourceId); // resource.getDbId());
            //}
            setMatchingApplications(user.getDbId(), resourceId);
        }

        if (action.equalsIgnoreCase("storeMetadata")) {
            storeMetadata(user.getDbId(), resourceId); // resource.getDbId());
        }

        if (action.equalsIgnoreCase("UpdateDataSize")) {
            updateDataSize(user.getDbId(), resourceId);
        }

        pm.shutdown();

    }

    private static void testRecon(Long SubmissionId, Long userId, IOPort oPort) {
        pm.init();
        System.out.println(pm.getMasterOutputURI(SubmissionId, userId, oPort));
        pm.shutdown();
    }

    private static void testMatchingApps(Long DataId) {
        pm.init("11231");
        System.out.println(pm.getApplications(DataId).size());
        pm.shutdown();
    }

    private static String getExperiment(String dataURI) {
        dataURI = dataURI.substring(dataURI.indexOf("/experiments/") + 13, dataURI.length());
        //System.out.println("URI:" + dataURI);
        return dataURI.substring(0, dataURI.indexOf("/"));
    }

    private static String updateUserProjects(Long resource) {
        System.out.print("\n\t\t\t~~~~~~~~~ UPDATING PROJECT USERS ~~~~~~~~~~~~\n\nRemoving access rights: ");

        Collection<Project> DBprojects = pm.executeQuery("from Project where ProjectDescription not like '(Archived)%'");
	 //Collection<User> DBusers = pm.executeQuery("from User");

        for (Project p : DBprojects) {
            System.out.print(". ");
            p.getUsers().remove(user);
            //p.getUsers().remove(user);
        }
        pm.update(user);
        System.out.print("Done\n\nGenerating access rights: ");

        try {
            for (String xnatproject : getProjects(resource)) {
                for (Project dbproject : DBprojects) {
                    if (dbproject.getXnatID().equals(xnatproject)) {
                        try {
                            if (getProjectUser(dbproject.getXnatID(), resource, userAuth.getUserLogin())) {
                                System.out.print("\t" + dbproject.getXnatID());
                                user.getProjects().add(dbproject);
                                dbproject.getUsers().add(user);
                                pm.update(user);
                                pm.update(dbproject);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static String updateDBData(Collection<DataElement> DataElements, String subject, Project project) {
        //Project project = pm.getProject(projectId);
        if (project == null) {
            System.out.println("\n\t project doesn't exist....");
            return null;
        }
        Project dbproject = pm.getProject(project.getXnatID(), project.getName());

        boolean found = false;
        Collection<DataElement> DBdataelements = pm.executeQuery("from DataElement");
        for (DataElement dataelement : DataElements) {
            //System.out.println("\nProject:" + project.getXnatID() + " \tData: " + dataelement.getURI());
            for (DataElement dbdataelement : DBdataelements) {
                found = false;
                if (dbdataelement.getURI().equals(dataelement.getURI()) && dbdataelement.getName().equals(dataelement.getName())) {
                    //System.out.println("\n\t" + dataelement.getURI() + " found");
                    if (!dbdataelement.getProjects().contains(dbproject)) {
                        dbdataelement.getProjects().add(dbproject);
                    }
                    if (!dbproject.getDataElements().contains(dbdataelement)) {
                        dbproject.getDataElements().add(dbdataelement);
                    }
                    pm.update(dbdataelement);
                    found = true;
                    break;
                }
            }
            if (!found) {
                //System.out.print("\n\t" + dataelement.getURI() + "(new)"); // not found " + dataelement.getResource().getDbId() + " / " + dataelement.getDbId());
                if (dbproject != null) {
                    dataelement.getProjects().add(dbproject);
                    dbproject.getDataElements().add(dataelement);
                } else {
                    dataelement.getProjects().add(project);
                    project.getDataElements().add(dataelement);
                }

                DBdataelements.add(dataelement);
                pm.persist(dataelement);
            }
	 	//dataelement.getProjects().add(project);

        }

        return null;
    }

    private static String updateDBProjects(Collection<Project> projects) {
        //Project project = pm.getProject(projectId);
        if (projects == null) {
            System.out.println("\n\tThere are no projects ....");
            return null;
        }

        boolean found = false;
        Collection<Project> DBprojects = pm.executeQuery("from Project");
        for (Project project : projects) {
            for (Project dbproject : DBprojects) {
                found = false;
                if (dbproject.getXnatID().equals(project.getXnatID()) && dbproject.getName().equals(project.getName())) {
                    found = true;
                    break;
                }
            }
            System.out.print("\n\t" + project.getXnatID());
            if (!found) {
                System.out.print("(new)"); // not found " + dataelement.getResource().getDbId() + " / " + dataelement.getDbId());
                pm.persist(project);
            }

        }
        return null;
    }

    private static boolean getProjectUser(String projectName, Long resourceId, String userId) throws Exception {
        initResource(resourceId);
        String uri = "/data/projects/" + projectName + "/users?format=xml";
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", uri};
        XNATRestClient xnat = new XNATRestClient();
        String xml = xnat.perform(arg);

     //System.out.println(xml);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        try {
            Document doc = db.parse(is);
            int indexlogin = -1;

            NodeList cols = doc.getElementsByTagName("column");
            for (int i = 0; i < cols.getLength(); i++) {
                if (cols.item(i).getTextContent().equalsIgnoreCase("login")) {
                    indexlogin = i;
                }
            }
	 //System.out.println("column: " + indexlogin);

            if (indexlogin == -1) {
                System.out.println("\t\tERROR: metadata XML-Schema for '" + uri + "' has changed at XNAT data server, cannot proceed further!");
                return false;
            }

            NodeList rows = doc.getElementsByTagName("row");
            for (int i = 0; i < rows.getLength(); i++) {
                Element element = (Element) rows.item(i);
                element.getAttribute("row");
                NodeList cells = element.getElementsByTagName("cell");
                if (userId.equals((((Element) cells.item(indexlogin)).getTextContent()))) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("\t ---> Problem parsing xml metadata file for '" + XnatHost + uri);
            return false;
        }
        return false;
    }

    /**
     * Retrieves the list of projects on the resource as Strings
     *
     * @param endpoint
     * @return
     * @throws Exception
     */
    private static List<String> getProjects(Long resourceId) throws Exception {
        //user = getUser(userID);
        initResource(resourceId);
        String uri = "/data/projects?format=xml";
        String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", uri};
        XNATRestClient xnat = new XNATRestClient();
        String xml = xnat.perform(arg);

     //System.out.println(xml);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        List<String> projects = new ArrayList<String>();

        try {
            Document doc = db.parse(is);
     // I need to identify the indexes of the different attributes from the col tags
            // .......................
            int indexlogin = -1;

            NodeList cols = doc.getElementsByTagName("column");
            for (int i = 0; i < cols.getLength(); i++) {
                if (cols.item(i).getTextContent().equalsIgnoreCase("ID")) {
                    indexlogin = i;
                }
            }
	 //System.out.println("column: " + indexlogin);

            if (indexlogin == -1) {
                System.out.println("\t\tERROR: metadata XML-Schema for '" + uri + "' has changed at XNAT data server, cannot proceed further!");
                return null;
            }

            NodeList rows = doc.getElementsByTagName("row");
            for (int i = 0; i < rows.getLength(); i++) {
                //User user1 = new User();
                Element element = (Element) rows.item(i);
                element.getAttribute("row");
                NodeList cells = element.getElementsByTagName("cell");
                projects.add(((Element) cells.item(indexlogin)).getTextContent());
                //System.out.println("\tuser: " + ((Element) cells.item(indexlogin)).getTextContent());
            }

        } catch (Exception e) {
            System.out.println("\t ---> Problem parsing xml metadata file for '" + XnatHost + uri);
            return null;
        }
        return projects;
    }

    /**
     * This method retrieves the scans (data sets) metadata from xnat and stores
     * them into the catalogue. For reasons related to data privacy this method
     * is not used and metadata is fetched on-line form xnat.
     *
     * @param userId User identifier
     * @param resourceId Resource identifier (where the data resides)
     * @return nothing (stores metadata into the database)
     * @throws Exception Problem parsing the xml file
     */
    public static String storeMetadata(Long userId, Long resourceId) throws Exception {
        System.out.print("\n\t~~~~~~~~~~~~~ Updating Scans' Metadata ~~~~~~~~~~~");
        String metadataPref = getPreference("metadata");
        if (metadataPref == null) {
            System.out.println("WARNING: metadata preferences are not defined for user: " + user.getDbId());
            return null;
        }

        for (Project project : user.getProjects()) {
            System.out.println("\n\t Project: " + project.getName() + "\t");

            for (DataElement data : project.getDataElements()) {
                //System.out.println("\ndata.getDate(): " + data.getDate());
                if (data.getProperties().size() == 0) {

                    String dataURI = data.getURI();
                    System.out.println("Data Element: " + data.getDbId());

                    String metaDataURI = XnatMetadata + "/projects/" + project.getXnatID() + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml";
                    //String metaDataURI = XnatMetadata + "/projects/" + getProjectName(projectId) + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml";
                    String arg[] = {"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
                    XNATRestClient xnat = new XNATRestClient();
                    String xml = xnat.perform(arg);

                    if (xml.contains("<h3>Experiment or project not found</h3>")) {
                        break;
                    }

                 //System.out.println(XnatHost + metaDataURI);
                    //System.out.println(xml);
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));
                    Collection<Property> properties = new ArrayList<Property>();

                    try {
                        Document doc = db.parse(is);
                        int indexKey = -1, indexValue = -1, indexDesc = -1;

                        NodeList cols = doc.getElementsByTagName("column");
                        for (int i = 0; i < cols.getLength(); i++) {
                            if (cols.item(i).getTextContent().equalsIgnoreCase("tag1")) {
                                indexKey = i;
                            }
                            if (cols.item(i).getTextContent().equalsIgnoreCase("value")) {
                                indexValue = i;
                            }
                            if (cols.item(i).getTextContent().equalsIgnoreCase("desc")) {
                                indexDesc = i;
                            }
                        }
                        if (indexKey == -1 || indexValue == -1 || indexDesc == -1) {
                            System.out.println("\t\tERROR: scans metadata XML-Schema has changed at XNAT data server, cannot proceed further!");
                            return null;
                        }

                        NodeList rows = doc.getElementsByTagName("row");
                        for (int i = 0; i < rows.getLength(); i++) {
                            Property property = new Property();
                            Element element = (Element) rows.item(i);
                            element.getAttribute("row");
                            NodeList cells = element.getElementsByTagName("cell");
                            property.setKey(((Element) cells.item(indexKey)).getTextContent());
                            property.setValue(((Element) cells.item(indexValue)).getTextContent());
                            property.setDescription(((Element) cells.item(indexDesc)).getTextContent());
                            if (metadataPref != null & metadataPref.contains(property.getKey())) {
                                data.getProperties().add(property);
                                pm.persist(property);
                            }
                        }

                    } catch (Exception e) {
                        System.out.print("Problem parsing xml metadata file.");
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method return the list of the scans metadata keywords as defined by
     * the user. If metadata is not defined for the user, the method returns the
     * default set.
     *
     * @param metadata Metadata keyword ('default metadata' in this case).
     * @return user metadata preferences
     */
    private static String getPreference(String metadata) {
        String userPref = "null";

        for (Preference pref : user.getPreferences()) {
            if (pref.getKey().equalsIgnoreCase(metadata)) {
                return pref.getValue();
            }
        }
        Collection<Preference> prefs = pm.executeQuery("from Preference where PrefDesc ='" + metadata + "'");

        for (Preference pref : prefs) {
            if (pref.getKey().equalsIgnoreCase(metadata)) {
                return pref.getValue();
            }
        }

        return userPref;
    }
}
