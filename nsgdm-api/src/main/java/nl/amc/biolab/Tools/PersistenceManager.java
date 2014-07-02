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
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Preference;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.Property;
import nl.amc.biolab.nsgdm.Replica;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.Status;
import nl.amc.biolab.nsgdm.Submission;
import nl.amc.biolab.nsgdm.SubmissionIO;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.Error;
import nl.amc.biolab.nsgdm.UserAuthentication;
import org.hibernate.HibernateException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
This code defines the class PersistenceManager with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class PersistenceManager {

    private int oneFile = 1;
    private int manyFiles = 2;
    private int oneVariable = 3;
    private int manyVariables = 4;
  
    static private PersistenceManager instance = null;
    
    //private static SessionFactory sessionFactory = null;
    private static SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
	public final static String WRONG_DATATYPE="Wrong data type!";
   
    private Session session = null;
    private User user = null;
    private UserAuthentication userAuth = null;
    
    // FOR XNAT
	//static String TunnelHost="http://localhost:9898/xnatZ0";
	static String XnatHost=null;
	static String XnatProject=null;
	static String XnatExperiment=null;
	static String XnatUser=null;
	static String XnatMetadata = null;
//	static Resource resource = null;

	public final static String NO_USER="User Does not exist in the NSG catalogue";
	public final static String NO_PASSWORD="User Password is not set for XNAT";
	public final static String WRONG_PASSWORD="Wrong user password for XNAT";

	
    /** 
     * This method instantiates the persistence Manager
     */
    static public PersistenceManager instance() {
        if (null == instance) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    /** 
     * This method opens a hibernate session to the database catalogue 
     */
    private void openSession() {
    	session = sessionFactory.openSession();
    }
    
    /** 
     * This method closes the hibernate session from the database catalogue 
     */
    private void closeSession() {
    	//session.flush();
    	//session.clear();
    	session.close();
    }
    
    /**
     * This method initializes a hibernate user session to the database catalogue
     * @param LiferayId: User liferay Id as a string
     * @return an object of type User
     */
    public User init(String LiferayId) {
    	session = sessionFactory.openSession();
    	user = getUser(LiferayId);
        return user;

    }

    /**
     * This method initializes a hibernate user session to the database catalogue
     * @param UserId: User Database ID DbId Id as in the database catalogue
     * @return an object of type User
     */

    public User init(Long userId) {
    	session = sessionFactory.openSession();
    	user = getUser(userId);
        return user;

    }

    /**
     * This method initializes a hibernate session to the database catalogue
     * without limiting the access to a specific user
     */
    public void init() {
    	session = sessionFactory.openSession();
    }

    /**
     * This method closes the hibernate session to the database catalogue
     * 
     */
    public void shutdown() {
    	session.close();
    	//sessionFactory.close();
    }
    /**
     * This method persists an object to the NSG catalogue
     * @param obj: object of any type (e.g. User, Procrssing, DataElement, etc.)
     */

    public void persist(final Object obj) {
       //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(obj);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
    }
/**
 * This method retrieves a list of objects from the database catalogue
 * @param type: Class type of the object
 * @param oId: Identifier of the object
 * @return an object of the specified type
 */
    public Object get(final Class<?> type, final Serializable oId) {
        Object res = null;
        //Session session = sessionFactory.openSession();
        try {
            res = session.get(type, oId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
        return res;
    }
/**
 * This method deletes the object from the NSG database catalog
 * @param obj: object to be deleted
 */
    public void delete(final Object obj) {
        //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(obj);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
    }
    /**
     * This method updates the object in the NSG database catalog
     * @param obj: object to be updated
     */

    public void update(final Object obj) {
        //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(obj);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
    }
/**
 * This methods returns a list of all objects of type DataElement
 * @return list of all Data elements
 */
    public List<DataElement> getAll() {
        List<DataElement> elements = new ArrayList<DataElement>();
        //Session session = sessionFactory.openSession();
        try {
            Query query = session.createQuery("from DataElement");
            List<Object> results = query.list();
            for (Object o : results) {
            	elements.add((DataElement)o);
            }
        } catch (Exception e) {
             logger.error(e.getMessage());
        } finally {
            //session.close();
        }
        return elements;
    }
    
/**
 * This method execute an HQL query    
 * @param query: query following HQL syntax
 * @return List of results
 */
    public List executeQuery(String HQLquery) {
        List results = null;
        //Session session = sessionFactory.openSession();
        //System.out.println(HQLquery);
        try {
            results = session.createQuery(HQLquery).list();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
        return results;
    }
    /**
     * This method execute an update SQL query    
     * @param squery: query following SQL syntax
     * @return number of updated records
     */

    public int executeUpdate(String squery) {
        int results = 0;
        //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery(squery);
            results = query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
        return results;
    }

    public List<?> executeSQL(String query) {
        List<?> results = null;
        //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            results = session.createSQLQuery(query).list();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage());
        } finally {
            //session.close();
        }
        return results;
    }

    public List<Object> executeSQL(String query, Class type, String gid) {
        List<Object> results = null;
        //Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            results = session.createSQLQuery(query).addEntity(type).setString(0, gid).list();
            tx.commit();
        } finally {
            //session.close();
        }
        return results;
    }

    public User getUser(String LiferayId) {
        List<User> results = null;
        try {
            results = session.createQuery("from User where LiferayID ='" + LiferayId + "'").list();
            //List<User> results = query.list();
            for (User u : results) {
            	return u;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
            return null;
    }
    
    public User getUser(Long userID) {
        try {
            return (User) session.get(User.class, userID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public List<Project> getProjects() {
        try {
            User u = (User) session.get(User.class, new Long(user.getDbId()));
            return (List<Project>) u.getProjects();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public List<Project> getProjects(Long userdbId) {
        try {
            User u = (User) session.get(User.class, userdbId);
            return (List<Project>) u.getProjects();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public Project getProject(Long projectID) {
        try {
            return (Project) get(Project.class, projectID);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\tproject id: " + projectID);
        } 
        return null;
    }
        
    public Project getProject(String XnatId, String Name) {
        try {
        Collection<Project> projects = executeQuery("from Project where XnatID ='" + XnatId + "' and ProjectName ='" + Name + "'");
        for (Project project : projects) {
        	return project;
        }
        } catch (Exception e) {
            logger.error(e.getMessage() + "\t error in getting project: " + XnatId + "-" + Name);
        } 
        return null;
    }
        
    public List<DataElement> getProjectData(Long projectID) {
        try {
            Project project = (Project) session.get(Project.class, projectID);
            if (project==null)
            	return null;
            return (List<DataElement>) project.getDataElements();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public DataElement getDataElement(Long aID) {
        try {
        	return (DataElement) session.get(DataElement.class, aID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
    
    // check existence of a data element based on URI value
    public DataElement checkDataElement(DataElement data) {
        boolean found = false;
        Collection<DataElement> elements = executeQuery("from DataElement where URI ='" + data.getURI() + "'");
            for (DataElement element : elements) {
            	found = true;
            	//u.setLiferayID(LiferayID);
            	//pm.update(u);
            	return element;
            }
            if (!found) {
            	DataElement element  = new DataElement();
            	element.setURI(data.getURI());
            	element.setResource(data.getResource());
            	element.setFormat(data.getFormat());
            	element.setScanID(data.getScanID());
            	element.setSubject(data.getSubject());
            	element.setType(data.getType());
            	persist(element);
            	return element;
            }
        return null;
    }
        
    public List<Application> getApplications() {
        try {
            User u = (User) session.get(User.class, new Long(user.getDbId()));
            return (List<Application>) u.getApplications();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public Application getApplication(Long aID) {
        try {
        	return (Application) get(Application.class, aID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }

        
    public Application getApplicationByName(String appName) {
 	    Collection<Application> apps = executeQuery("from Application where Name ='" + appName + "'");
 	    for (Application app : apps) {
 	    	return app;
 	    }
 	    Application application = new Application();
 	    application.setName(appName);
 	    application.setDescription(appName);
 	    persist(application);

       	return null;

    }
    public List<IOPort> getApplicationPorts(Long aID) {
        try {
        	Application app = (Application) session.get(Application.class, aID);
        	return (List<IOPort>) app.getIOPorts();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public Processing getProcessing(Long pID) {
        try {
        	return (Processing) session.get(Processing.class, pID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
      
       
    public List<Processing> getProcessings() {
        try {
            User u = (User) session.get(User.class, new Long(user.getDbId()));
            return (List<Processing>) u.getProcessings();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public List<Processing> getAllProcessings() {
        try {
            return (List<Processing>) executeQuery("from Processing");
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public List<SubmissionIO> getSubmissionIOs(Long submissionId) {
        try {
        	Submission submission = (Submission) session.get(Submission.class, submissionId);
            if (submission!=null)
            	return submission.getSubmissionIOs();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public Error getSubmissionError(Long submissionId) {
        try {
        	Submission submission = (Submission) session.get(Submission.class, submissionId);
            if (submission!=null && submission.getErrors().size()>0)
            	return submission.getErrors().get(submission.getErrors().size()-1);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
    public List<SubmissionIO> getProcessingIOs(Long processingId) {
    	List<SubmissionIO> submissionIOs = new ArrayList<SubmissionIO>();
        try {
        	Processing processing = (Processing) session.get(Processing.class, processingId);
        	for (Submission submission : processing.getSubmissions()) {
        		submissionIOs.addAll(submission.getSubmissionIOs());    		
        		/*     		for (SubmissionIO so : sub.getSubmissionIOs()) {
     			submissionIOs.add(so);
     		}
*/        	}
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return submissionIOs;
    }
    public Collection<SubmissionIO> getSubmissionInputs(Long submissionId) {
    	Collection<SubmissionIO> submissionIOs = executeQuery("from SubmissionIO where SubmissionID ='" + submissionId + "' and Type='Input'");
       	return submissionIOs;

    }

    public Collection<SubmissionIO> getSubmissionOutputs(Long submissionId) {
    	Collection<SubmissionIO> submissionIOs = executeQuery("from SubmissionIO where SubmissionID ='" + submissionId + "' and Type='Output'");
       	return submissionIOs;

    }

    public List<SubmissionIO> getProcessingOutputs(Long ProcessingId) {
        Processing processing = (Processing) get(Processing.class, new Long(ProcessingId));
        if (processing==null)
        	return null;
        List<SubmissionIO> submissionIOs = new ArrayList<SubmissionIO>();
 		for (Submission sub : processing.getSubmissions()) {
 			submissionIOs.addAll(executeQuery("from SubmissionIO where SubmissionID ='" + sub.getDbId() + "' and Type='Output'"));
 		}
       	return submissionIOs;

    }
        
    public List<SubmissionIO> getProcessingInputs(Long ProcessingId) {
        Processing processing = (Processing) get(Processing.class, new Long(ProcessingId));
        if (processing==null)
        	return null;
        List<SubmissionIO> submissionIOs = new ArrayList<SubmissionIO>();
        Collection<SubmissionIO> submissionIs = new ArrayList<SubmissionIO>();
 		for (Submission sub : processing.getSubmissions()) {
 			//submissionIOs.addAll(executeQuery("from SubmissionIO where SubmissionID ='" + sub.getDbId() + "' and Type='Input'"));
 	    	submissionIs = executeQuery("from SubmissionIO where SubmissionID ='" + sub.getDbId() + "' and Type='Input'");
     		for (SubmissionIO so : submissionIs) {
     			submissionIOs.add(so);
     		}
 		}
       	return submissionIOs;

    }
    
/*    public Application setApplication(String Name, String Description, String Version, String ReleaseDate, String OS, String Platform, String Executable) {
        //DataManager pm = DataManager.instance();
        //init();
        boolean found = false;
        Collection<Application> applications = executeQuery("from Application where Name ='" + Name + "' or Executable='" + Executable + "'");
            for (Application app : applications) {
            	found = true;
            	//u.setLiferayID(LiferayID);
            	//pm.update(u);
            	return app;
            }
            if (!found) {
            	Application app  = new Application();
            	app.setName(Name);
            	app.setDescription(Description);
            	app.setVersion(Version);
            	app.setReleaseDate(ReleaseDate);
            	app.setOS(OS);
            	app.setPlatform(Platform);
            	app.setExecutable(Executable);
            	persist(app);
            	return app;
            }
            //pm.shutdown();

         return null;
    }

*/   
    public List<Application> getApplications(Long dataId) {
        try {
            DataElement de = (DataElement) session.get(DataElement.class, dataId);
            if (de==null || user.getApplications()==null)
            	return null;
            String matchingStr = de.getApplications();
            List<Application> apps = new ArrayList<Application>();
            for (Application app : user.getApplications()){
                for (IOPort ioport : app.getIOPorts()){
                	if (ioport.isVisible() && ioport.getIOType().equalsIgnoreCase("Input") && matchingStr.contains(ioport.getDataFormat()))
                		apps.add(app);
                }
            }
            return apps;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }
        
   public String checkApplicationInput(Long applicationId, List<List<Long>> inputData) throws RuntimeException {
	Long[] ports = getPorts(applicationId, "Input");
   DataElement de = new DataElement();
   String error = "";
   int i = 0;
   if (inputData!=null) {
   	for (List<Long> list_i: inputData) {
   		i = 0;
   		if (list_i.size()!=ports.length) {
   			System.out.println("\t Warning ... " + this.getClass().getCanonicalName() + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "' for Application:" + applicationId);
   			//toReturn = toReturn + this.getClass().getCanonicalName() + ": Processing " + processing.getName() + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "'\n";
   			//continue;
   		}
   			
   	    for (Long l: list_i) {
 			  de = (DataElement) get(DataElement.class, l);
 			  // port j
     		//if (de!=null && !submission.getInputData().contains(de) && i<ports.length) {

       	if (de!=null && i<ports.length) {
 			IOPort port = getPort(ports[i]);  
       		System.out.println("\tChecking input '" + de.getDbId() + "' for application '" + port.getApplication().getName() +"' on port " + port.getDbId());
         	//if ((de.getApplications()==null && port.getDataFormat()!=null) || (port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
            if ((port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
         		error = error + "Wrong data type! expected '" + port.getDataFormat() + "' found '" + de.getApplications() + "' (input: " + de.getDbId() + ")\n";
       		}
       		i++;
   	    }
   	}
   	}
   
   	if (error.length()<1)
   		return null;
   	
   	return error;
}    


    private Long[] getPorts(Long applicationId, String type) {
    	List<Long> ports = new ArrayList<Long>();
        try {
        	Application application = (Application) get(Application.class, applicationId);
        	
        	for (IOPort port: application.getIOPorts()) {
        		if (port.getIOType().equalsIgnoreCase(type) && port.isVisible())
        			ports.add(port.getDbId());
        	}
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        // need to sort the list
		//System.out.println("\n number of ports: " + ports.size());
        Long[] array = ports.toArray(new Long[ports.size()]);
        Arrays.sort(array);
		//System.out.println("\n number of ports: " + array.length);
        return array;
    }
    
    private IOPort getPort(Long portId) {
        try {
        	return (IOPort) get(IOPort.class, portId);
         } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }

    public String getInputURI(Long dataId) {
    	String toReturn="";
        try {
            DataElement de = (DataElement) session.get(DataElement.class, dataId);
            if (de==null)
            	return "";
            toReturn = de.getURI();
            if (toReturn.contains("?format=zip"))
            	toReturn = toReturn.substring(0, de.getURI().indexOf("?format=zip"));
            
            return toReturn.replace(getXnatTunelPath(), getXnatAbsolutePath());
            //return toReturn.replace("http://localhost:9898/", "http://mri-neutrino.amc.nl:8080/")
            //		+ ";jsessionid=" + user.getSession();
            
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
    public String getViewURI(Long dataId) {
    	String toReturn="";
        try {
            DataElement de = (DataElement) session.get(DataElement.class, dataId);
            if (de==null)
            	return "";
            toReturn = de.getURI();
            if (toReturn.contains("/out/files/"))
            	toReturn = toReturn.substring(0, de.getURI().indexOf("/out/files/")+11);
            
            return toReturn.replace(getXnatTunelPath(), getXnatAbsolutePath());
            //return toReturn.replace("http://localhost:9898/", "http://mri-neutrino.amc.nl:8080/")
            //		+ ";jsessionid=" + user.getSession();
            
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
    public String getDownloadURI(Long dataId) {
    	String toReturn="";
        try {
            DataElement de = (DataElement) session.get(DataElement.class, dataId);
            if (de==null)
            	return "";
            toReturn = de.getURI();
            if (toReturn.contains("/out/files/"))
            	toReturn = toReturn.substring(0, de.getURI().indexOf("/out/files/")+10); 
            			//+ "?format=zip";
            
            return toReturn.replace(getXnatTunelPath(), getXnatAbsolutePath());

        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
public String getProcessingReport(Long pId) {
    	
    	String report="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    	String body = "<Document>\n";
    	String inputs = "\t<Inputs>\n";
    	String outputs = "\t<Outputs>\n";
    	String processes = "";
    	int sizeInput = 0;  // total input data size
    	int sizeOutput = 0; // total output data size
    	int in = 0;  // number of inputs
    	int out = 0; // number of outputs
    	int CPUTime = 0; // Total CPU Time
		Processing proc =  (Processing)  get(Processing.class, pId);
		if (proc==null)
			return "No such a Processing: " + pId;

		// query to get start/end time and duration
		List<?> result = executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)) FROM neuroscience.Status where SubmissionID in (select SubmissionID from neuroscience.Submission where Processingid=" + pId + ")");
		
	    Iterator<?> iter = result.iterator();
		Object[] row = (Object[]) iter.next();

		body += "\t<Name>" + proc.getName() + "</Name>\n\t<Description>" + proc.getDescription() + "</Description>\n";
		body +=  "\t<Status>" + proc.getStatus() + "</Status>\n\t<Date>" + proc.getDate() + "</Date>\n";
		body += "\t<ApplicationName>" + proc.getApplication().getName() + "</ApplicationName>\n";
		body += "\t<ApplicationVersion>" + proc.getApplication().getVersion() + "</ApplicationVersion>\n";
		body += "\t<ApplicationDesc>" + proc.getApplication().getDescription() + "</ApplicationDesc>\n";
		body += "\t<User>" + proc.getUsers().iterator().next().getFirstName() + " " + proc.getUsers().iterator().next().getLastName() + "</User>\n";
		body += "\t<NbreTasks>" + proc.getSubmissions().size() + "</NbreTasks>\n";
		body += "\t<StartTime>" + row[0] + "</StartTime>\n";
		body += "\t<EndTime>" + row[1] + "</EndTime>\n";
		body += "\t<ElapsedTime>" + row[2] + "</ElapsedTime>\n";
		body += "\t<CPUTime>###cputime</CPUTime>\n";
		
		processes += "\t<Processes>\n";
		for (Submission sub:proc.getSubmissions()) {
			// query to get start/end time and duration
			List<?> subresult = executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)), time_to_sec(timediff(max(statustime), min(statustime))) FROM neuroscience.Status where SubmissionID=" + sub.getDbId());
			
		    Iterator<?> subiter = subresult.iterator();
			Object[] subrow = (Object[]) subiter.next();
			CPUTime += Integer.parseInt(subrow[3].toString());
			
			processes += "\t\t<Process Name=\"" + sub.getDbId() + "\" Status=\"" + sub.getStatus() + "\"";
			processes += " Start=\"" + subrow[0] + "\" End=\"" + subrow[1]  + "\" Duration=\"" + subrow [2] + "\">\n";
			processes += "\t\t <Statuses>\n";
			for (Status status:sub.getStatuses()) {
				processes += "\t\t  <Status Value=\"" + status.getDbId() + "\" Timestamp=\"" + status.getTimestamp() + "\" />\n";
			}
			

			processes += "\t\t </Statuses>\n\t\t</Process>\n";
			for (SubmissionIO subio:sub.getSubmissionIOs()) {
				if (subio.getType().equalsIgnoreCase("Input")) {
					inputs += "\t\t<Input Name=\"" + subio.getDataElement().getName() + "\" Date=\"" + subio.getDataElement().getDate();
					inputs += "\" Size=\"" + subio.getDataElement().getSize() + "\" ScanID=\"" + subio.getDataElement().getScanID() + "\" />\n";
					sizeInput += subio.getDataElement().getSize();
					in++;
				}
				if (subio.getType().equalsIgnoreCase("Output")) {
					outputs += "\t\t<Input Name=\"" + subio.getDataElement().getName() + "\" Date=\"" + subio.getDataElement().getDate();
					outputs += "\" Size=\"" + subio.getDataElement().getSize() + "\" ScanID=\"" + subio.getDataElement().getScanID() + "\" />\n";									
					sizeOutput += subio.getDataElement().getSize();
					out++;
				}
			}
			
		}
		long days = CPUTime / (60 * 60 * 24) ;                      
		long Hours = (CPUTime - (days*60*60))/ (60*60);                      
		long Minutes = (CPUTime - (days*60*60*24) - (Hours*60*60))/ 60;                      
		long Seconds = CPUTime -(days*60*60*24) - (Hours*60*60) - (Minutes*60);         

		String duration = CPUTime + " Seconds (" + (days>0?days + " days":"") + " " + (Hours>0?Hours + " hours":"") 
										+ " " + (Minutes>0?Minutes + " min":"") + " " + (Seconds>0?Seconds + " sec":"");

		body = body.replaceAll("###cputime", duration);
		body += "\t<AmountInputData>" + sizeInput + " KB</AmountInputData>\n";
		body += "\t<AmountOutputData>" + sizeOutput + " KB</AmountOutputData>\n";
		body += "\t<NbreInputs>" + in + "</NbreInputs>\n";
		body += "\t<NbreOutputs>" + out + "</NbreOutputs>\n";
		body += processes + "\t</Processes>\n";
		inputs += "\t</Inputs>\n";
		outputs += "\t</Outputs>\n";
		//body += "\t<name>" + proc + "</name>\n";
    	
        return report + body + inputs + outputs + "</Document>";
    }
    
public String getProcessingReport2(Long pId) {
	
	String body = null;
	String inputs = null;
	String outputs = null;
	int sizeInput = 0;  // total input data size
	int sizeOutput = 0; // total output data size
	int in = 0;  // number of inputs
	int out = 0; // number of outputs
	int CPUTime = 0; // Total CPU Time
	int Sidx = 0; // submissions' counter

	inputs = "  <tr><th colspan=2>\n"
	+  	"      <table class=\"activitytable\" width=890>\n"
	+  	"        <tr>\n"
	+  	"          <td align=left width=600><font color=#0000ff>Input Data Sets</a></font> <font color=#999999></font></td>\n"
	+  	"          <td align=left width=200><i><font color=#0000ff>Date</font><i></td>\n"
	+  	"          <td align=left width=90><i><font color=#0000ff>Size</font><i></td>\n"
	+  	"        </tr>\n";
	outputs = inputs.replaceAll("Input", "Output");
	
	Processing proc =  (Processing)  get(Processing.class, pId);
	if (proc==null)
		return "No such a Processing: " + pId;

	// query to get start/end time and duration
	List<?> result = executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)) FROM neuroscience.Status where SubmissionID in (select SubmissionID from neuroscience.Submission where Processingid=" + pId + ")");
	
    Iterator<?> iter = result.iterator();
	Object[] row = (Object[]) iter.next();

	body = "  <tr bgcolor=\"#cccccc\">\n"
    +  "    <td colspan=3 align=left bgcolor=\"#ffffcc\"><font color=#990000 size=+1>" + proc.getDescription() + "</font>\n"
    +  "      <br><font color=black>By <b>" + proc.getUsers().iterator().next().getFirstName() + " " + proc.getUsers().iterator().next().getLastName() + 
       "      </b> on: " + proc.getDate() + " </font>\n"
    +  "    </td>\n"
    +  "    <td colspan=3 align=left bgcolor=\"#ffffcc\"><font color=#990000 size=+1>" + proc.getApplication().getName() + "</font><br>\n"
    +  "      <font color=666666>\t" + proc.getApplication().getDescription() + "</font>\n"
    +  "    </td>\n"
    +  "  </tr>\n"

	+  "  <tr>\n"
    +  "    <th width=50></th><th align=left bgcolor=#dcddc0 width=150>Start Time: </th><th align=left width=250>" + row[0] + "</th>\n"
    +  "    <th width=50></th><th align=left bgcolor=#dcddc0 width=150>Numbre of Tasks: </th><th align=left width=250>" + proc.getSubmissions().size() + "</th>\n"
    +  "  </tr>\n"
    +  "  <tr>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>End Time: </th><th align=left>" + row[1] + "</th>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>Input data sets: </th><th align=left>###input / ###inputsize MB</th>\n"
    +  "  </tr>\n"
    +  "  <tr>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>Elapsed Time: </th><th align=left>" + row[2] + "</th>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>Output data sets: </th><th align=left>###output / ###outputsize MB</th>\n"
    +  "  </tr>\n"
    +  "  <tr>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>CPU Time: </th><th align=left>###cputime</th>\n"
    +  "    <th></th><th align=left bgcolor=#dcddc0>Final Status: </th><th align=left>" + proc.getStatus() + "</th>\n"
    +  "  </tr>\n"
    +  "</table>\n";
	
	
	body += "<p>\n<table class=\"reporttable\" width=900>\n" 
	+  	"  <tr>\n"
	+  	"    <th>\n"
	+  	"      <table class=\"activitytable\" width=560>\n"
	+  	"        <tr>\n"
	+  	"          <td align=left width=400><font color=#0000ff>Processes</a></font> <font color=#999999></font></td>\n"
	+  	"            <td align=left width=80><i><font color=#0000ff>Duration</font><i></td>\n"
	+  	"            <td align=left width=80><i><font color=#0000ff>Status</font><i></td>\n"
	+  	"        </tr>\n";

	for (Submission sub:proc.getSubmissions()) {
		// query to get start/end time and duration
		List<?> subresult = executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)), time_to_sec(timediff(max(statustime), min(statustime))) FROM neuroscience.Status where SubmissionID=" + sub.getDbId());
		
	    Iterator<?> subiter = subresult.iterator();
		Object[] subrow = (Object[]) subiter.next();
		CPUTime += Integer.parseInt(subrow[3].toString());
		Sidx++;
		
		body +=	"        <tr><th align=left><i><a href=\"#\" onclick=\"toggleDisplay('toggleActivity" + Sidx + "');\">" + Sidx + "- " + sub.getName() + "</a></i>\n"
			  + "          <div id=\"toggleActivity" + Sidx + "\" style=\"display:none\">\n"
			  + "            <table class=\"smalltable\" width=270>\n"
			  + "              <tr><td width=10></td><td width=60>Start Time:</td><td width=200>" + subrow[0] + "</td></tr>\n"
		      + "              <tr><td width=10></td><td>End Time:</td><td>" + subrow[1]  + "</td></tr>\n"
			  + "              <tr><td width=10></td><td colspan=2>Executions details:<br>";

		String st = "";
		for (Status status:sub.getStatuses()) {
			//processes += "\t\t  <Status Value=\"" + status.getDbId() + "\" Timestamp=\"" + status.getTimestamp() + "\" />\n";
			if (!status.getValue().equalsIgnoreCase(st))
				body +=	" - " + status.getValue() + ": " +status.getTimestamp() + "<br>";
			st = status.getValue();
		}
		body +=	"</td></tr>\n"
		+  	"            </table>\n"
		+  	"          </div>\n"
		+  	"         </th><th valign=top>" + subrow [2] + "</th><th valign=top>" + sub.getStatus() + "</th></tr>\n";
		
		for (SubmissionIO subio:sub.getSubmissionIOs()) {
			if (subio.getType().equalsIgnoreCase("Input")) {
				in++;
				inputs += "        <tr><th align=left><i><a href=\"#\" onclick=\"toggleDisplay('toggleInput" + in + "');\">" + in + "- " + subio.getDataElement().getName() + "</a></i>\n"
						+  	"          <div id=\"toggleInput" + in + "\" style=\"display:none\">\n"
						+  	"            <table class=\"smalltable\" width=300>\n"
						+  	"              <tr><td width=10></td><td colspan width=80>Scan ID:</td><td width=210>" + subio.getDataElement().getScanID() + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Subject Id:</td><td>" + subio.getDataElement().getSubject()  + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Data Type:</td><td>" + subio.getDataElement().getType()  + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Data Format:</td><td>" + subio.getDataElement().getFormat()  + "</td></tr>\n";
	
				inputs +=	"            </table>\n"
						+  	"          </div>\n"
						+  	"        </th><th valign=top>" + subio.getDataElement().getDate() + "</th><th valign=top>" + subio.getDataElement().getSize() + "</th></tr>\n";
						
			}
			if (subio.getType().equalsIgnoreCase("Output")) {
				out++;
				outputs += "        <tr><th align=left><i><a href=\"#\" onclick=\"toggleDisplay('toggleOutput" + out + "');\">" + out + "- " + subio.getDataElement().getName() + "</a></i>\n"
						+  	"          <div id=\"toggleOutput" + out + "\" style=\"display:none\">\n"
						+  	"            <table class=\"smalltable\" width=300>\n"
						+  	"              <tr><td width=10></td><td colspan width=80>Scan ID:</td><td width=210>" + subio.getDataElement().getScanID() + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Subject Id:</td><td>" + subio.getDataElement().getSubject()  + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Data Type:</td><td>" + subio.getDataElement().getType()  + "</td></tr>\n"
						+  	"              <tr><td width=10></td><td>Data Format:</td><td>" + subio.getDataElement().getFormat()  + "</td></tr>\n";
	
				outputs +=	"            </table>\n"
						+  	"          </div>\n"
						+  	"        </th><th valign=top>" + subio.getDataElement().getDate() + "</th><th valign=top>" + subio.getDataElement().getSize() + "</th></tr>\n";
			}
		}
		
	}
	
	inputs += "      </table>\n    </th></tr>\n";
	outputs += "      </table>\n    </th></tr>\n";
	long days = CPUTime / (60 * 60 * 24) ;                      
	long Hours = (CPUTime - (days*60*60))/ (60*60);                      
	long Minutes = (CPUTime - (days*60*60*24) - (Hours*60*60))/ 60;                      
	long Seconds = CPUTime -(days*60*60*24) - (Hours*60*60) - (Minutes*60);         

	String duration = (days>0?days + " days":"") + " " + (Hours>0?Hours + " hours":"") 
									+ " " + (Minutes>0?Minutes + " min":"") + " " + (Seconds>0?Seconds + " sec":"");
	
	body +=	"        <tr><td><font color=#990000 colspan=36>Total CPU Time:</font> \t<i>" + duration + "</font><i></td></tr>\n"
			+ "      </table>\n"
			+ "    </th>\n"
			+ "    <th width=330 align=middle valign=top><i><a href=\"#\" onclick=\"toggleDisplay('toggleGraph');\">+ data flow</a> <br>\n"
			+ "      <div id=\"toggleGraph\" style=\"display:''\">\n"
			//+ "	       <a href=\"10826.png\" target=_blank><img src=\"10826.png\" width=340 border=0></a>\n"
			+ getApplicationGraph(proc.getApplication().getDbId())
			+ "      </div>\n"
			+ "    </th>\n"
			+ "  </tr>\n";
	
	duration = (days>0?days +":":"") + (Hours>0?Hours + ":":"") 
			+ (Minutes>0?Minutes + ":":"") + (Seconds>0?Seconds:"");


	body = body.replaceAll("###cputime", duration);
	body = body.replaceAll("###inputsize", String.valueOf(sizeInput));
	body = body.replaceAll("###input", String.valueOf(in));
	body = body.replaceAll("###outputsize", String.valueOf(sizeOutput));
	body = body.replaceAll("###output", String.valueOf(out));
	/*
	body += "\t<AmountInputData>" + sizeInput + " KB</AmountInputData>\n";
	body += "\t<AmountOutputData>" + sizeOutput + " KB</AmountOutputData>\n";
	body += "\t<NbreInputs>" + in + "</NbreInputs>\n";
	body += "\t<NbreOutputs>" + out + "</NbreOutputs>\n";
	body += processes + "\t</Processes>\n";
	inputs += "\t</Inputs>\n";
	outputs += "\t</Outputs>\n";
	//body += "\t<name>" + proc + "</name>\n";
*/	
    return getHeaderReport() + body + inputs + outputs + "</table>\n</html>";
}

    public boolean isDataSourceAlive(Long resourceId) throws Exception {
        HttpURLConnection httpUrlConn;
    	XNATplugin xnat = new XNATplugin ();
    	xnat.initResource(resourceId);
	    String urlString = XNATplugin.XnatHost;
        try {
            httpUrlConn = (HttpURLConnection) new URL(urlString)
                    .openConnection();
 
            // A HEAD request is just like a GET request, except that it asks
            // the server to return the response headers only, and not the
            // actual resource (i.e. no message body).
            // This is useful to check characteristics of a resource without
            // actually downloading it,thus saving bandwidth. Use HEAD when
            // you don't actually need a file's contents.
            httpUrlConn.setRequestMethod("HEAD");
 
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);
 
            // Print HTTP status code/message for your information.
/*            System.out.println("Response Code: "
                    + httpUrlConn.getResponseCode());
            System.out.println("Response Message: "
                    + httpUrlConn.getResponseMessage());
 
*/            return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\t" + urlString);
            return false;
        }
    }
    @SuppressWarnings("unchecked")
	private String getXnatAbsolutePath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='xnat absolute path'");
       if (prefs.size()<1)
    	   return "";
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return "";
   }
    
    @SuppressWarnings("unchecked")
	private String getXnatTunelPath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='xnat tunel path'");
       if (prefs.size()<1)
    	   return "";
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return "";
   }
    
    public Collection<Submission> getProcessingSubmissions(Long processingId) {
    	Processing processing = (Processing) get(Processing.class, processingId);
        if (processing==null)
        	return null;
    	return processing.getSubmissions(); 
    }

public Long getNewProcessingID(Long project, Long application) {
	Processing processing  = new Processing();
	processing.setName("Initialization Name");
	processing.setApplication(getApplication(application));
	processing.setProject(getProject( project));
	persist(processing);
	return processing.getDbId();

}


public Long storeProcessing(Long ProcessingId, String Name, String Description, Long UserID, String processingStatus) {
    Processing processing = (Processing) get(Processing.class, ProcessingId);
    if (processing==null) {
    	return null;
    }
    processing.setName(Name);
    processing.setDescription(Description);
    processing.setDate(new Date());
    processing.setStatus(processingStatus);
    processing.setLastUpdate(new Date());
    processing.getUsers().add((User) get(User.class, UserID));
/*    	if (inputData!=null)
		storeProcessingInput(processing.getDbId(), inputData);
*/    	update(processing);
	return processing.getDbId();

}

public void storeProcessingStatus(Long ProcessingID, String status) {
	Processing processing = (Processing) get(Processing.class, new Long(ProcessingID));
         
    if (processing!=null && status!=null) {
    	processing.setStatus(status);
        processing.setLastUpdate(new Date());
       update(processing);
    }
 }


private boolean checkProcessingInput(Long processingId, List<List<Long>> inputData) throws RuntimeException {
	Processing processing = (Processing) get(Processing.class, processingId);
	Long[] ports = getInPorts(processing.getDbId());
DataElement de = new DataElement();
String toReturn = null;
//System.out.print("\t checking Input data for processing ... " + processing.getName());
int i = 0;
if (inputData!=null) {
	for (List<Long> list_i: inputData) {
		i = 0;
		if (list_i.size()!=ports.length) {
			System.out.print("\t Warning ... " + this.getClass().getCanonicalName() + ": Processing " + processing.getName() + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "'");
			//toReturn = toReturn + this.getClass().getCanonicalName() + ": Processing " + processing.getName() + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "'\n";
			//continue;
		}
			
	    for (Long l: list_i) {
			  de = (DataElement) get(DataElement.class, l);
			  // port j
  		//if (de!=null && !submission.getInputData().contains(de) && i<ports.length) {

    	if (de!=null && i<ports.length) {
      			IOPort port = getPort(ports[i]);  
           		System.out.println("\tChecking input '" + de.getDbId() + "' for application '" + port.getApplication().getName() +"' on port " + port.getDbId());
             	//if ((de.getApplications()==null && port.getDataFormat()!=null) || (port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
                if ((port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
             		toReturn = toReturn + "Wrong data type! expected '" + port.getDataFormat() + "' found '" + de.getApplications() + "' (input: " + de.getDbId() + ")\n";
           		}
    		i++;
	    }
	}
	}
if (toReturn!=null) 
	throw new RuntimeException(toReturn);

System.out.println("\t ....Valid");    

	return true;
}    

public int storeSubmissionInput(Long submissionId, List<List<Long>> inputData) {
	Submission submission = (Submission) get(Submission.class, submissionId);
	Long ProcessingId = submission.getProcessing().getDbId();
	Long[] ports = getInPorts(ProcessingId);
DataElement de = new DataElement();
//String reconString = "";
int i = 0;
if (submission!=null) {
	for (List<Long> list_i: inputData) {
		i = 0;
		if (list_i.size()!=ports.length) {
			System.out.println("\t Warning ... " + this.getClass().getCanonicalName() + ": Processing " + ProcessingId + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "'");
			logger.warn(": Processing " + ProcessingId + ": number of inputs '" + list_i.size() + "' doesn't much number of ports '" + ports.length + "'", this.getClass().getCanonicalName());
			//continue;
		}
			
	    for (Long l: list_i) {
			  de = (DataElement) get(DataElement.class, l);
			  // port j
  		//if (de!=null && !submission.getInputData().contains(de) && i<ports.length) {

    	if (de!=null && i<ports.length) {
    			SubmissionIO sin = new SubmissionIO();
  			
    			//System.out.println("\t    Port @" + (i) + " = " + ports[i]);
      			IOPort port = getPort(ports[i]);

      			//if (!port.getDataFormat().contains(de.getFormat()))
                if ((port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
             	//if ((de.getApplications()==null && port.getDataFormat()!=null) || (port.getDataFormat()!=null && de.getApplications()!=null  && !de.getApplications().contains(port.getDataFormat())))
      				System.out.println("Warning: wrong data type! expected " + port.getDataFormat() + " found " + de.getApplications());
      			
    			sin.setDataElement(de);
    			sin.setSubmission(submission);
    			sin.setPort(port);
    			sin.setType("Input");
    			persist(sin);
    	    	//storeProcessingStatus(pin.getDbId(), "Created", new Date());
/*        			reconString=de.getURI();
    			reconString = reconString.substring(0, reconString.indexOf("/scans/"));
    			reconString = reconString.replaceAll("/data/experiments/", "/data/archive/projects/" + submission.getProcessing().getProject().getXnatID() + "/subjects/" + de.getSubject() + "/experiments/");
    			reconString = reconString + "/reconstructions/" + submission.getProcessing().getName() + "_NSG" + sin.getDbId() + "?type=RECON&baseScanType=" + de.getType();
    					
    			try {
					createReconstruction(reconString, de.getResource().getDbId(), userId);
				} catch (Exception e) {
					e.printStackTrace();
				}
*/
    		}
    		i++;
	    }
	}
	}
	return inputData.size();
}    


/*    public Collection<SubmissionIO> getProcessingOutput(Long ProcessingId, Long portId) {
    Processing processing = (Processing) get(Processing.class, new Long(ProcessingId));
    Collection<SubmissionIO> outputs = new ArrayList<SubmissionIO>();
		for (Submission sub : processing.getSubmissions()) {
 		for (SubmissionIO so : sub.getOutputData()) {
 			if (so.getPort().getDbId()==portId)
 				outputs.add(so);
 		}
		}
   	return outputs;

}

*/  
/*    public Collection<DataElement> getProcessingOutput(Long ProcessingId) {
    Processing processing = (Processing) get(Processing.class, new Long(ProcessingId));
    Collection<DataElement> outputs = null;
		for (Submission sub : processing.getSubmissions()) {
 		for (SubmissionOutput so : sub.getOutputData()) {
 			outputs.add(so.getOutputData());
 		}
		}
   	return outputs;

}

public Collection<DataElement> getProcessingOutput(Long ProcessingId, Long portId) {
    Processing processing = (Processing) get(Processing.class, new Long(ProcessingId));
    Collection<DataElement> outputs = null;
		for (Submission sub : processing.getSubmissions()) {
 		for (SubmissionOutput so : sub.getOutputData()) {
 			if (so.getPort().getDbId()==portId)
 				outputs.add(so.getOutputData());
 		}
		}
   	return outputs;

}

*/
public IOPort getSubmissionOutputPort(Long SubmissionId) {
	Submission submission = (Submission) get(Submission.class, new Long(SubmissionId));
	    for (IOPort port : submission.getProcessing().getApplication().getIOPorts()) {
	    	if (port.getIOType().equals("Output") && port.isVisible())
	    		return port;
	    }
	return null;
}

public IOPort getSubmissionInputPort(Long SubmissionId) {
	Submission submission = (Submission) get(Submission.class, new Long(SubmissionId));
	    for (IOPort port : submission.getProcessing().getApplication().getIOPorts()) {
	    	if (port.getIOType().equals("Input") && port.isVisible())
	    		return port;
	    }
	return null;
}

public Long storeSubmissionOutput(Long SubmissionId, Long portId, String URI) {
    Submission submission = (Submission) get(Submission.class, new Long(SubmissionId));
	//System.out.println("storeSubmissionOutput: Id"  + SubmissionId + "  port: " + portId + " URI:" + URI);

    Resource resource = (Resource) get(Resource.class, 1L); // TODO
    IOPort outputport = (IOPort) get(IOPort.class, new Long(portId));
    //IOPort outputport = getSubmissionOutputPort(SubmissionId);

    if (submission==null || resource==null  || outputport==null ) 
    	return 0L;

   

    IOPort inputport = new IOPort();

	    //Collection<SubmissionIO> SIOs = executeQuery("from SubmissionIO where SubmissionID ='" + SubmissionId + "'");
    if (submission.getSubmissionIOs().isEmpty() || resource==null ) 
    	return 0L;
    
	String baseType="";
	String scanId="";
	String appName="";
	    for (SubmissionIO sin : submission.getSubmissionIOs()) {
	    	if (sin.getPort().getIOType().equals("Input")){
	    		inputport = sin.getPort();
	    		baseType = sin.getPort().getDataFormat();
	    		scanId = sin.getDataElement().getScanID();
	 	    	appName = sin.getPort().getApplication().getName();
	 	    	resource = sin.getDataElement().getResource();
	    	}
	    }

    DataElement de = new DataElement();
    // extract subject from URI
    /*
    int idx1 = URI.indexOf(".Recon.")+7;
    if (idx1 >7)
    	de.setScanID(URI.substring(idx1, URI.indexOf(".", idx1+1)));
     */    
    de.setScanID(scanId);
    //de.setType(port.getDataType());
    de.setType("Recon " + appName);

    if (resource.getName().equalsIgnoreCase("webdav")) {
	    de.setSubject("subject");
	    de.setURI(URI);
	    de.setName("output-" + appName + "_autodock" + submission.getDbId());
    }
    else {
	    de.setSubject(URI.substring(URI.indexOf("/subjects/")+10, URI.indexOf("/experiments/")));
	    de.setURI(URI.substring(0,URI.lastIndexOf("?format")));
	    de.setName("output-" + appName + "_NSG" + submission.getDbId());
    }
    
    //de.setName("output-" + submission.getName() + ":" + port.getPortName() + "@" + resource.getName());
    de.setResource(resource);
    de.getPorts().add(inputport);
    de.setDate(new Date());
    de.getProjects().add(submission.getProcessing().getProject());
    //de.setSubject();
    
    if (outputport!=null){
    	de.setFormat(outputport.getDataFormat());
    	de.setApplications(outputport.getOutputApps());
        de.getPorts().add(outputport);
    }

    persist(de);
    
    if (outputport!=null){
        SubmissionIO po = new SubmissionIO();
        po.setSubmission(submission);
        po.setDataElement(de);
        po.setPort(outputport);
        po.setType("Output");
        persist(po);
    }
    
	return de.getDbId();

}

public String getMasterOutputURI(Long SubmissionId, Long userId, IOPort oPort) {
	    Collection<SubmissionIO> SIOs = executeQuery("from SubmissionIO where SubmissionID ='" + SubmissionId + "' and Type='Input'");
	    //System.out.println("query: "  + "from SubmissionIO where SubmissionID ='" + SubmissionId + "' and Type='Input'");
	    String reconString="";
    	if (SIOs==null)
    		return null;
	    for (SubmissionIO sin : SIOs) {
            final Submission submission = sin.getSubmission();
            final Processing processing = submission.getProcessing();
            final Project project = processing.getProject();
            final DataElement dataElement = sin.getDataElement();
            final Resource resource = dataElement.getResource();
	    	// if not xnat data server (e.g. webdav), return resource base uri
	    	if (resource.getName().equalsIgnoreCase("webdav"))
	    		return resource.getBaseURI() + project.getName().replace(" ", "%20") + "/output.tar.gz";
            final String xnatID = project.getXnatID();
            final String subject = dataElement.getSubject();
            final String dataType = dataElement.getType().replace(" ", "_");
	    	String today = new SimpleDateFormat("ddMMyyyy'_'HHmmssSSS").format(new Date()); 
            
            String reconDest = "/reconstructions/" + xnatID.replace(".", "_") + "_" + dataType.replace(".", "_") + "_NSG" + submission.getDbId() + "_" + today;
//            reconDest = reconDest.replace(" ", "_");

			reconString=dataElement.getURI();
            if (reconString.indexOf("/scans/") > 0) {
                reconString = reconString.substring(0, reconString.indexOf("/scans/"));
                reconString = reconString.replaceAll("/data/experiments/", "/data/archive/projects/" + xnatID + "/subjects/" + subject + "/experiments/");
                //reconString = reconString + "/reconstructions/" + sin.getSubmission().getProcessing().getName() + "_NSG" + sin.getSubmission().getDbId();
                reconString = reconString + reconDest; //new Date().getTime();
            } else if (reconString.indexOf("/reconstructions/") > 0) {
                reconString = reconString.substring(0, reconString.indexOf("/reconstructions/"));
                //reconString = reconString + "/reconstructions/" + sin.getSubmission().getProcessing().getName() + "_NSG" + sin.getSubmission().getDbId();
                reconString = reconString + reconDest;
            } else {
                return null;
            }
            
            String reconstructionUri = reconString + "?type=" + processing.getApplication().getDescription().replace(" ", ".") + "&baseScanType=" + dataType;
	
			try {
				//createReconstruction(reconString + "?type=" + oPort.getDataFormat() + "&baseScanType=" + sin.getDataElement().getFormat(), sin.getDataElement().getResource().getDbId(), userId);
				//createReconstruction(reconString + "?type=" + sin.getDataElement().getType().replaceAll(" ", ".") + "&baseScanType=" + sin.getDataElement().getFormat(), sin.getDataElement().getResource().getDbId(), userId);
				createReconstruction(reconstructionUri, resource.getDbId(), userId);
			} catch (Exception e) {
				e.printStackTrace();
                throw new RuntimeException(e);
			}
            final String reconType = oPort.getDataFormat().replace(" ", "_");
				
			//reconString = reconString + "/out/files/" + fileName + "?format=zip&description=reconstructedImage&collection=" + sin.getSubmission().getProcessing().getApplication().getName();
			//reconString = reconString + "/out/files/" + sin.getDataElement().getSubject() 
			reconString = reconString + "/resources/" + processing.getName() + "/files/" + subject 					
					+ ".Recon." + dataElement.getScanID() + "." + reconType
				    + "?format="+ reconType
					+ "&description=reconstructedImage&collection=" + processing.getApplication().getInternalName();
	
            System.out.println("MasterOutputURI: "  + reconString);
			return reconString;
	    }
	    return null;
	}

public IOPort getApplicationOutputPort(Long applicationId) {
    try {
    	Application app = (Application) get(Application.class, applicationId);
    	for (IOPort port: app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Output"))
    			return port;
    	}
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    return null;
}


public Long storeSubmission(Long ProcessingId, String Name, String Status, List<List<Long>> inputData) {
	if (checkProcessingInput(ProcessingId, inputData)) {
	Long sid = storeSubmission1(ProcessingId, Name, Status);
	storeSubmissionStatus(sid, Status, new Date());
	
	if (inputData!=null)
		storeSubmissionInput(sid, inputData);

	System.out.println("Submission: "  + sid + ": " + Name + "-" + Status + " Process:" + ProcessingId);
	return sid;
	}
	return null;

}

private Long storeSubmission1(Long ProcessingId, String Name, String Status) {
	Submission submission  = new Submission();
	submission.setName(Name);
	submission.setProcessing(getProcessing(ProcessingId));
	submission.setResults(false);
	submission.setStatus(Status);
	persist(submission);
	return submission.getDbId();

}

/*    private void setSubmissionStatus(Long SubmissionID, String status) {
    Submission submission = (Submission) get(Submission.class, new Long(SubmissionID));
         
    if (submission!=null && status!=null) {
       submission.setStatus(status);
       update(submission);
    }
 }
*/
public boolean hasSubmissionResults(Long SubmissionID) {
    Submission submission = (Submission) get(Submission.class, new Long(SubmissionID));
    return submission.getResults();
 }

public void setSubmissionResults(Long SubmissionID, boolean results) {
    Submission submission = (Submission) get(Submission.class, new Long(SubmissionID));
         
    if (submission!=null) {
       submission.setResults(results);
       update(submission);
    }
 }

public Long storeSubmissionStatus(Long SubmissionID, String status, Date timestamp) {
	Submission submission = (Submission) get(Submission.class, new Long(SubmissionID));
    
    if (submission!=null && status!=null) {
        Status pstatus = new Status();
        pstatus.setValue(status);
        if (timestamp==null)
        	pstatus.setTimestamp((new Date()));
        else
        	pstatus.setTimestamp(timestamp);
        submission.getStatuses().add(pstatus);
        pstatus.setSubmission(submission);
        persist(pstatus);
        submission.setStatus(status);
		update(submission);
		System.out.println("Status: "  + pstatus.getDbId() + ": " + status + "-" + pstatus.getTimestamp() + " Submission:" + SubmissionID);
    	return pstatus.getDbId();
    }
	return null;
}

public Long updateSubmissionName(Long SubmissionID, String SubmissionName) {
	Submission submission = (Submission) get(Submission.class, new Long(SubmissionID));
    
    if (submission!=null && SubmissionName!=null) {
        submission.setName(SubmissionName);
		update(submission);
    	return submission.getDbId();
    }
	return null;
}

public Collection<Processing> getProcessingsInProgress() {
	Collection<Processing> processings = executeQuery("from Processing where ProcessingStatus like '%In Progress%'");
   	return processings;
}
    
public Collection<Submission> getSubmissionsInProgress() {
	Collection<Submission> submissions = executeQuery("from Submission where Status like '%In Progress%'");
   	return submissions;
}
    

public Collection<Status> getSubmissionStatuses(Long submissionId) {
	Submission submission = (Submission) get(Submission.class, submissionId);
	return submission.getStatuses();
}

public Long storeIOPort(int portNumber, String portName, String displayName, String ioType, String dataType, String dataFromat, Long applicationId, String resourceName, boolean visible ) {
	//Resource resource = (Resource) get(Resource.class, resourceId);
	Resource resource = getResourceByName(resourceName);
	Application application = (Application) get(Application.class, applicationId);

	IOPort port = new IOPort();
	port.setPortNumber(portNumber);
	port.setPortName(portName);
	port.setDisplayName(displayName);
	port.setIOType(ioType);
	port.setDataType(dataType);
	port.setDataFormat(dataFromat);
	port.setResource(resource);
	port.setApplication(application);
	port.setVisible(visible);
	persist(port);

    return port.getDbId();
}

public Long storeApplication(String AppName, String AppDescription, String Executable, int type) {
	Application app = new Application();
	//app.setName(AppName.replaceAll(".", "_").replaceAll(" ", "_"));
	app.setName(AppName.replaceAll("\\.", "_").replaceAll(" ", "_"));
	app.setDescription(AppDescription);
	app.setExecutable(Executable);
	app.setType(type);
	persist(app);
    return app.getDbId();
}

public Long storeUser(String LiferayId, String FirstName, String LastName, String email) {
	User user = new User();
	user.setLiferayID(LiferayId);
	user.setFirstName(FirstName);
	user.setLastName(LastName);
	user.setEmail(email);
	persist(user);
    return user.getDbId();
}
/**
 * This method updates the user password on a given resource
 * @param userId User Identifier for which to update/set the password
 * @param userLogin userLogin User login on the resource
 * @param userPass User password for the resource
 * @param resourceId: resource identifier
 * @return an object of type UserAuthentication
 */
public UserAuthentication setUserPassword(Long userId, String userLogin, String userPass, Long resourceId) {
	
	if(getUser(userId)==null) {
		System.out.println("No such a user: " + userId);
		return null;
	}
	if(getResource(resourceId)==null){
		System.out.println("No such a resource: " + resourceId);
		return null;
	}
	//System.out.println(getUser(userId).getDbId());
	//System.out.println(getResource(resourceId).getDbId());
	//System.out.println(getUser(userId).getDbId() + "/" +  getResource(resourceId).getDbId() + "/" + userPass);
		userAuth = getAuthentication(userId, resourceId);
		if (userAuth==null) {
		    userAuth = new UserAuthentication();
		    userAuth.setUser(getUser(userId));
		    userAuth.setResource(getResource(resourceId));
		    userAuth.setUserLogin(userLogin);
		    userAuth.setAuthentication(encryptString(userPass));
			persist(userAuth);
			return userAuth;			
		}
		else {
		//if (userAuth.getUserLogin().equalsIgnoreCase(userLogin)) {
			userAuth.setUserLogin(userLogin);
			userAuth.setAuthentication(encryptString(userPass));
	    	update(userAuth);
	    	return userAuth;
		}	
   }


    public Submission getSubmission(Long submissionId) {
        return (Submission) get(Submission.class, submissionId);
    }

    public Submission getSubmissionByName(String submissionName) {
        List<Submission> results = null;
        try {
            results = session.createQuery("from Submission where Name ='" + submissionName + "'").list();
            //List<User> results = query.list();
            for (Submission u : results) {
            	return u;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return null;
    }


private Long[] getInPorts(Long processingId) {
	List<Long> ports = new ArrayList<Long>();
    try {
    	Processing proc = (Processing) get(Processing.class, processingId);
    	
    	Application app = proc.getApplication();
    	for (IOPort port: app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Input") && port.isVisible())
    			ports.add(port.getDbId());
    	}
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    // need to sort the list
	//System.out.println("\n number of ports: " + ports.size());
    Long[] array = ports.toArray(new Long[ports.size()]);
    Arrays.sort(array);
	//System.out.println("\n number of ports: " + array.length);
    return array;
}

public Collection<IOPort> getProcessingInputPorts(Processing proc) {
	Application app = proc.getApplication();
return getApplicationInputPorts(app.getDbId());
}

public Collection<IOPort> getProcessingOutputPorts(Processing proc) {
	Application app = proc.getApplication();
return getApplicationOutputPorts(app.getDbId());
}

public Collection<IOPort> getApplicationInputPorts(Long applicationId) {
	List<IOPort> inputPorts = new ArrayList<IOPort>();
    try {
    	Application app = (Application) get(Application.class, applicationId);
    	for (IOPort port: app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Input"))
    			inputPorts.add(port);
    	}
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    Collections.sort(inputPorts, new portComparator()); // use the comparator as much as u want
    return inputPorts;
}

public Collection<IOPort> getApplicationOutputPorts(Long applicationId) {
	List<IOPort> outputPorts = new ArrayList<IOPort>();
    try {
    	Application app = (Application) get(Application.class, applicationId);
    	for (IOPort port: app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Output"))
    			outputPorts.add(port);
    	}
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    Collections.sort(outputPorts, new portComparator()); // use the comparator as much as u want
    return outputPorts;
}

public int getSubmissionType(Long applicationId) {
	//List<IOPort> inputPorts = new ArrayList<IOPort>();
	int type = -1;
    try {
    	Application app = (Application) get(Application.class, applicationId);
    	for (IOPort port: app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Input")) {
    			if (port.getDataType().equalsIgnoreCase("File"))
    				type = oneFile;
    			else 
    					type = manyFiles;
    		}
    	}
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    return type;
}

/**
 * Stores the URI for a DataElement replica on a given resource
 * @param dataElementID: Data Element Identifier
 * @param resourceId: resource Identifier
 * @param ReplicaURI: Replica URI
 * @return Data Element Identifier
 */
public Long storeReplica(Long dataElementID, Long resourceId, String ReplicaURI) {
	DataElement element = (DataElement) get(DataElement.class, new Long(dataElementID));
	Resource endPoint = (Resource) get(Resource.class, new Long(resourceId));
    
    if (element!=null && ReplicaURI!=null) {
        Replica replica = new Replica();
        replica.setReplicaURI(ReplicaURI);
        replica.setDataElement(element);
        replica.setResource(endPoint);
        element.getReplicas().add(replica);
        endPoint.getReplicas().add(replica);
        persist(replica);
		update(element);
		//System.out.println("DataElement: "  + element.getDbId() + "\tReplica: " + replica.getReplicaURI());
    	return replica.getDbId();
    }
	return null;
}
/**
 * Retrieves a resource by its name, 
 * if resource doesn't exists, it creates a new one
 * @param resourceName: Resource Name
 * @return an object of type Resource
 */
private Resource getResourceByName(String resourceName) {
	    Collection<Resource> resources = executeQuery("from Resource where Name ='" + resourceName + "'");
	    for (Resource res : resources) {
	    	return res;
	    }
	    Resource resource = new Resource();
	    resource.setName(resourceName);
	    resource.setDescription(resourceName);
	    persist(resource);

   	return resource;

}
/**
 * Retrieves a resource by Identifier, 
 * @param resourceId: Resource Identifier
 * @return an object of type Resource or null
 */
public Resource getResource(Long resourceId) {
    try {
        return (Resource) session.get(Resource.class, resourceId);
    } catch (Exception e) {
        logger.error(e.getMessage());
    } 
    return null;
}
    


/**
 * Retrieves the Replicas of a given data element
 * @param dataElementId: Data element Identifier
 * @return List of objects of type Replica
 */
public Collection<Replica> getReplicas(Long dataElementId) {
	DataElement element = (DataElement) get(DataElement.class, new Long(dataElementId));
    return element.getReplicas();
}

/**
 * Deletes a Replica by ID
 * @param replicaId: Replica Identifier
 * @return number of deleted replicas
 */
public int deleteReplica(Long replicaId) {
    int res=0;
    Transaction tx = null;
    try {
        tx = session.beginTransaction();
		Query query = session.createQuery("delete from Replica where dbId = :replicaId");
		query.setParameter("replicaId", replicaId);
		System.out.println("\tremed items: " + query.executeUpdate());    		
		res = query.executeUpdate();
        tx.commit();
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        logger.error(e.getMessage());
    } finally {
        //session.close();
    }
    return res;
}

/**
 * Deletes Replicas with the given data element and URI
 * @param DataId: Data Element Identifier
 * @param ReplicaURI: Replica URI
 * @return number of deleted replicas
 */
public int deleteReplica(Long DataId, String ReplicaURI) {
    Transaction tx = null;
    int res=0;
    try {
        tx = session.beginTransaction();
		//Query query = session.createQuery("delete from Replica where ReplicaURI= '" + ReplicaURI + "' and DataID = " + DataID);
		Query query = session.createQuery("delete from Replica where ReplicaURI= :ReplicaURI and DataID = :DataID");
		query.setParameter("ReplicaURI", ReplicaURI);
		query.setParameter("DataID", DataId);
		System.out.println("\tremoved items: " + query.executeUpdate());    		
		res = query.executeUpdate();
        tx.commit();
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        logger.error(e.getMessage());
    } finally {
        //session.close();
    }
    return res;
}

/**
 * Deletes ALL Replicas of a given data element
 * @param DataId: Data Element Identifier
 * @return number of deleted replicas
 */
public int deleteAllReplicas(Long DataID) {
    Transaction tx = null;
    int res=0;
    try {
        tx = session.beginTransaction();
		//Query query = session.createQuery("delete from Replica where ReplicaURI= '" + ReplicaURI + "' and DataID = " + DataID);
		Query query = session.createQuery("delete from Replica where DataID = :DataID");
		query.setParameter("DataID", DataID);
		//System.out.println("\tremoved items: " + query.executeUpdate());  
		res = query.executeUpdate();
        tx.commit();
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        logger.error(e.getMessage());
    } finally {
        //session.close();
    }
    return res;
}

private String createReconstruction(String reconString, Long resourceId, Long userId) throws Exception {
     //init();
   	 Resource resource = (Resource) get(Resource.class, resourceId);
   	 //User user = (User) get(User.class, userId);
   	 UserAuthentication userAuth = getAuthentication(userId, resourceId);
   	 if (resource==null)
   		 return "no EndPoint resources are found ...";
   	 if (userAuth==null)
   		 return "no credentials for user " + userId + " on resource " + resourceId + " !";
	 //shutdown();
	 String XnatHost = reconString.substring(0, reconString.indexOf("/data/archive"));
	 reconString = reconString.substring(reconString.indexOf("/data/archive"));
	 System.out.println("xnat-creating reconstruction: " + XnatHost + reconString);
	 //System.out.println(XnatHost + reconString);
	XNATRestClient arcGet = new XNATRestClient();
    String arg[]={"-host", XnatHost, "-u", userAuth.getUserLogin(), "-p", decryptString(userAuth.getAuthentication()), "-m", "PUT", "-remote", reconString};
    String xmlResult = arcGet.perform(arg);
    
    if (xmlResult.length()>10)
    	System.out.println("ERROR@" + resource.getName() + ": " + xmlResult.substring(xmlResult.indexOf("<h3>")+4, xmlResult.indexOf("</h3>")) + "\n\t" + reconString);
  if (xmlResult.contains("Login attempt failed. Please try again.")) {
	    return "nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!";
  	
  }
  
  return null;
  

}
/**
 * Retrieves the User's credentials on a given resource
 * @param userId: User Identifier
 * @param resourceId: Resource Identifier
 * @return User's credentials
 * @throws RuntimeException
 */
public Map<String,Blob> getCredentials(Long userId, Long resourceId) throws RuntimeException {
     //init();
  	 UserAuthentication userAuth = getAuthentication(userId, resourceId);
   	 //User user = (User) get(User.class, userId);
   	 if (userAuth==null)
   		throw new RuntimeException("no credentials for user " + userId + " on resource " + resourceId + " !");
   	 
   	 Map<String,Blob> mp=new HashMap<String, Blob>();
   	 mp.put(userAuth.getUserLogin(), userAuth.getAuthentication());
   	 
   	 return mp;
 

}
/**
 * Retrieves the Master URI of a given data element
 * @param dataId: Dat aElement Identifier
 * @return String URI
 * @throws RuntimeException
 */
public String getMasterURI(Long dataId) throws RuntimeException {
   	 DataElement data = (DataElement) get(DataElement.class, dataId);
   	 if (data==null)
   		throw new RuntimeException("no such a file! ...");
   	 
   	 return data.getURI();
}
/**
 * Retrieves the replica for a data element on a given resource
 * @param dataId: Data element Identifier
 * @param resourceId: Resource Identifier
 * @return String URI (replica)
 * @throws RuntimeException
 */
public String getReplicaURI(Long dataId, Long resourceId) throws RuntimeException {
   	 DataElement data = (DataElement) get(DataElement.class, dataId);
   	 for (Replica r : data.getReplicas()) {
   		 if (r.getResource().getDbId()==resourceId)
   			 return r.getReplicaURI();
   	 } 	 
   	 return null;
}

/**
 * Stores a Submission error in the catalogue
 * @param SubmissionId: Submission Identifier
 * @param Code: Error Code
 * @param Message: Error Message
 * @param Description: Error Description
 * @return Error identifier from the catalogue
 */
    public Long storeError(Long SubmissionId, int Code, String Message, String Description) {
    	Error error  = new Error();
    	error.setCode(Code);
    	error.setMessage(Message);
    	error.setSubmission(getSubmission(SubmissionId));
    	if (Description!=null && Description.length()>1024)
    		Description = Description.substring(0, 1000) + "\n...\n" + Description.length();
    	error.setDescription(Description);
    	getSubmission(SubmissionId).getErrors().add(error);
    	persist(error);
    	return error.getDbId();

    }
    /**
     * Retrieves the NSG Administrator e-mail
     * @return NSG Administrator e-mail if any
     */
    @SuppressWarnings("unchecked")
	public String getAdminEmail() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Admin Email'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    /**
     * Retrieves the NSG e-mail Host
     * @return NSG e-mail Host if any
     */
    @SuppressWarnings("unchecked")
	public String getEmailHost() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Email Host'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    /**
     * Retrieves the gUSE output path
     * @return gUSE output path if any
     */
    @SuppressWarnings("unchecked")
	public String getGuseOutputPath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='gUSE output path'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    
    /**
     * Retrieves the gUSE Admin Panel path
     * @return gUSE Admin Panel path if any
     */
    @SuppressWarnings("unchecked")
	public String getAdminPanel() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Admin Panel'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    
    /**
     * Retrieves the Grid Storage path
     * @return Grid Storage path if any
     */
    @SuppressWarnings("unchecked")
	public String getGridStoragePath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Grid Storage Path'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    
    /**
     * Retrieves the Cloud Storage path
     * @return Cloud Storage path if any
     */
    @SuppressWarnings("unchecked")
	public String getCloudStoragePath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Cloud Storage Path'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    
    /**
     * Retrieves the Cluster Storage path
     * @return Cluster Storage path if any
     */
    @SuppressWarnings("unchecked")
	public String getClusterStoragePath() {

       Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='Cluster Storage Path'");
       if (prefs!=null && prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }
    
    /**
     * Retrieves the NSG Global Preferences
     * @param preference: Preference String
     * @return Preference value if any
     */
    @SuppressWarnings("unchecked")
	public String getGlobalPreference(String preference) {

       Collection<Preference> prefs = executeQuery("from Preference where PrefKey='Global Pref' and PrefDesc ='" + preference + "'");
       if (prefs==null || prefs.size()<1)
    	   return null;
       for (Preference pref : prefs) {
       		return pref.getValue();
       }
   	   
      return null;
   }

    
    // FROM here goes XNAT plugin methods
    /**
     * Initializes a Resource
     * @param resourceId: Resource Identifier
     * @return an object of type Resource
     * @throws Exception
     */
	public Resource initResource(Long resourceId) throws Exception {

	   	 Resource resource = (Resource) get(Resource.class, resourceId);
	   	 if (resource==null)
	   		 return null;
	   	 String xnatStr = setXnatProperties(resource.getName());
	   	 if (xnatStr!=null) 
	 		return null; //xnatStr; 
	   	 return resource;
	}
/**
 * Creates a Reconstruction on XNAT	
 * @param project: Project Name
 * @param processing: Processing Name
 * @param reconstruction: Reconstruction String
 * @param resourceId: Resource Identifier
 * @param userId: User Identifier
 * @return Null if reconstruction is created and an error string otherwise
 * @throws Exception
 */
	public String putReconstruction(String project, String processing, String reconstruction, Long resourceId, Long userId) throws Exception {
		Resource resource = (Resource) get(Resource.class, resourceId);
	   	 if (resource==null)
	   		 return "no EndPoint resources are found ...";
		 //user = getUser(userId);
	  	 userAuth = getAuthentication(userId, resourceId);
		 
	   	 String processingURI = XnatHost + "/data/archive/projects/" + project + "/subjects/013/experiments/" + processing + "/reconstructions/" + reconstruction;
		 System.out.println("URI1:" + processingURI);
		 processingURI = "/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00002/reconstructions/Recon006?type=NIFTI&baseScanType=DTI";
		 //processingURI = "/data/archive/projects/test/subjects/014/experiments/xnatZ0_E00002/reconstructions/Recon001/out/files/XNATRestClient.exe?format=mgz&description=reconstructedImage' -local XNATRestClient.exe";
		 System.out.println("URI2:" + XnatHost + processingURI + "\n" + userAuth.getUserLogin() + decryptString(userAuth.getAuthentication()));
		XNATRestClient arcGet = new XNATRestClient();
		// Retrieve projects from Xnat for active user
	    String arg[]={"-host", XnatHost, "-user_session", userAuth.getSession(), "-m", "PUT", "-remote", processingURI};
	    String xmlResult = arcGet.perform(arg);
       if (xmlResult.contains("Login attempt failed. Please try again.")) {
   	    return "nsgdm-api: Login to XNAT failed for user '" + userAuth.getUserLogin() + "', wrong username or password!";
       	
       }
       
       return null;
       

	}
/*
 	private String putProcessing(String project, String processing, Long resourceId) throws Exception {
		resource = (Resource) get(Resource.class, resourceId);
	   	 if (resource==null)
	   		 return "no EndPoint resources are found ...";
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
 * This method perform a complete check about a given user. It checks whether a user exists or not in the catalog.
 * If the user exists in the catalog, it checks whether the user has the proper credentials or not to access the data.	
 * @param LiferayID of Type String
 * @return an object of type User
 * @throws RuntimeException of NO_USER, NO_PASSWORD, or WRONG_PASSWORD
 */

   public User checkUser(String LiferayID) throws RuntimeException 
   {
	   Long resourceId = 1L;
	   User user = getUser(LiferayID);
	     //System.out.println(user.getLastName());
	     if (user==null) {
	    	 logger.error("User '" + LiferayID + "' Doesn't exist in the NSG catalogue");
	    	 throw new RuntimeException(NO_USER);
	     }
	     
	  	 userAuth = getAuthentication(user.getDbId(), resourceId);
	     if (userAuth==null){
	    	 logger.error("No Credentials are set for user'" + LiferayID + "' on resource " + resourceId);	
	    	 throw new RuntimeException(NO_PASSWORD);	
	     }    	 
	  	 
	     if (userAuth.getAuthentication()==null){
	    	 logger.error("Password is not set for user'" + LiferayID + "' on resource " + resourceId);	
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
   	   	update(user);
   	   	return user;
      }
      return null;

   }
   
   /**
    * This method retrieves the metadata of a given data set. Metadata is filtered based on the user preferences
    * If the metadata preferences for the user are not defined. The user will get the default set of metadata.
    * @param userID
    * @param projectID
    * @param dataID
    * @param resourceId: Identifier of the resource where the data resides
    * @return List of key-value properties (returns null in case of an exception)
    * @throws Exception: null if not able to process the metadata
    */
   public List<Property> getXnatMetadata(Long userID, Long projectID, Long dataID, Long resourceId) throws Exception
   {
       //user = getUser(userID);
	  	 userAuth = getAuthentication(user.getDbId(), resourceId);
/*    	DataElement element2 = getData(dataID);
       if (1==1)
       	return (List<Property>) element2.getProperties();
*/
       String metadataPref = getPreference("metadata");
       if (metadataPref==null) {
       	System.out.println("WARNING: metadata preferences are not defined for user: " + userAuth.getUserLogin());
       	return null;
       }

   	DataElement data = getData(dataID);
   	initResource(data.getResource().getDbId());
   	String dataURI = data.getURI();
       //System.out.println("Processing: " + getExperiment(dataURI));

   	String metaDataURI = XnatMetadata + "/projects/" + getProjectName(projectID) + "/experiments/" + getExperiment(dataURI) + "/scans/" + data.getScanID() + "&format=xml";
   	//String metaDataURI = XnatMetadata + "/projects/" + getProjectName(projectID) + "/experiments/" + processing.getDescription() + "/scans/" + data.getScanID() + "&format=xml";
   	String arg[]={"-host",XnatHost, "-user_session", userAuth.getSession(), "-m", "GET", "-remote", metaDataURI};
   	XNATRestClient xnat = new XNATRestClient();
       String xml = xnat.perform(arg);
  	    
//       if (xml.contains("<h3>Experiment or project not found</h3>"))
//       	return null;
       
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
   
  
   private String setXnatProperties(String xnatEndPoint) {
   	
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
 /**
 * This method encrypts encrypts a password string    
 * @param str of type string
 * @return encrypted string str as Blob
 */
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
/**
 * This method decrypts a password string       
 * @param strb of type blob
 * @return decrypted password strb as String
 */
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
 * Retrieves xnat project name
 * @param ProjectID of type long
 * @return project name as string
 */
   
private String getProjectName(Long ProjectID) {
	    Project project = (Project)  get(Project.class, ProjectID);
	    if (project==null) {
	    	throw new RuntimeException("Project '" + ProjectID + "' Doesn't exist in the NSG catalogue");
	    }
	    return project.getXnatID();
}
/**
 * This method returns an object of type DataElement for the given dataID
 * @param dataID of type long
 * @return an object of type DataElement
 */
private DataElement getData(Long dataID) {
	    DataElement data = (DataElement)  get(DataElement.class, dataID);
	    if (data==null) {
	    	throw new RuntimeException("DataElement '" + dataID + "' Doesn't exist in the NSG catalogue");
	    }
	    return data;
}

private String getPreference(String metadata) {
	 String userPref = "null";

	 for (Preference pref : user.getPreferences()) {
		 if (pref.getKey().equalsIgnoreCase(metadata)) {
			   return pref.getValue();
		 }
	 }
   Collection<Preference> prefs = executeQuery("from Preference where PrefDesc ='default metadata'");
   
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
/**
 * Return the User's authentication for a given data source
 * @param userId: database Id of the user to authenticate
 * @param resourceId: resource for which to authenticate (the user)
 * @return encrypted user authentication
 * @throws RuntimeException
 */
public UserAuthentication getAuthentication(Long userId, Long resourceId) throws RuntimeException {
	Collection<UserAuthentication> userAuths = executeQuery("from UserAuthentication where UserKey =" + userId + " and ResourceID=" + resourceId);
	   
 	 if (userAuths.size()<1)
 		return null;
 	 
	 for (UserAuthentication userAuth : userAuths) {
		 return userAuth;
	 }
  	 
  	 return null;
}

public String getDataHistory(Long dataId) {
    String body = "";
    String txtColor = "#000000";
    String txtColorA = "#0099ff"; // text color Activity
    String txtColorE = "#0099ff"; // text color Entity
    int x = 13, y = 53; //x,y left-up coordinates
    int h = 120; //w: shape width, : shape hight
    int i = 0;
    String dynamicStr = ""; //String to store data computed dynamically
	int nlinks = 0; //number of links
    Long subId = 1L;
	int dist = 0;
   
	body = body + "\tend   = drawEllipse(ctx, " + (x-3) + ", " + (y-3) + ", w+6, h/2+6,'#ffffcc');\n";
    while (dataId!=null && subId!=null) {
    	//System.out.print("data_"+ dataId + "<-");
    	DataElement data =  (DataElement)  get(DataElement.class, dataId);
    	if (data==null)
    		return "No such data: " + dataId;
	
    	body = body + "\tend   = drawEllipse(ctx, " + x + ", " + y + ", w, h/2,'#ffffcc');\n";
		body = body + "\t\tdrawText(ctx, " + (x+20) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + data.getName() + "');\n";
		dynamicStr += "\txs[" + nlinks + "] = " + (x+20) + "; \n";
		dynamicStr += "\tys[" + nlinks + "] = " + (y+5+h/4) + "; \n";
		dynamicStr += "\tlinks[" + nlinks + "] = \"" + data.getURI() + "\"; \n";
		dynamicStr += "\ttxts[" + nlinks + "] = \"" + data.getName() + "\"; \n";
		nlinks++;

		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Subject ID: " + data.getSubject() + "');\n";
		dist +=12;
		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Scan ID:     " + data.getScanID() + "');\n";
		dist +=24;
		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Type:   " + data.getType() + "');\n";
		dist +=12;
		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Format: " + data.getFormat() + "');\n";
		dist +=12;
		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Creation Date: " + data.getDate() + "');\n";
		dist = 0;
        //body = body + "\t\tdrawText3(ctx, " + x + ", " + y + ",'" + txtColorA + "', 'Data ID: " + data.getDbId() + "');\n";
		
		y = y + h;
	
		Collection<SubmissionIO> submissionIOs = executeQuery("from SubmissionIO where Type='Output' and DataID=" + dataId);
	
		if (submissionIOs.size()>0) {// && subId!=null) {
			Iterator<SubmissionIO> iter = submissionIOs.iterator();
			SubmissionIO submissionIO = (SubmissionIO) iter.next();
			
			subId = submissionIO.getSubmission().getDbId();
		    //System.out.println("SubmissionIO_"+ subId + "<-");
		    Submission submission = submissionIO.getSubmission();
		    //System.out.print("subm_"+ subId + "<-");
		    if (submission!=null && dataId!=null) {
			    //System.out.println("Submission_"+ submission.getDbId() + "<-");
		    	String color =getColor(submission.getStatus());

		        body = body + "\tstart   = rectangle(ctx, " + x + ", " + y + ", '"+ color + "');\n";
		        body = body + "\t\tdrawText(ctx, " + (x+20) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + submission.getProcessing().getApplication().getName() + "');\n";
		        dynamicStr += "\txs[" + nlinks + "] = " + (x+20) + "; \n";
		        dynamicStr += "\tys[" + nlinks + "] = " + (y+5+h/4) + "; \n";
		        dynamicStr += "\ttxts[" + nlinks + "] = " + submission.getName() + "; \n";
		        nlinks++;

		        body = body + "\t\tdrawText3(ctx, " + (x-30) + ", " + y + ",'" + txtColorA + "', 'Version: " + submission.getProcessing().getApplication().getVersion() + "');\n";

		        //body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Status: " + submission.getStatus() + "');\n";
		        //dist +=12;
		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Submission Date: " + submission.getProcessing().getDate() + "');\n";
		        dist +=12;
		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Submitter: " + submission.getProcessing().getUsers().iterator().next().getFirstName() +
		        		" " + submission.getProcessing().getUsers().iterator().next().getLastName() + "');\n";
		        dist +=24;
		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Start Date: " + submission.getProcessing().getDate() + "');\n";
		        dist +=12;
		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'End  Date:  " + getTime(submission.getDbId(), "Done") + "');\n";
		        dist +=12;
		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Description: " + submission.getProcessing().getDescription() + "');\n";
		        dist = 0;
		        

		        body = body + "\tctx.lineWidth = 1;\n";
		    	body = body + "\tctx.fillStyle = ctx.strokeStyle = '#990000';\n";
		    	body = body + "\tarrow(ctx,start,end,5);\n";
		    	
		    	if (i>0)
			    	body = body + "\tarrow2(ctx,end,5);\n";

		        y = y + h;
		        i++;
		    }
		    dataId= null;
		    Collection<SubmissionIO> subInputs = executeQuery("from SubmissionIO where Type='Input' and SubmissionID=" + subId);
		    for (SubmissionIO subIn: subInputs) {
		    	data = subIn.getDataElement();
		    	Collection<SubmissionIO> subOutputs = executeQuery("from SubmissionIO where Type='Output' and DataID=" + subIn.getDataElement().getDbId());
			    if (subOutputs.size()>0) {
			    	
			    	dataId = subOutputs.iterator().next().getDataElement().getDbId();//Long.valueOf(rs3.getString("DataID"));
				    //System.out.print("data_"+ dataId + "<-");
			    }
			    else {
			        body = body + "\tend   = drawEllipse(ctx, " + x + ", " + y + ", w, h/2,'#ffffcc');\n";
			        body = body + "\t\tdrawText(ctx, " + (x+20) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + data.getName() + "');\n";
					dynamicStr += "\txs[" + nlinks + "] = " + (x+20) + "; \n";
					dynamicStr += "\tys[" + nlinks + "] = " + (y+5+h/4) + "; \n";
					dynamicStr += "\tlinks[" + nlinks + "] = \"" + data.getURI() + "\"; \n";
					dynamicStr += "\ttxts[" + nlinks + "] = \"" + data.getName() + "\"; \n";
					nlinks++;

			        body = body + "\tarrow2(ctx,end,5);\n";
	
					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Subject ID: " + data.getSubject() + "');\n";
					dist +=12;
					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Scan ID:     " + data.getScanID() + "');\n";
					dist +=24;
					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Type:   " + data.getType() + "');\n";
					dist +=12;
					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Format: " + data.getFormat() + "');\n";
					dist +=12;
					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Creation Date: " + data.getDate() + "');\n";
					dist = 0;
			        //body = body + "\t\tdrawText3(ctx, " + x + ", " + y + ",'" + txtColorA + "', 'Data ID: " + data.getDbId() + "');\n";

			        y = y + h;
			    }
		    }
	    }	    
		else {
		    dynamicStr = "var ch = " + y + ";\n" +
	                 "\tvar ins = [" + nlinks + "]; \n" + 
	                 "\tvar links = [" + nlinks + "]; \n" + 
	                 "\tvar ys = [" + nlinks + "]; \n" + 
	                 "\tvar xs = [" + nlinks + "]; \n" + 
	                 "\tvar nlinks = " + nlinks + "; \n" + dynamicStr;
			return (getHeader().replaceAll("#othersVariables#", dynamicStr) + body + "\n}\n" + getFooter());
		}
	}
    dynamicStr = "var ch = " + y + ";\n" +
                 "\tvar ins = [" + nlinks + "]; \n" + 
                 "\tvar links = [" + nlinks + "]; \n" + 
                 "\tvar ys = [" + nlinks + "]; \n" + 
                 "\tvar xs = [" + nlinks + "]; \n" + 
                 "\tvar nlinks = " + nlinks + "; \n" + dynamicStr;

    return (getHeader().replaceAll("#othersVariables#", dynamicStr) + body + "\n}\n" + getFooter());
}

public String getApplicationGraph(Long AppId) {
    String body = "", inputs ="", outputs = "";
    String txtColor = "#000000";
    String txtColorA = "#0099ff"; // text color Activity
    String txtColorE = "#0099ff"; // text color Entity
    int x = 13, y = 13; //x,y left-up coordinates
    int h = 100; //w: shape width, : shape hight
	int dist = 0;
   
    	Application app =  (Application)  get(Application.class, AppId);
    	if (app==null)
    		return "No such application: " + AppId;
    	
    	/*

    	body += "ctx.font = '16pt Arial';\n"
    		 + "ctx.fillStyle = 'cornflowerblue';\n"
    		 + "ctx.strokeStyle = 'blue';\n"
    		 + "ctx.fillText('Data Flow', 5, 15);\n"
    		 + "ctx.strokeText('Data Flow', 5, 15 );\n";
		*/
		y = y + h;
		
        inputs += "\tctx.lineWidth = 1;\n";
        inputs += "\tctx.fillStyle = ctx.strokeStyle = '#990000';\n";

		
		body += "\tmid   = rectangle(ctx, " + x + ", " + y + ", '#efefef');\n";
        body += "\t\tdrawText(ctx, " + (x+10) + ", " + (y+h/4) + ",'" + txtColor + "', '" + app.getName() + "');\n";
        body += "\t\tdrawText3(ctx, " + (x) + ", " + (y+h/4+12) + ",'" + txtColorA + "', '" + app.getDescription() + "');\n";
        dist = 12;
        y = y - h;

        for (IOPort port:app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Input") && port.isVisible()) {
    	    	inputs += "\tstart   = drawEllipse(ctx, " + x + ", " + y + ", w, h/2,'#ffffcc');\n"
    			    + "\t\tdrawText(ctx, " + (x+10) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + port.getDisplayName() + "');\n";
    	    	//inputs += "\t\tdrawText2(ctx, " + x + ", " + (y+h/2+dist) + ",'" + txtColorE + "', 'format: " + port.getDataFormat().replaceAll("#", "") + "');\n";
    			//dist +=12;
    			inputs += "\t\tdrawText2(ctx, " + x + ", " + (y+h/2+dist) + ",'" + txtColorE + "', 'type:     " + port.getDataType() + "');\n";
    	        inputs += "\tarrow(ctx,start,mid,5);\n";

    			dist = 12;
    			x += 20;
    		}
    	}
        
        x=13;
        
        y = y + 2*h;
 
        for (IOPort port:app.getIOPorts()) {
    		if (port.getIOType().equalsIgnoreCase("Output") && port.isVisible()) {
    	    	inputs += "\tend   = drawEllipse(ctx, " + x + ", " + y + ", w, h/2,'#ffffcc');\n"
    			    + "\t\tdrawText(ctx, " + (x+10) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + port.getDisplayName() + "');\n"
    				+ "\t\tdrawText2(ctx, " + x + ", " + (y-h/2+dist) + ",'" + txtColorE + "', 'format: " + port.getDataFormat() + "');\n";
    			dist +=12;
    			inputs += "\t\tdrawText2(ctx, " + x + ", " + (y-h/2+dist) + ",'" + txtColorE + "', 'type:     " + port.getDataType() + "');\n";
    			dist +=12;
    			//inputs += "\t\tdrawText2(ctx, " + x + ", " + (y-h/2+dist) + ",'" + txtColorE + "', 'output:   " + port.getOutputApps().replaceAll("#", "") + "');\n";
    	        inputs += "\tarrow(ctx,mid,end,5);\n";
    			dist = 0;    			
    			//x = x + 400;
		    	
    		}
    	}

   	
        return (getHeader2() + body + inputs + getFooter3());
        //return ( body + inputs + "\n}\n");
}

/**
 * Retrieves the Cloud Storage path
 * @return Cloud Storage path if any
 */
@SuppressWarnings("unchecked")
private Date getTime(Long sId, String status) {

   Collection<Status> statuses = executeQuery("from Status where Value ='" + status + "' and SubmissionID='" + sId + "'");
   if (statuses!=null && statuses.size()<1)
	   return null;
   for (Status sts : statuses) {
   		return sts.getTimestamp();
   }
	   
  return null;
}


private static String getHeader() {
	return "\n<html> \n"
    + "	<head> \n"
    + "	<TITLE>Neuroscience Gateway: Data History</title> \n"
    + "	<style type=\"text/css\"> \n"
    + "	body { background:#eee; margin:2em 4em; text-align:center; } \n"
    + "	canvas { background:#fff; border:1px solid #666 } \n"
    + "	</style> \n"
    + "	\n"
    + "	<script type=\"application/javascript\"> \n"
    + "	<!-- \n"
    + "	var ctx; \n"
    + "	var canvas; \n\n"
    + "	var w = 250;\n"
    + "	var h = 120;\n"
    + "	var cw = 500;\n\n"
    + "	var cx, cy; \n"
    + "	var idx = -1; \n"
    + "	var r = 50; \n"
    + "	#othersVariables#"
    //+ "	var incircle = false; \n\n"
    + "\nfunction draw() { \n"
    + "\tcanvas = document.getElementById(\"canvas\"); \n"
    + "\tif (!canvas.getContext) { \n"
    + "\t  return; \n"
    + "\t} \n"
    + "\tctx = canvas.getContext(\"2d\"); \n"
    + "\tctx.canvas.width = cw; \n"
    + "\tctx.canvas.height = ch; \n"
    + "	\n"
    + "\tcanvas.addEventListener(\"mousemove\", on_mousemove, false); \n"
    + "\tcanvas.addEventListener(\"click\", on_click, false); \n"
    + "\tctx.save(); \n"
    + "	\n"
    + "\tfor (var i=0;i<cw*ch/10000;++i) randomCircle(ctx,'#f7f3f7');\n"
    + "ctx.font = '28pt Arial';\n"
    + "ctx.fillStyle = 'cornflowerblue';\n"
    + "ctx.strokeStyle = 'blue';\n"

	+ "var txt = 'Data History Report';\n"
	+ "//ctx.fillText(txt, (canvas.width-ctx.measureText(txt).width)/2,30);\n"
	+ "ctx.strokeText(txt, (canvas.width-ctx.measureText(txt).width)/2,30);\n"

    + "\tvar start = 0;\n"
    + "\tvar end = 0;\n";


}
private static String getHeader2() {
	return " <canvas id='canvas'> \n"
		    + "Canvas not supported \n"
		    + "</canvas> \n"

		    + "<script type=\"application/javascript\"> \n"
		    + "<!--\n"
		    + "var canvas = document.getElementById('canvas'), \n"
		    + "ctx = canvas.getContext('2d'); \n"  
		    + "	var cw = 290;\n"
		    + "	var ch = 350;\n"
		    + "\tctx.canvas.width = cw; \n"
		    + "\tctx.canvas.height = ch; \n"
		    + "	var w = 150;\n"
		    + "	var h = 100;\n"
		    + "	\n"
		    + "\tfor (var i=0;i<cw*ch/10000;++i) randomCircle(ctx,'#f7f3f7');\n\n"
		    + "\tvar start = 0;\n"
		    + "\tvar mid = 0;\n"
		    + "\tvar end = 0;\n";

}
private static String getFooter() {
	return 
    "function arrow(ctx,p1,p2,size){\n" +
    "    ctx.beginPath();\n" +
    "    ctx.fillStyle = '#990000'\n" +
    "    ctx.lineCap = 'round';\n" +
    "    ctx.moveTo(p1.x+w/2, p1.y);\n" +
    "    ctx.lineTo(p1.x+w/2,p2.y+h/2); // 50 + 10 of the ellipse\n" +
    "    ctx.stroke();\n" +

        // arrowhead

    "    ctx.moveTo(p1.x+w/2,p2.y+h/2);\n" +
    "    ctx.lineTo(p1.x+w/2-size, p2.y+h/2+size*2.5);\n" +
    "    ctx.lineTo(p1.x+w/2+size, p2.y+h/2+size*2.5);\n" +
    "    ctx.closePath();\n" +
    "    ctx.fill();\n" +

        //ctx.restore();
    "  }\n" +

     " function arrow2(ctx,p1,size){\n" +
     "   ctx.beginPath();\n" +
     "   ctx.fillStyle = '#990000'\n" +
     "   ctx.lineCap = 'round';\n" +
     "   ctx.moveTo(p1.x+w/2, p1.y-h/2);\n" +
     "   ctx.lineTo(p1.x+w/2, p1.y);\n" +
     "   ctx.stroke();\n" +

        // arrowhead

     "   ctx.moveTo(p1.x+w/2, p1.y-h/2);\n" +
     "   ctx.lineTo(p1.x+w/2-size, p1.y-h/2+size*2.5);\n" +
     "   ctx.lineTo(p1.x+w/2+size, p1.y-h/2+size*2.5);\n" +
     "   ctx.closePath();\n" +
     "   ctx.fill();\n" +

        //ctx.restore();
     " }\n" +

     " function rectangle(ctx,x, y, color){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fillRect(x,y,w,h/2);\n" +
     "   ctx.strokeRect(x,y,w,h/2);\n" +
     "   ctx.fillStyle = '#FFFFFF';\n" +
     "   ctx.font = \"24px Arial\";\n" +
     "  return {x:x,y:y};\n" +
     " }\n" +

     " function drawText(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"bold 12px Verdana\";\n" +
     "   //ctx.strokeText(txt,x+20,y+5+h/4);\n" +
     "   ctx.fillText(txt,x,y);\n" +
     "   ctx.fillStyle = '#00000';\n" +
     " }\n" +

     " function drawText2(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"13px Calibri\";\n" +
     "   ctx.fillText('- ' + txt,x+w+5,y+10);\n" +    
     " }\n" +


     " function drawText3(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"13px Calibri\";\n" +
     "   ctx.fillText('- ' + txt,x+50,y+h/2-8);\n" +    
     " }\n" +

     " function circle(ctx,x,y, color){\n" +
     "   ctx.beginPath();\n" +
     "   ctx.arc(x, y, 50, 0, 20, false);\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fill();\n" +
     "   ctx.stroke();\n" +
     "   return {x:x,y:y};\n" +
     " }\n" +

    " function circle(ctx,x,y, color){\n" +
	"   ctx.save();\n" +
	"   //ctx.translate(canvas.width/2, ctx.height/2);\n" +
	"   //ctx.scale(2, 1);\n" +
	"   ctx.beginPath();\n" +
	"   ctx.arc(x+w/2, y, 50, 0, Math.PI * 2, false);\n" +
	"   ctx.fillStyle = color;\n" +
	"   ctx.fill();\n" +
	"   ctx.stroke();\n" +
	"   return {x:x,y:y};\n" +
	" }\n" +
     " function randomCircle(ctx,color){\n" +
     "   ctx.save();\n" +
     "   ctx.beginPath();\n" +
     "   ctx.arc(\n" +
     "     Math.round(Math.random()*(ctx.canvas.width  - 100) + 50),\n" +
     "     Math.round(Math.random()*(ctx.canvas.height - 100) + 50),\n" +
     "     Math.random()*20 + 10,\n" +
     "     0, Math.PI * 2, false\n" +
     "   );\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fill();\n" +
        //ctx.lineWidth = 0;
        //ctx.stroke();
     "   ctx.restore();\n" +
     " }\n" +

 " function drawEllipse(ctx, x, y, w, h, color) {\n" +
 "   var kappa = .5522848,\n" +
 "       ox = (w / 2) * kappa, \n" +// control point offset horizontal
 "       oy = (h / 2) * kappa, \n" +// control point offset vertical
 "       xe = x + w,           \n" +// x-end
 "       ye = y + h,           \n" +// y-end
 "       xm = x + w / 2,       \n" +// x-middle
 "       ym = y + h / 2;       \n" +// y-middle

 "   ctx.beginPath();\n" +
 "   ctx.moveTo(x, ym);\n" +
 "   ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);\n" +
 "   ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);\n" +
 "   ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);\n" +
 "   ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);\n" +
 "      ctx.fillStyle = color;\n" +
 "       ctx.fill();\n" +
 "   ctx.closePath();\n" +
 "   ctx.stroke();\n" +
 "   return {x:x,y:y};\n" +
 " }\n" 

    + "   function on_click(e) { \n"
    + "\tif (idx>=0) { \n"
    + "\t   window.location = links[idx]; \n"
    + "\t} \n"
    + "   } \n"
    + "	\n"
    + "	\n"
    + "   function on_mousemove (ev) { \n"
    + "	var x, y; \n"
    + "	\n"
    + "	// Get the mouse position relative to the canvas element. \n"
    + "	if (ev.layerX || ev.layerX == 0) { // Firefox \n"
    + "\t	x = ev.layerX; \n"
    + "\t	y = ev.layerY; \n"
    + "	} \n"
    + "	else if (ev.offsetX || ev.offsetX == 0) { // Opera \n"
    + "\t	x = ev.offsetX; \n"
    + "\t	y = ev.offsetY; \n"
    + "	} \n"
    + "	\n"

    + "	//--------------------- \n"
    + "	// draw the coords: \n"
    + "	//--------------------- \n"
    + "	\n"
    + "	ctx.font = \"12px sans-serif\"; \n"
    + "	ctx.fillStyle = \"rgba(0, 128, 0, 1.0)\"; \n"
    + "	var str = x.toString() + \",\" + y.toString(); \n"
    + "	ctx.clearRect (0, 0, 60, 15); \n"
    + "	ctx.fillText(str, 0, 10); \n"
    + "	\n"
    + "	//------------------------------------ \n"
    + "	//</ncirclese point is in a circle: \n"
    + "	//------------------------------------ \n"
    + "	\n"
    + "	for(i=0; i<nlinks; i++) { \n"
    + "\t	var tw = 200; //ctx.measureText(linkText).width; \n"
    + "\t	var th = 10; //ctx.measureText(linkText).height; \n"
    //linkWidth=ctx.measureText(linkText).width;

    + "\t	if ( (x>=(xs[i]) && x<=(xs[i]+tw)) && (y>=(ys[i]-th) && y<=(ys[i])) ) { \n"
    + "\t\t	if (!ins[i]) { \n"
    + "\t\t\t	document.body.style.cursor = \"pointer\"; \n"
    + "	//ctx.beginPath(); \n"
    + "	//ctx.strokeText(txts[i],xs[i],ys[i]); \n"
    + "	//ctx.stroke(); \n"
    + "\t\t\t	ins[i] = true; \n"
    + "\t\t\t	idx = i; \n"
    + "\t\t	} \n"
    + "\t	} \n"
    + "\t	else { \n"
    + "\t\t	if (ins[i]) { \n"
    + "	//ctx.beginPath(); \n"
    + "	//ctx.fillText(txts[i],xs[i],ys[i]); \n"
    + "	//ctx.stroke(); \n"
    + "\t\t\t	ins[i] = false; \n"
    + "\t\t\t	document.body.style.cursor = \"\"; \n"
    + "\t\t	} \n"
    + "\t	} \n"
    + "	} \n"
    + "	\n"
    + "	\n"
    + "	\n"
    + "	} \n"
    + "	\n"
    + "	\n"
    + "	\n"
    + "	--> \n"
    + "	\n"
    + "	</script> \n"
    + "	</head><body onload=\"javascript:draw();\"><div id=\"wrap\"></div><canvas id=\"canvas\" width=\"1\" height=\"1\"></canvas></body></html>\n";
}
private static String getFooter3() {
	return 
    "function arrow(ctx,p1,p2,size){\n" +
    "    ctx.beginPath();\n" +
    "    ctx.fillStyle = '#990000'\n" +
    "    ctx.lineCap = 'round';\n" +
    "    ctx.moveTo(p1.x+w/2, p1.y+h/2);\n" +
    "    ctx.lineTo(p1.x+w/2,p2.y); // 50 + 10 of the ellipse\n" +
    "    ctx.stroke();\n" +

        // arrowhead

    "    ctx.moveTo(p1.x+w/2,p2.y);\n" +
    "    ctx.lineTo(p1.x+w/2-size, p2.y-size*2.5);\n" +
    "    ctx.lineTo(p1.x+w/2+size, p2.y-size*2.5);\n" +
    "    ctx.closePath();\n" +
    "    ctx.fill();\n" +

        //ctx.restore();
    "  }\n" +

     " function rectangle(ctx,x, y, color){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fillRect(x,y,w,h/2);\n" +
     "   ctx.strokeRect(x,y,w,h/2);\n" +
     "   ctx.fillStyle = '#FFFFFF';\n" +
     "   ctx.font = \"24px Arial\";\n" +
     "  return {x:x,y:y};\n" +
     " }\n" +

     " function drawText(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"12px Verdana\";\n" +
     "   var tw = (w-ctx.measureText(txt).width)/2;\n" +
     "   if (tw<x)\n" +
     "     tw = x;\n" +
     "   ctx.fillText(txt,tw,y);\n" +
     "   ctx.fillStyle = '#00000';\n" +
     " }\n" +

     " function drawText2(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"13px Calibri\";\n" +
     "   ctx.fillText('- ' + txt,x+w/2,y);\n" +    
     " }\n" +

     " function drawText3(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"13px Calibri\";\n" +
     "   var tw = (w-ctx.measureText(txt).width)/2;\n" +
     "   if (tw<x)\n" +
     "     tw = x;\n" +
     "   ctx.fillText(txt,tw,y);\n" +    
     " }\n" +


     " function randomCircle(ctx,color){\n" +
     "   ctx.save();\n" +
     "   ctx.beginPath();\n" +
     "   ctx.arc(\n" +
     "     Math.round(Math.random()*(ctx.canvas.width  - 100) + 50),\n" +
     "     Math.round(Math.random()*(ctx.canvas.height - 100) + 50),\n" +
     "     Math.random()*20 + 10,\n" +
     "     0, Math.PI * 2, false\n" +
     "   );\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fill();\n" +
     "   ctx.restore();\n" +
     " }\n" +

 " function drawEllipse(ctx, x, y, w, h, color) {\n" +
 "   var kappa = .5522848,\n" +
 "       ox = (w / 2) * kappa, \n" +// control point offset horizontal
 "       oy = (h / 2) * kappa, \n" +// control point offset vertical
 "       xe = x + w,           \n" +// x-end
 "       ye = y + h,           \n" +// y-end
 "       xm = x + w / 2,       \n" +// x-middle
 "       ym = y + h / 2;       \n" +// y-middle

 "   ctx.beginPath();\n" +
 "   ctx.moveTo(x, ym);\n" +
 "   ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);\n" +
 "   ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);\n" +
 "   ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);\n" +
 "   ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);\n" +
 "      ctx.fillStyle = color;\n" +
 "       ctx.fill();\n" +
 "   ctx.closePath();\n" +
 "   ctx.stroke();\n" +
 "   return {x:x,y:y};\n" +
 " }\n" 

    + "	\n"
    + "	--> \n"
    + "	\n"
    + "	</script> \n";
}
private static String getFooter2() {
	return 
    "function arrow(ctx,p1,p2,size){\n" +
    "    var dist = 10;\n" +
    "    ctx.beginPath();\n" +
    "    ctx.fillStyle = '#990000'\n" +
    "    ctx.lineCap = 'round';\n" +
    "    ctx.moveTo(p1.x+w/2, p1.y);\n" +
    "    ctx.lineTo(p1.x+w/2,p2.y+h/2); // 50 + 10 of the ellipse\n" +
    "    ctx.stroke();\n" +

        // arrowhead

    "    ctx.moveTo(p1.x+w/2,p2.y+h/2);\n" +
    "    ctx.lineTo(p1.x+w/2-size, p2.y+h/2+size*2.5);\n" +
    "    ctx.lineTo(p1.x+w/2+size, p2.y+h/2+size*2.5);\n" +
    "    ctx.closePath();\n" +
    "    ctx.fill();\n" +

        //ctx.restore();
    "  }\n" +

     " function arrow2(ctx,p1,size){\n" +
     "   var dist = 10;\n" +
     "   ctx.beginPath();\n" +
     "   ctx.fillStyle = '#990000'\n" +
     "   ctx.lineCap = 'round';\n" +
     "   ctx.moveTo(p1.x+w/2, p1.y-h/2);\n" +
     "   ctx.lineTo(p1.x+w/2, p1.y);\n" +
        //ctx.lineTo(p1.x+w/2,p1.y+h); // 50 + 10 of the ellipse
     "   ctx.stroke();\n" +

        // arrowhead

     "   ctx.moveTo(p1.x+w/2, p1.y-h/2);\n" +
     "   ctx.lineTo(p1.x+w/2-size, p1.y-h/2+size*2.5);\n" +
     "   ctx.lineTo(p1.x+w/2+size, p1.y-h/2+size*2.5);\n" +
     "   ctx.closePath();\n" +
     "   ctx.fill();\n" +

        //ctx.restore();
     " }\n" +

     " function rectangle(ctx,x, y, color){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fillRect(x,y,w,h/2);\n" +
     "   ctx.strokeRect(x,y,w,h/2);\n" +
     "   ctx.fillStyle = '#FFFFFF';\n" +
     "   ctx.font = \"24px Arial\";\n" +
     "  return {x:x,y:y};\n" +
     " }\n" +

     " function drawText(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"bold 12px Verdana\";\n" +
     "   //ctx.strokeText(txt,x+20,y+5+h/4);\n" +
     "   ctx.fillText(txt,x+20,y+5+h/4);\n" +
     "   ctx.fillStyle = '#00000';\n" +
     " }\n" +

     " function drawText2(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"12px Calibri\";\n" +
     "   ctx.fillText('- ' + txt,x+w+5,y+10);\n" +    
     " }\n" +


     " function drawText3(ctx,x, y, color, txt){\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.font = \"12px Calibri\";\n" +
     "   ctx.fillText('- ' + txt,x+30,y+h/2-8);\n" +    
     " }\n" +

     " function circle(ctx,x,y, color){\n" +
     "   ctx.beginPath();\n" +
     "   ctx.arc(x, y, 50, 0, 20, false);\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fill();\n" +
     "   ctx.stroke();\n" +
     "   return {x:x,y:y};\n" +
     " }\n" +

    " function circle(ctx,x,y, color){\n" +
	"   ctx.save();\n" +
	"   //ctx.translate(canvas.width/2, ctx.height/2);\n" +
	"   //ctx.scale(2, 1);\n" +
	"   ctx.beginPath();\n" +
	"   ctx.arc(x+w/2, y, 50, 0, Math.PI * 2, false);\n" +
	"   ctx.fillStyle = color;\n" +
	"   ctx.fill();\n" +
	"   ctx.stroke();\n" +
	"   return {x:x,y:y};\n" +
	" }\n" +
     " function randomCircle(ctx,color){\n" +
     "   ctx.save();\n" +
     "   ctx.beginPath();\n" +
     "   ctx.arc(\n" +
     "     Math.round(Math.random()*(ctx.canvas.width  - 100) + 50),\n" +
     "     Math.round(Math.random()*(ctx.canvas.height - 100) + 50),\n" +
     "     Math.random()*20 + 10,\n" +
     "     0, Math.PI * 2, false\n" +
     "   );\n" +
     "   ctx.fillStyle = color;\n" +
     "   ctx.fill();\n" +
        //ctx.lineWidth = 0;
        //ctx.stroke();
     "   ctx.restore();\n" +
     " }\n" +


 "   </script>\n" +
 " </body></html>\n";
}
private static String getColor(String status) {
	String color ="#3333ff";
    if (status!=null && status.equals("Done"))
        color="#33ff00";
    else
        if (status!=null && status.equals("Failed"))
        	color="#ff0000";
        else
            if (status!=null && status.equals("On Hold"))
            	color="#ff6633";
    return color;

	}

private static String getHeaderReport() {
	return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
    +  "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en_US\" lang=\"en_US\">\n"
    +  "<head>\n"
    +  "<title></title>\n"
    +  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
    +  "<meta name=\"keywords\" content=\"\" />\n"
    +  "<meta name=\"description\" content=\"\" />\n"
    +  "<!--link href=\"css/default.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" /-->\n"
    +  "<style>\n"
    +  "body {font-family:calibri,verdana,arial,sans-serif; font-size:100%;}\n"
    +  "h1 {font-size:2.5em;}\n"
    +  "h2 {font-size:1.875em;}\n"
    +  "p {font-size:0.875em;}\n"
    +  "</style>\n"
    +  "\n"
    +  "</head>\n"
    +  "\n"
    +  "<body>\n"
    +  "<br />\n"
    +  "\n"
    +  "<!-- CSS goes in the document HEAD or added to your external stylesheet -->\n"
    +  "<style type=\"text/css\">\n"
    +  "table.reporttable {\n"
    +  "font-family: verdana,arial,sans-serif;\n"
    +  "font-size:11px;\n"
    +  "color:black;\n"
    +  "border-width: 1px;\n"
    +  "border-style: solid;\n"
    +  "border-color: red;\n"
    +  "border-collapse: collapse;\n"
    +  "}\n"
    +  "table.reporttable th {\n"
    +  "background:#ffffff url('cell-blue.jpg');\n"
    +  "border-width: 1px;\n"
    +  "padding: 3px;\n"
    +  "}\n"
    +  "table.reporttable td {\n"
    +  "font-size:11px;\n"
    +  "background-color:#ffffcc;\n"
    +  "border-width: 1px;\n"
    +  "padding: 3px;\n"
    +  "border-style: solid;\n"
    +  "border-color: red;\n"
    +  "color: #990000\n"
    +  "}\n"
    +  "\n"
    +  "</style>\n"
    +  "\n"
    +  "<!-- CSS goes in the document HEAD or added to your external stylesheet -->\n"
    +  "<style type=\"text/css\">\n"
    +  "table.activitytable {\n"
    +  "font-family: verdana,arial,sans-serif;\n"
    +  "font-size:11px;\n"
    +  "color:#333333;\n"
    +  "border-width: 1px;\n"
    +  "border-color: #999999;\n"
    +  "border-collapse: collapse;\n"
    +  "}\n"
    +  "table.activitytable th {\n"
    +  "background-color:#efefef;\n"
    +  "border-width: 1px;\n"
    +  "padding: 4px;\n"
    +  "border-style: solid;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "table.activitytable tr {\n"
    +  "background-color:#d4e3e5;\n"
    +  "}\n"
    +  "table.activitytable td {\n"
    +  "background-color:#ccccff;\n"
    +  "border-width: 1px;\n"
    +  "padding: 4px;\n"
    +  "border-style: none;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "table.smalltable {\n"
    +  "font-family: calibri, tahoma,verdana;\n"
    +  "}\n"
    +  "table.smalltable td {\n"
    +  "background-color:#f4f4f4;\n"
    +  "font-size:12px;\n"
    +  "color:#666666;\n"
    +  "padding: 1px;\n"
    +  "}\n"
    +  "table.entitytable {\n"
    +  "font-family: verdana,arial,sans-serif;\n"
    +  "font-size:11px;\n"
    +  "color:#333333;\n"
    +  "border-width: 1px;\n"
    +  "border-color: #999999;\n"
    +  "border-collapse: collapse;\n"
    +  "}\n"
    +  "table.entitytable th {\n"
    +  "background-color:#efefef;\n"
    +  "border-width: 1px;\n"
    +  "padding: 2px;\n"
    +  "border-style: solid;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "table.entitytable tr {\n"
    +  "background-color:#d4e3e5;\n"
    +  "}\n"
    +  "table.entitytable td {\n"
    +  "background-color:#ffffcc;\n"
    +  "border-width: 1px;\n"
    +  "padding: 2px;\n"
    +  "border-style: solid;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "table.hovertable {\n"
    +  "font-family: verdana,arial,sans-serif;\n"
    +  "font-size:11px;\n"
    +  "color:#333333;\n"
    +  "border-width: 1px;\n"
    +  "border-color: #999999;\n"
    +  "border-collapse: collapse;\n"
    +  "}\n"
    +  "table.hovertable th {\n"
    +  "background-color:#c3dde0;\n"
    +  "border-width: 1px;\n"
    +  "padding: 8px;\n"
    +  "border-style: solid;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "table.hovertable tr {\n"
    +  "background-color:#d4e3e5;\n"
    +  "}\n"
    +  "table.hovertable td {\n"
    +  "border-width: 1px;\n"
    +  "padding: 8px;\n"
    +  "border-style: solid;\n"
    +  "border-color: #a9c6c9;\n"
    +  "}\n"
    +  "</style>\n"
    +  "<script type=\"text/javascript\">\n"
    +  "function toggleDisplay(item) {\n"
    +  "if(document.getElementById(item).style.display == \"\" ) {\n"
    +  "document.getElementById(item).style.display = \"none\";\n"
    +  "}\n"
    +  "else {\n"
    +  "document.getElementById(item).style.display = \"\";\n"
    +  "}\n"
    +  "}\n"
    +  "</script>\n"
	+  "<table class=\"reporttable\" width=900 border=1>\n"
    +  "  <tr>\n"
    +  "    <th colspan=3 align=left><i><font color=#0000ff size=+1>Processing Report</font></th>\n"
    +  "    <th colspan=3 align=right><font color=#666666><script type=\"text/javascript\">document.write(new Date())</script></font></th>\n"
    +  "  </tr>\n";

}

}
