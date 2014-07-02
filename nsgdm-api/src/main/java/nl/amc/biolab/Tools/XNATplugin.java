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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import nl.amc.biolab.Tools.XNATRestClient;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.Preference;
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
 * This package contains three (03) examples for querying the data based on:
 * 1- Project ID
 * 2- Experiment ID
 * 3- User ID
 */
public class XNATplugin {
    /**
     * Create the test case
     *
     * @param AppRetrieveData
     */
	//static String TunnelHost="http://localhost:9898/xnatZ0";
	static String XnatHost=null;
	static String XnatProject=null;
	static String XnatExperiment=null;
	static String XnatUser=null;
	static String XnatMetadata = null;
	static Resource resource = null;

	public final static String NO_USER="User Does not exist in the NSG catalogue";
	public final static String NO_PASSWORD="User Password is not set for XNAT";
	public final static String WRONG_PASSWORD="Wrong user password for XNAT";
	//static String userLogin="";
	//static String userKey="";
	static User user = new User();
    private UserAuthentication userAuth = null;
 	static Collection<Project> projects = new ArrayList<Project>();
    static PersistenceManager pm = PersistenceManager.instance();
    private static final Logger logger = LoggerFactory.getLogger(SynchOffLine.class);



	public Resource initResource(Long resourceId) throws Exception {
	     //user = getUser(userID);
		// setting resource properties

	     pm.init();
	     resource = (Resource) pm.get(Resource.class, resourceId);
	   	 if (resource==null)
	   		 return null;
	     
	   	 //System.out.print("resource: " + resource.getName());

	   	 String resourceStr = setResourceProperties(resource.getName());
	   	 if (resourceStr!=null) 
	 		return null; //resourceStr; 

	   	 //System.out.println("@" + XnatHost);
	   	 pm.shutdown();

	   	 return resource;

	}
/*	
	private String putReconstruction(String project, String processing, String reconstruction, Long resourceId, Long userId) throws Exception {
	     pm.init();
	     resource = (Resource) pm.get(Resource.class, resourceId);
	   	 if (resource==null)
	   		 return "no EndPoint resources are found ...";
		user = pm.getUser(userId);
		userAuth = pm.getAuthentication(user.getDbId(), resource.getDbId());
	   	pm.shutdown();
		 
	   	 String processingURI = XnatHost + "/data/archive/projects/" + project + "/subjects/013/experiments/" + processing + "/reconstructions/" + reconstruction;
		 System.out.println("URI1:" + processingURI);
		 processingURI = "/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00002/reconstructions/Recon006?type=NIFTI&baseScanType=DTI";
		 //processingURI = "/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00002/reconstructions/Recon001/out/files/XNATRestClient.exe?format=mgz&description=reconstructedImage' -local XNATRestClient.exe";
		 System.out.println("URI2:" + XnatHost + processingURI + "\n" + userAuth.getUserID() + decryptString(userAuth.getAuthentication()));
		//initEndPoint(endPoint);
		XNATRestClient arcGet = new XNATRestClient();
		// Retrieve projects from Xnat for active user
	    String arg[]={"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "PUT", "-remote", processingURI};
	    String xmlResult = arcGet.perform(arg);
        if (xmlResult.contains("Login attempt failed. Please try again.")) {
    	    return "nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserID() + "', wrong username or password!";
        	
        }
        
        return null;
        

	}
	private String putProcessing(String project, String processing, Long resourceId) throws Exception {
	     pm.init();
	     resource = (Resource) pm.get(Resource.class, resourceId);
	   	 pm.shutdown();
	   	 if (resource==null)
	   		 return "no EndPoint resources are found ...";
	   	
	   	userAuth = pm.getAuthentication(user.getDbId(), resource.getDbId());
	   	String processingURI = XnatHost + "/data/archive/projects/" + project + "/subjects/013/experiments/" + processing;
	   	System.out.println("URI:" + processingURI);
	     
		XNATRestClient arcGet = new XNATRestClient();
		// Retrieve projects from Xnat for active user
		//XNATRestClient -host http://mri-neutrino:8080/xnatZ0 -u Ammar -p BenAmmar99 -m PUT -remote "/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00002/reconstructions/FreeSurferDA?type=NIFTI&baseScanType=DTI"
	    String arg[]={"-host",XnatHost, "-user_session", userAuth.getSession(), "-m", "PUT", "-remote", processingURI};
	    //System.out.println(user.getUserID() + ": " + decryptString(user.getAuthentication()) + "--" + XnatHost + XnatProject + "?format=xml");
	    String xmlResult = arcGet.perform(arg);
        if (xmlResult.contains("Login attempt failed. Please try again.")) {
    	    //System.out.println("nsgdm-api: Login to XNAT failed for user '" + userLogin + "', wrong username or password!");
    	    return "nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserID() + "', wrong username or password!";
    	    	
        }
        
        
        return null;
        

	}
	
*/
	/**
	 * Check User authentication on a given Resource
	 * @param userId User Identifier
	 * @param resourceId ResourceIdentifier
	 * @return User's Authentication (encrypted)
	 * @throws RuntimeException
	 */
    public UserAuthentication checkUser(Long userId, Long resourceId) throws RuntimeException 
    {
	     pm.init();
	     User user = getUser(userId);
	     //System.out.println(user.getLastName());
	     if (user==null) {
	    	 logger.error("User '" + userId + "' Doesn't exist in the NSG catalogue");
	    	 throw new RuntimeException(NO_USER);
	     }
		 userAuth = pm.getAuthentication(user.getDbId(), resourceId);
	     if (userAuth==null) {
	    	 logger.error("Authentication for User '" + userId + "' is not defined for resource '" +  resourceId + "'");
	    	 throw new RuntimeException(NO_USER);
	     }
	     
	     if (userAuth.getAuthentication()==null){
	    	 logger.error("Password is not set for user'" + userId + "' for Resource '" + resourceId + "'");	
	    	 throw new RuntimeException(NO_PASSWORD);	
	     }    	 

		try {
			initResource(resourceId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XNATRestClient arcGet = new XNATRestClient();
		
		   String arg[]={"-host",XnatHost, "-u", userAuth.getUserLogin(), "-p", decryptString(userAuth.getAuthentication()), "-m", "GET", "-remote", "/data/JSESSION"};
		   //String arg[]={"-host",XnatHost, "-u", user.getUserID(), "-p", decryptString(user.getAuthentication()), "-m", "GET", "-remote", "/data/JSESSION"};
       String xmlResult = arcGet.perform(arg);
       System.out.println("JSESSIONID: "+ xmlResult + "\tUser: " + user.getLiferayID() + "(" + user.getFirstName() + " " + user.getLastName()+ ")");
       if (xmlResult.contains("Login attempt failed. Please try again.") || xmlResult.contains("Unknown Exception. Contact technical support.")) {
    	   logger.error("Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!");
   	    	throw new RuntimeException(WRONG_PASSWORD);
       }
       
      
    	   //setCookieUsingCookieHandler(xmlResult);
    	   //getCookieUsingCookieHandler();
    	   
    	   
       if (xmlResult.length()==32) {
    	   userAuth.setSession(xmlResult);
    	   //pm.init();
    	   pm.update(userAuth);
    	   //pm.shutdown();
    	   return userAuth;
       }
	   pm.shutdown();
       return null;

    }
    /**
     * retrieves the metadata of a given data element on a given resource
     * @param userId User Identifier
     * @param projectId Project Identifier
     * @param dataId Data Identifier
     * @param resourceId Resource Identifier
     * @return
     * @throws Exception
     */
    public List<Property> getMetadata(Long userId, String xnatPprojectName, Long dataId, Long resourceId) throws Exception
    {
        user = getUser(userId);
		userAuth = pm.getAuthentication(user.getDbId(), resourceId);
        String metadataPref = getPreference("metadata");
        if (metadataPref==null) {
        	System.out.println("WARNING: metadata preferences are not defined for user: " + user.getDbId());
        	return null;
        }

    	DataElement data = getData(dataId);
    	initResource(data.getResource().getDbId());
    	String dataURI = data.getURI();
        //System.out.println("Processing: " + getExperiment(dataURI));

    	String metaDataURI = XnatMetadata + "/projects/" + xnatPprojectName + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml";
    	//String metaDataURI = XnatMetadata + "/projects/" + getProjectName(projectId) + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml";
    	String arg[]={"-host",XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
    	XNATRestClient xnat = new XNATRestClient();
        String xml = xnat.perform(arg);
   	    
//        if (xml.contains("<h3>Experiment or project not found</h3>"))
//        	return null;
        
        //System.out.println(XnatHost + metaDataURI);
        //System.out.println(xml);
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        Collection<Property> properties = new ArrayList<Property>();
        
        try {
        	Document doc = db.parse(is);
        // I need to identify the indexes of the different attributes from the col tags
        // .......................
        int indexKey=-1, indexValue=-1, indexDesc=-1;
    
    NodeList cols = doc.getElementsByTagName("column");
    for (int i = 0; i < cols.getLength(); i++) {
			//Element element1 = (Element) cols.item(i);
			//cols.item(i).getTextContent();
        if (cols.item(i).getTextContent().equalsIgnoreCase("tag1"))
        	indexKey = i;
        if (cols.item(i).getTextContent().equalsIgnoreCase("value"))
        	indexValue = i;
        if (cols.item(i).getTextContent().equalsIgnoreCase("desc"))
        	indexDesc = i;
    }
    if (indexKey==-1 || indexValue==-1 || indexDesc==-1) {
    	System.out.println("\t\tERROR: scans metadata XML-Schema has changed at XNAT data server, cannot proceed further!");
    	return null;
    }
    
    //(0008,0012)		DA	20121107	Instance Creation Date
    //(0008,0013)		TM	150606	Instance Creation Time
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
        	Property property = new Property();
			Element element = (Element) rows.item(i);
            element.getAttribute("row");
            NodeList cells = element.getElementsByTagName("cell");
            property.setKey(((Element) cells.item(indexKey)).getTextContent());
            property.setValue(((Element) cells.item(indexValue)).getTextContent());
            property.setDescription(((Element) cells.item(indexDesc)).getTextContent());
       	 	if (metadataPref!=null & metadataPref.contains(property.getKey()))
       	 		properties.add(property);
            
            //System.out.println("\t\t" + property.getKey() + " = " + property.getValue() + " : " + property.getDescription());
            
            // get data element properties

          }
        
        }
        catch (Exception e) 
        {
            System.out.print("Problem parsing xml metadata file.");
            return null;
        }
        return (List<Property>) properties;
    }
    
   
    private String setResourceProperties(String xnatEndPoint) {
    	
    Properties prop = new Properties();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();           
    InputStream stream = loader.getResourceAsStream(xnatEndPoint + ".properties");
    if (stream==null)
    	return "proprieties file for " + xnatEndPoint + " is missing (" + xnatEndPoint + ".properties)";;
	try {
	    prop.load(stream);
		//set the properties value
		XnatHost=prop.getProperty("XnatHost");
		//TunnelHost=prop.getProperty("XnatHost");
		XnatProject = prop.getProperty("XnatProject");
		XnatExperiment = prop.getProperty("XnatExperiment");
		XnatUser = prop.getProperty("XnatUser");
		XnatMetadata = prop.getProperty("XnatMetadata");

	} catch (IOException ex) {
		ex.printStackTrace();
    }

    return null;

    }
  
    public UserAuthentication setUserPassword(Long userId, String userLogin, String userPass, Long resourceId) {
	    pm.init();
    	
    		userAuth = pm.getAuthentication(userId, resourceId);
    	    	if (userAuth.getUserLogin().equalsIgnoreCase(userLogin)) {
    	    		userAuth.setAuthentication(encryptString(userPass));
    		    	pm.update(userAuth);
    		    	pm.shutdown();
    		    	return userAuth;
    	    	}
    	    
    	    UserAuthentication userAuth = new UserAuthentication();
    	    userAuth.setUser(pm.getUser(userId));
    	    userAuth.setResource(pm.getResource(resourceId));
    	    userAuth.setUserLogin(userLogin);
    	    userAuth.setAuthentication(encryptString(userPass));
    		pm.persist(userAuth);
	    	pm.shutdown();
    		return userAuth;
       }
    

    private static Blob encryptString(String str){
        Blob strb = null;
        try {
    	str = new BASE64Encoder().encodeBuffer(str.getBytes());
        byte[]  buff  = new byte[10];
        buff = str.getBytes();
            strb = new SerialBlob (buff);
    		strb.setBytes(1,buff);
    	} catch (SQLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        return strb;
     }
        
    public static String decryptString(Blob strb){
    	if (strb==null)
    		return null;
    	String str=null;
        try {
	     	byte[] bdata = strb.getBytes(1, (int) strb.length());
	     	str = new String(bdata);
		    try {
				byte[] decodedBytes = new BASE64Decoder().decodeBuffer(str);
				return new String (decodedBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     	
    	} catch (SQLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        return str;
     }
        
/**
 * Retrieves a user by Liferay ID
 * @param LiferayId User Liferay Id
 * @return an object of type User, if any
 */
 public static User getUser(String LiferayId) {
	    pm.init();
	    Collection<User> users = pm.executeQuery("from User where LiferayID ='" + LiferayId + "'");
	    for (User u : users) {
	    	pm.shutdown();
	    	return u;
	    }
	    pm.shutdown();
	    throw new RuntimeException(NO_USER);
	    //return null;
}

 /**
  * Retrieves a user by identifier (DB)
  * @param UserId User Identifier
  * @return an object of type User, if any
  */
 private static User getUser(Long userId) {
	    pm.init();
	    User user = (User)  pm.get(User.class, userId);
	    if (user==null) {
		    pm.shutdown();
	    	throw new RuntimeException("User '" + userId + "' Doesn't exist in the NSG catalogue");
	    }
	    pm.shutdown();
	    return user;
}
/*
 private static String getProjectName(Long ProjectID) {
	    pm.init();
	    Project project = (Project)  pm.get(Project.class, ProjectID);
	    if (project==null) {
		    pm.shutdown();
	    	throw new RuntimeException("Project '" + ProjectID + "' Doesn't exist in the NSG catalogue");
	    }
	    pm.shutdown();
	    return project.getXnatID();
}

*/
 private DataElement getData(Long dataID) {
	    pm.init();
	    DataElement data = (DataElement)  pm.get(DataElement.class, dataID);
	    if (data==null) {
		    pm.shutdown();
	    	throw new RuntimeException("DataElement '" + dataID + "' Doesn't exist in the NSG catalogue");
	    }
	    pm.shutdown();
	    return data;
}

 private String getPreference(String metadata) {
	 String userPref = "null";

	 for (Preference pref : user.getPreferences()) {
		 if (pref.getKey().equalsIgnoreCase(metadata)) {
			   return pref.getValue();
		 }
	 }
    //pm.init();
    Collection<Preference> prefs = pm.executeQuery("from Preference where PrefDesc ='default metadata'");
    //pm.shutdown();
    
    for (Preference pref : prefs) {
    	if (pref.getKey().equalsIgnoreCase(metadata)) {
    		return pref.getValue();
    	}
    }
	   
	   
   return userPref;
}

 private static String getExperiment(String dataURI) {
	 dataURI = dataURI.substring(dataURI.indexOf("/experiments/")+13, dataURI.length());
	 //System.out.println("URI:" + dataURI);
     return dataURI.substring(0, dataURI.indexOf("/"));
}

 
 
 }
