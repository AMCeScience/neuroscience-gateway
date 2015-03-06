package nl.amc.biolab.tools;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nl.amc.biolab.config.manager.ConfigurationManager;
import nl.amc.biolab.datamodel.manager.HibernateUtil;

public class HibernateListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
    	try {
    		ConfigurationManager.init();
    		
    		System.out.println("############################################# Starting session factory");
    		
    		HibernateUtil.getSessionFactory();
    		
    		if (HibernateUtil.getSessionFactory().isClosed()) {
    			System.out.println("############################################# Failed session factory");
    		} else {
    			System.out.println("############################################# Done with session factory");
    		}
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    		
    		e.printStackTrace();
    	}
    }

    public void contextDestroyed(ServletContextEvent event) {
    	System.out.println("############################################# Destroying session factory");
    	
    	HibernateUtil.getSessionFactory().close();
    }
}