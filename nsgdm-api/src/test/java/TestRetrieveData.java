import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nl.amc.biolab.Tools.PersistenceManager;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.Property;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.UserProcessing;
import nl.amc.biolab.nsgdm.UserProject;

/**
*
   Created on : 26 November 2012, 16:59
   Author     : a.benabdelkader@amc.uva.nl
*/

/** * * * * *  Unit test for data retrieval  * * * * * * 
 * 
 * This package contains three (03) examples for querying the data based on:
 * 1- Project ID
 * 2- Experiment ID
 * 3- User ID
 */
public class TestRetrieveData 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param TestRetrieveData
     */
    public TestRetrieveData( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestRetrieveData.class );
    }

    public void testApp()
    {
    	
 /*   	//SynchXNAT xnat = new SynchXNAT ();
    	try {
			SynchXNAT.initEndPoint(1);
	   	 	User user = SynchXNAT.getUser("Ammar");
	   	 //User user = SynchXNAT.checkUser("admin");
	   	 	if (user==null)
	   	 		return;
	   	 	//User user = SynchXNAT.getUser("Ammar");
	   	 	List<Property> properties = SynchXNAT.getXnatMetadata(user.getDbId(), new Long (1), "http://mri-neutrino:8080/xnatZ0/data/experiments/xnatZ0_E00001/scans/501/files?format=zip");
	        for (Property property : properties) {
	       		 System.out.println("\t\t" + property.getKey() + " "  + property.getDescription() + ": " + property.getValue());
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/    	PersistenceManager pm = new PersistenceManager(); 
    	User user = pm.init(1L);
  		Long[] dataa = {3L,7L,99L,8L};
  		//System.out.println("\n\t Processing: " + pm.storeProcessing(pm.getNewProcessingID(), "name5", "Description", "Date3", "Developers", new Long (1), new Long (1), new Long (1), dataa));
  		//System.out.println("\n\t Processing Input: " + pm.setProcessingInput(new Long (28), dataa));
  		//System.out.println("\n\t Processing Output: " + pm.setProcessingOutput(new Long (28), dataa));
  		//System.out.println("\n\t Processing Output: " + pm.storeProcessing("name2", "Description", "Date", "Developers", new Long (1), new Long (1), new Long (1)));
/*    	Collection<DataElement> ProcessingOutput = new ArrayList<DataElement>();
    	for (int i=1; i<=3; i++){
        	DataElement data = new DataElement();
        	data.setURI("Output Uri - "+ i);
        	data.setType("Nifti");
        	ProcessingOutput.add(data);
    	}
  		System.out.println("\n\t Processing Output: " + pm.setProcessingOutput(new Long (31), ProcessingOutput));
*/ 

   	
    	//User u = pm.getUser("Ammar");
    	
    	/** * * * * Retrieve projects for the given user session * * * *
         * 1- the list of projects with their details 
         * 2- for each project, all the experiments with their details
         * 3- for each project, all the data elements with their details
         */

    	Collection<Application> applications = pm.getApplications();
  		int j=1;
  		if (applications.size()<1) {
  			System.out.println("No applications for user " + user.getFirstName() + " " + user.getLastName());
  		}
  			
  	    System.out.println("\tApplications: ");
  		for (Application app : applications) {
  	  	    System.out.println("Application " + (j++) + ": " + app.getName() + " - " + app.getDescription() + " (" + app.getIOPorts().size() + " port(s))");
	  		int k=1;
  	  	    System.out.println("\tProcessings: ");
	  		for (Processing exp : app.getProcessings()) {
	  	  	    System.out.print("\t  Processing " + (k++));
	  	  	    //System.out.println(":\t" + exp.getName() + "\t" + exp.getDevelopers() + "\tApp: " + exp.getApplication().getName());
	  	  	    //System.out.println(": " + exp.getName() + " - " + exp.getDevelopers() + ", App: " + exp.getApplication().getName());
  		    }
	  		k=1;
  	  	    System.out.println("\tIO Ports: ");
	  		for (IOPort port : app.getIOPorts()) {
	  	  	    System.out.print("\t  Port " + (k++));
	  	  	    System.out.println(":\t" + port.getPortName() + "\t" + port.getIOType() + "\t" + port.getDataType() + "\t" + port.getDataFormat());
  		    }
  		}

    	
    	/** * * * * Retrieve projects for the given user session * * * *
         * 1- the list of projects with their details 
         * 2- for each project, all the experiments with their details
         * 3- for each project, all the data elements with their details
         */

    	Collection<Project> projects = pm.getProjects();
  		j=1;
  		if (projects==null) {
  			System.out.println("No projects for that user");
  			return;
  		}
  			
  	    System.out.println("\tProjects: ");
  		for (Project project : projects) {
  	  	    System.out.println("Project " + (j++) + ": " + project.getName() + " - " + project.getDescription() + " (" + project.getDataElements().size() + " data elements)");
	  		int k=1;
  	  	    System.out.println("\tProcessings: ");
	  		for (Processing exp : project.getProcessings()) {
	  	  	    System.out.print("\t  Processing " + (k++));
	  	  	    //System.out.println(":\t" + exp.getName() + "\t" + exp.getDevelopers() + "\tApp: " + exp.getApplication().getName());
	  	  	    //System.out.println(": " + exp.getName() + " - " + exp.getDevelopers() + ", App: " + exp.getApplication().getName());
  		    }
	  		k=1;
  	  	    System.out.println("\tData Elements: ");
	  		for (DataElement elt : project.getDataElements()) {
	  	  	    System.out.print("\t  Data " + (k++));
	  	  	    System.out.println(":\t" + elt.getScanID() + "\t" + elt.getURI());
  		    }
  		}

    	/** * * * * Retrieve processing for the given user session * * * *
         * 1- the list of processings with their details 
         * 2- for each processing, all the experiments with their details
         * 3- for each processing, all the data elements with their details
         */
  		
    	Collection<Processing> processings = pm.getProcessings();
  		if (processings==null) {
  			System.out.println("No processings for that user");
  			return;
  		}
  			
	  	j=1;
  	  	System.out.println("\n\nProcessings: ");
	  	for (Processing prc : processings) {
	  	  	System.out.print("\tProcessing " + (j++));
	  	  	//System.out.println(":\t" + prc.getName() + "\t" + prc.getDevelopers() + "\tApp: " + prc.getApplication().getName() + "\tProject: " + prc.getProject().getName());
	  	  	//System.out.println(": " + exp.getName() + " - " + exp.getDevelopers() + ", App: " + exp.getApplication().getName());
  		    
	  		int k=1;
  	  	    System.out.println("\t\tInput Data Elements: ");
/*	  		for (DataElement elt : prc.getInputData()) {
	  	  	    System.out.print("\t\t  Input " + (k++));
	  	  	    System.out.println(":\t" + elt.getScanID() + "\t" + elt.getURI());
  		    }
*/	  		
	  		k=1;
  	  	    System.out.println("\t\tOutput Data Elements: ");
/*	  		for (DataElement elt : prc.getOutputData()) {
	  	  	    System.out.print("\t\t  Output " + (k++));
	  	  	    System.out.println(":\t" + elt.getScanID() + "\t" + elt.getURI());
  		    }
*/	  		
	  		k=1;
  	  	    System.out.println("\t\tProcessing Users: ");
	  		for (User u2 : prc.getUsers()) {
	  	  	    System.out.print("\t\t  User " + (k++));
	  	  	    System.out.println(":\t" + u2.getFirstName() + "\t" + u2.getLastName());
  		    }
	  		}

  		pm.shutdown();
if (1==1)
	return;

//DataManager pm = DataManager.instance();
//        pm.init();
        
        /** * * * * Projects Retrieval  * * * *
         * For each project, we retrieve:
         * 1- Project details
         * 2- all experiments with their details
         * 3- all users with their details and access rights (ACLs)
         * 4- all applications with their details
         * 5- all the data elements with their metadata
         */
        Collection<Project> userProjects = pm.executeQuery("from Project");
        int i=1;
  	    for (Project prj : userProjects) {
  	        System.out.println("\nProject " + (i++) + ": " + prj.getName() + " - " + prj.getDescription());
  	        System.out.println("\tExperiments: ");
  	        j = 1;
  		    for (Processing exp : prj.getProcessings()) {
  		        //System.out.println("\t  Experiment " + (j++) + ": " + exp.getName() + ", developpers: " + exp.getDevelopers());
  	 	    }
  		    j=1;
  	        System.out.println("\tUsers: ");
  		    for (User u : prj.getUsers()) {
  		    		System.out.println("\t  User " + (j++) + ": " + u.getLiferayID() + ": " + u.getFirstName() + " " + u.getLastName());
	 	    }
  		    j=1;
  	        System.out.println("\tUsers2: ");
  		    for (User u : prj.getUsers()) {
  		    		System.out.println("\t  User " + (j++) + ": " + u.getLiferayID() + ": " + u.getFirstName() + " " + u.getLastName());
	 	    }
  		    j=1;
  	        System.out.println("\tData elements: ");
  		    for (DataElement data : prj.getDataElements()) {
  	    		System.out.println("\t  Element " + (j++) + ": " + data.getURI());
  	    		
  	    		for (Property prp : data.getProperties()) {
  	    			System.out.println("\t\t" + "- " + prp.getKey() + ": " + prp.getValue() + ", ");
  	    		}
  		    }
  	    }

        /** * * * * Experiment Retrieval  * * * *
         * For each experiment, we retrieve:
         * 1- all users with their details and access rights (ACLs)
         * 2- all applications with their details
         * 3- all projects with their details
         * 2- for each project, all the data elements with their metadata
         */
      	  Processing exp = (Processing)  pm.get(Processing.class, new Long(1));
        System.out.print("Experiment: ");
	    //System.out.println(exp.getName() + ", developpers: " + exp.getDevelopers());
  		j=1;
  	    System.out.println("\tUsers: ");
		for (User u : exp.getProject().getUsers()) {
		    		System.out.println("\t  User " + (j++) + ": " + u.getLiferayID() + ": " + u.getFirstName() + " " + u.getLastName());
 	    }
        System.out.println("\tApplication: " + exp.getApplication().getName() + " - " + exp.getApplication().getOS());
        System.out.println("\tProject: " + exp.getProject().getName() + " - " + exp.getProject().getDescription());
  		    j=1;
  	        System.out.println("\tData elements: ");
/*  		    for (DataElement data : exp.getOutputData()) {
  	    		System.out.println("\t  Element " + (j++) + ": " + data.getURI());
  	    		
  	    		for (Property prp : data.getProperties()) {
  	    			System.out.println("\t\t" + "- " + prp.getKey() + ": " + prp.getValue() + ", ");
  	    		}
  		    }
*/  	        /** * * * * User Retrieval  * * * *
  	         * For each user, we retrieve:
  	         * 1- the user details
  	         * 2- all projects with their details and user access rights (ACLs)
  	         * 3- for each project, all the experiments with their details
  	         * 4- for each project, all the applications with their details
  	         */
      	  	  User u = (User) pm.get(User.class, new Long(1));
  	        Collection<Project> projectss = u.getProjects();
	        System.out.print("User: ");
		    System.out.println(u.getLiferayID() + ": " + u.getFirstName() + " " + u.getLastName());
	  		j=1;
  	  	    System.out.println("\tProjects: ");
	  		for (Project project : projectss) {
	  	  	    System.out.println("\t  Project " + (j++) + ": " + project.getName() + " - " + project.getDescription() + " (" + project.getDataElements().size() + " data elements)");
	  		}
	  		j=1;
  	  	    System.out.println("\tUser Experiments: ");
  		    for (UserProcessing ue : u.getExperimentACLs()) {
	  	  	    System.out.print("\t  Experiment " + (j++));
	  	  	    //System.out.println(": " + ue.getProcessing().getName() + ", developpers: " + ue.getProcessing().getDevelopers() + ", ACL: " + ue.getACL());
	 	    }

        assertTrue( true );
    }
}
