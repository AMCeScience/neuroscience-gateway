package nl.amc.biolab.tools;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nl.amc.biolab.datamodel.manager.HibernateUtil;

public class HibernateListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
    	try {
    		HibernateUtil.getSessionFactory();
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    		
    		e.printStackTrace();
    	}
    }

    public void contextDestroyed(ServletContextEvent event) {
    	HibernateUtil.getSessionFactory().close();
    }
}