
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.UserProcessing;
import nl.amc.biolab.nsgdm.UserProject;

import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/** * * * * *  Unit test for data retrieval  * * * * * * 
 * 
 * This package contains three (03) examples for querying the data based on:
 * 1- Project ID
 * 2- Experiment ID
 * 3- User ID
 */
public class TestUpdateData 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param AppRetrieveData
     */
    public TestUpdateData( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestUpdateData.class );
    }

    public void testApp()
    {
        //PersistenceManager pm = PersistenceManager.instance();
        //pm.init();
        
        /** * * * * Update Test  * * * *
         * This example shows how to make data updates using the API:
         * 1- Create a new experiment 'exp'
         * 2- link the experiment to an existing project
         * 3- link the experiment to an existing application
         * 4- persist the experiment
         */

       
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
//*        
        session.beginTransaction();
        Processing exp = new Processing();
        exp.setName("DTI ATLAS 2");
        exp.setDescription("DTI ATLAS 2");
        //exp.setDevelopers("Vladimir");
        //DataElement elt = (DataElement) pm.get(DataElement.class, new Long(5));
        Project project = (Project) session.get(Project.class, new Long(1));
        Application app = (Application) session.get(Application.class, new Long(1));

        exp.setProject(project);
        exp.setApplication(app);
        project.getProcessings().add(exp);
        app.getProcessings().add(exp);
 	    //exp.getElements().add(elt);
 	    //elt.getExperiments().add(exp);
 	    

 	    	session.persist(exp);
 	    	//session.update(project);
 	    	//session.update(app);
 	        session.getTransaction().commit();
//*/        
 	        /** * * * * Update Test  * * * *
 	         * This example shows how to create a new user and grant him access to project and application:
 	         * 1- Create a new user 
 	         * 2- grant the user access to project 1
 	         * 3- grant the user access to application 1
 	         * 4- persist the changes
 	         */
 	       User user = (User) session.get(User.class, new Long(1));
 	       exp = (Processing) session.get(Processing.class, new Long(1));
 	        UserProcessing ue = new UserProcessing();
 	        
        session.beginTransaction();
    	//exp.getUsers().add(user);
    	//user.getExperiments().add(exp);
 	    
        ue.setACL("RRR");
        ue.setUser(user);
        ue.setProcessing(exp);
    	
 	    session.save(ue);

        session.getTransaction().commit();
	        /** * * * * Update Test  * * * *
	         * This example shows how to create a new user and grant him access to project and application:
	         * 1- Create a new user 
	         * 2- grant the user access to project 1
	         * 3- grant the user access to application 1
	         * 4- persist the changes
	         **/
       //*
	        user = new User();
	        //user.setUserID("Shayan");
	        user.setFirstName("Shayan");
	        user.setLastName("Shahad");
	        user.setAffiliation("AMC");
	        user.setAffiliation("AMC, Bio Lab");
	        app = (Application) session.get(Application.class, new Long(1));
	        project = (Project) session.get(Project.class, new Long(1));
	        UserProject acl = new UserProject();
	        
    session.beginTransaction();
	app.getUsers().add(user);
	user.getApplications().add(app);
	    
/*	    acl.setACL("RRR");
	    acl.setUser(user);
	    acl.setProject(project);
	    acl.getUsers().add(user);
	    acl.getProjects().add(project);
	    project.getACLs().add(acl);
	    user.getACLs().add(acl);
*/	    user.getProjects().add(project);
	    project.getUsers().add(user);
	
	    session.update(app);

    session.getTransaction().commit();
//*/    
        assertTrue( true );
    }
}
