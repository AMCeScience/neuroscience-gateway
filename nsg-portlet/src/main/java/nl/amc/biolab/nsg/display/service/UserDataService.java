/*
 * Neuroscience Gateway Proof of Concept/Research Portlet
 * This application was developed for research purposes at the Bioinformatics Laboratory of the AMC (The Netherlands)
 *
 * Copyright (C) 2013 Bioinformatics Laboratory, Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.amc.biolab.nsg.display.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.amc.biolab.config.exceptions.ReaderException;
import nl.amc.biolab.config.manager.ConfigurationManager;
import nl.amc.biolab.datamodel.manager.PersistenceManager;
import nl.amc.biolab.datamodel.objects.Application;
import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.datamodel.objects.IOPort;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.datamodel.objects.Project;
import nl.amc.biolab.datamodel.objects.Resource;
import nl.amc.biolab.datamodel.objects.Status;
import nl.amc.biolab.datamodel.objects.Submission;
import nl.amc.biolab.datamodel.objects.SubmissionIO;
import nl.amc.biolab.datamodel.objects.User;
import nl.amc.biolab.datamodel.objects.UserAuthentication;
import nl.amc.biolab.nsg.dataobjects.NsgDataElement;
import nl.amc.biolab.nsg.dataobjects.NsgProject;
import nl.amc.biolab.nsg.dataobjects.NsgProperty;
import nl.amc.biolab.tools.BlobHandler;
import nl.amc.biolab.xnat.tools.XnatClient;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleLocalServiceUtil;

/**
 * UserDataService per user<br />
 * Connects to backend data management services<br />
 * Has current user data selections (projectDbId, ...)<br />
 *
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class UserDataService {
	private Long xnat_resource_id;
	
    Logger logger = Logger.getLogger(UserDataService.class);

    private PersistenceManager persistenceManager = new PersistenceManager();

    // current user objects
    private String liferayScreenName;
    private Long liferayId;
    private Long userId; // backend eCat user id

    private Long projectDbId;
    private Set<Long> dataElementDbIds;
    private Long processingDbId;

    protected UserDataService() {
        logger.setLevel(Level.DEBUG);
        
        try {
			xnat_resource_id = ConfigurationManager.read.getLongItem("nsg", "used_xnat_resource");
		} catch (ReaderException e) {
			xnat_resource_id = 8L;
		}
    }

    /**
     * Get a UserDataService for specified user<br />
     * Starts a PersistanceManager session which has to be explicitly closed
     * with closeSession() and can be opened again with openSession()<br />
     *
     * @param liferayScreenName the (portal) screen name of the current user
     * @param liferayId the (portal) user id of the current user
     * @param xnatLogin true is login into xnat
     * @throws SynchXNAT RuntimeException
     */
    public UserDataService(String liferayScreenName, Long liferayId) {
        this();

        if (liferayScreenName == null || liferayId == null) {
            userId = 0L;
            
            return;
        }

        this.liferayId = liferayId;
        this.liferayScreenName = liferayScreenName;
        
        openSession();

		//        logger.debug("Finalizing the creation of the user data service object.");
        
        User user = null;
        user = persistenceManager.get.user(liferayId.toString());
        
        this.userId = (user == null) ? 0L : user.getDbId();
    }

    public String getLiferayScreenName() {
        return liferayScreenName;
    }

    public void setLiferayScreenName(String liferayScreenName) {
        this.liferayScreenName = liferayScreenName;
    }

    public Long getLiferayId() {
        return liferayId;
    }

    public void setLiferayId(Long liferayId) {
        this.liferayId = liferayId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     *
     * @return current backend user
     */
    public User getUser() {
        User user = null;
        
        try {
            user = persistenceManager.user.get_user();
        } catch (Exception e) {
            logger.error("getUser failed", e);
        }

        return user;
    }

    public Long getProjectDbId() {
        return projectDbId;
    }

    public void setProjectDbId(Long projectDbId) {
        this.projectDbId = projectDbId;
    }

    public Set<Long> getDataElementDbIds() {
        return dataElementDbIds;
    }

    public void setDataElementDbIds(Set<Long> dataElementDbIds) {
        this.dataElementDbIds = dataElementDbIds;
    }

    public Long getProcessingDbId() {
        return processingDbId;
    }

    public void setProcessingDbId(Long processingDbId) {
        this.processingDbId = processingDbId;
    }

    /**
     * @return current project
     */
    public Project getProject() {
        Project project = null;
        try {
            project = persistenceManager.get.project(projectDbId);
        } catch (Exception e) {
            logger.error("getProject failed");
        }

        return project;
    }

    /**
     *
     * @return projects of current user
     */
    public List<NsgProject> getProjects() {
    	List<Project> projects = null;
        List<NsgProject> nsg_projects = new ArrayList<NsgProject>();
        
        try {
            projects = persistenceManager.get.projects();
            
            Long used_resource = ConfigurationManager.read.getLongItem("nsg", "used_xnat_resource");
            
            for (Project project : projects) {
            	System.out.println("this id: " + project.getValueByName("resource_id"));
            	
            	Long project_resource_id = null;
            	String resource_name = "";
            	
            	if (project.getValueByName("resource_id") != null) {
            		project_resource_id = Long.parseLong(project.getValueByName("resource_id"));
            		
            		if (project_resource_id != null && project_resource_id == used_resource) {
            			if (project_resource_id != null) {
        					Resource resource = persistenceManager.get.resource(project_resource_id);
        					
        					if (resource != null) {
        	            		resource_name = resource.getName();
        	            	}
        				}
            		} else {
            			// This project either is on the wrong resource or has no resource
            			
            			continue;
            		}
            	}
            	
            	nsg_projects.add(new NsgProject(project, resource_name));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return nsg_projects;
    }

    public Collection<NsgDataElement> getProjectData(Long projectDbId) {
    	Collection<DataElement> dataElements = new ArrayList<DataElement>();
    	Collection<NsgDataElement> nsgDataElements = new ArrayList<NsgDataElement>();

        if (projectDbId == null) {
            return nsgDataElements;
        }

        dataElements = persistenceManager.get.project(projectDbId).getDataElements();
        
        for(DataElement element : dataElements) {
        	if (element.getExisting()) {
        		NsgDataElement el = new NsgDataElement(element);
        		
        		nsgDataElements.add(el);
        	}
        }

        return nsgDataElements;
    }

    public DataElement getDataElement(Long dbId) {
        DataElement de = null;
        try {
            de = persistenceManager.get.dataElement(dbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return de;
    }

    /**
     *
     * @return applications of current user
     */
    public List<Application> getAllApplications() {
        List<Application> applications = null;
        
        try {
            applications = persistenceManager.get.applications();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        
        if (applications == null) {
        	System.out.println("no applications found");
        	
            applications = new ArrayList<Application>();
        }

        return applications;
    }

    /**
     *
     * @return processings of current user
     */
    public List<Processing> getCurrentProcessing() {
        List<Processing> processings = null;
        try {
            processings = persistenceManager.get.processings();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (processings == null) {
            processings = new ArrayList<Processing>();
        }

        return processings;
    }

    /**
     *
     * @return all processings
     */
    public List<Processing> getAllProcessing() {
        List<Processing> processings = null;
        try {
        	persistenceManager.user.override_user_view();
        	
            processings = persistenceManager.get.processings();
            
            persistenceManager.user.reset_user_view();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (processings == null) {
            processings = new ArrayList<Processing>();
        }

        return processings;
    }

    public Processing getProcessing(Long dbId) {
        Processing p = null;
        try {
            p = persistenceManager.get.processing(dbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return p;
    }

    /**
     *
     * @param processingDbId
     * @return list of related input and output submissionIOs maps
     */
    public List<Map<List<SubmissionIO>, List<SubmissionIO>>> getSubmissionIO(Long processingDbId) {
        List<Map<List<SubmissionIO>, List<SubmissionIO>>> submissionIOs = new ArrayList<Map<List<SubmissionIO>, List<SubmissionIO>>>();

        if (processingDbId == 0L) {
            return submissionIOs;
        }

        try {
            for (Submission sub : persistenceManager.get.processing(processingDbId).getSubmissions()) {
                logger.debug("\tsubmission: " + sub.getDbId() + ": " + sub.getName());
            	
                Map<List<SubmissionIO>, List<SubmissionIO>> map = new HashMap<List<SubmissionIO>, List<SubmissionIO>>();
                List<SubmissionIO> input = new ArrayList<SubmissionIO>();
                List<SubmissionIO> output = new ArrayList<SubmissionIO>();
                
                for (SubmissionIO io : persistenceManager.get.submission(sub.getDbId()).getSubmissionInputs()) {
                    input.add(io);
                    
                    logger.debug("\tinput: "
                            + io.getDataElement().getURI() + " on port "
                            + io.getPort().getPortNumber() + ", named "
                            + io.getPort().getPortName() + ", type:  "
                            + io.getType());
                }

                for (SubmissionIO io : persistenceManager.get.submission(sub.getDbId()).getSubmissionOutputs()) {
                    output.add(io);
                    
                    logger.debug("\toutput: " + ": "
                            + io.getDataElement().getURI() + " on port "
                            + io.getPort().getPortNumber() + ", named "
                            + io.getPort().getPortName() + ", type:  "
                            + io.getType());
                }
                
                map.put(input, output);
                submissionIOs.add(map);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return submissionIOs;
    }

    public nl.amc.biolab.datamodel.objects.Error getSubmissionError(long submissionId) {
    	nl.amc.biolab.datamodel.objects.Error err = null;
        try {
            err = persistenceManager.get.submission(submissionId).getErrors().iterator().next();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return err;
    }

    public boolean isNSGAdmin() {
        List<Role> roles = new ArrayList<Role>();
        try {
            roles = RoleLocalServiceUtil.getUserRoles(liferayId);
        } catch (SystemException e) {
            e.printStackTrace();
        }

        for (Role r : roles) {
            if (r.getName().equals("NSG Admin")) {
                return true;
            }
        }

        return false;
    }
    
    // NOTE: copy from ProcessingService.java:_getSessionUri()
	private String _getSessionUri(String dataElementUri) {
        final String sessionDescriptor = "/experiments/";
        
        int sessionIndex = dataElementUri.indexOf(sessionDescriptor) + sessionDescriptor.length();
        int endIndex = dataElementUri.indexOf("/", sessionIndex);
        
        if (sessionIndex == -1 || endIndex == -1) {
        	throw new IllegalArgumentException("No session information could be found in the provided URI.");
        }
        
        return dataElementUri.substring(0, endIndex);
	}
	
    public List<Application> getApplications(DataElement dataElement) {
    	List<Application> apps = new ArrayList<Application>();
    	List<Application> userApps = persistenceManager.get.applications();
    	
    	for (IOPort port : dataElement.getUsableIOPorts()) {
    		boolean may_use = false;
    		
    		Application app = port.getApplication();
    		
    		// Check if this user may access this application
    		for (Application userApp : userApps) {
    			if (userApp.getDbId() == app.getDbId()) {
    				may_use = true;
    				
    				break;
    			}
    		}
    		
    		// If not skip this IOPort
    		if (!may_use) {
    			continue;
    		}
    		
    		String appName = app.getName();
    		
    		// NOTE: adapted from ProcessingService.java:prepareSubmission()
    		// find matching inputs -> for tracula
    		if (appName.startsWith("Tracula")) {    // or appName.contains("Tracula"); also, ignore case? Prefer regex?
    			String sessionUri =  _getSessionUri(dataElement.getURI());
    			logger.debug("Finding Matching inputs; session URI determined as: " + sessionUri);
    			
    			boolean matched_inputs = true; // assume inputs exists unless proven otherwise
    			
    			for (IOPort trac_port : app.getInputPorts()) {
    				if (!trac_port.isVisible()) {    // ignore the visible ports that should be filled already by the UI (there should be one visible port, though)
    					DataElement el = getMatchingInput(sessionUri, trac_port.getDataFormat());
    					
    					if(el == null) { 
    						logger.debug("No matching input found for port ID " + trac_port.getDbId() + "(#" + trac_port.getPortNumber() + " " + trac_port.getPortName());
    						matched_inputs = false; // not all inputs can be matched!
    					}
    				}
    			}
    			
    			if(!matched_inputs) {
    				logger.debug("Tracula application will be skipped as usable application because of missing input data elements");
    				logger.debug("NOTE: fails silently for the user; i.e. reporting no matchinig application notifications");
    				continue; // skip Tracula application; TODO: show notification to user?
    			}
    		}
    		
    		apps.add(port.getApplication());
    	}
    	
    	return apps;
    }

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * check xnat user login
     *
     * @throws SynchXNAT checkUser RuntimeException
     */
    public boolean xnatLogin() {
		Resource xnat_resource = persistenceManager.get.resource(xnat_resource_id);
		
		User user = persistenceManager.get.user(liferayId.toString());
		boolean return_val = false;

		if (user == null) {
			logger.error("User '" + liferayId + "' Doesn't exist in the NSG catalogue");

			throw new RuntimeException("No User.");
		}

		UserAuthentication userAuth = persistenceManager.get.userAuthenticationByResourceId(user.getDbId(), xnat_resource_id);
		
		if (userAuth == null) {
			logger.error("No Credentials are set for user'" + liferayId + "' on resource " + xnat_resource_id);

			throw new RuntimeException("Wrong Password.");
		}

		if (userAuth.getAuthentication() == null) {
			logger.error("Password is not set for user'" + liferayId + "' on resource " + xnat_resource_id);

			throw new RuntimeException("No Password.");
		}
		
		XnatClient xnat = new XnatClient(xnat_resource.getBaseURI());
		
		BlobHandler handler = new BlobHandler();
		
		return_val = xnat.authenticateUser(userAuth.getUserLogin(), handler.decryptString(userAuth.getAuthentication()));
		
		if (return_val == false) {
			throw new RuntimeException("Wrong Password.");
		}
		
		return return_val;
    }
    
    public boolean checkAuthentication(User user) {
        return persistenceManager.get.userAuthenticationByResourceId(user.getDbId(), xnat_resource_id) != null 
        		&& persistenceManager.get.userAuthenticationByResourceId(user.getDbId(), xnat_resource_id).getAuthentication() != null;
    }

    /**
     * set xnat password
     *
     * @param password
     */
    public void setPassword(String username, String password) {
        if (liferayId == null) {
            return;
        }
        
        try {
            UserAuthentication auth = persistenceManager.get.userAuthenticationByResourceId(userId, xnat_resource_id);
            
            BlobHandler handler = new BlobHandler();
            
            if (auth == null) {
            	auth = persistenceManager.get.userAuthentication(
            			persistenceManager.insert.userAuthentication(username, handler.encryptString(password), "", persistenceManager.get.user(userId), persistenceManager.get.resource(xnat_resource_id))
        			);
            } else {
            	auth.setAuthentication(handler.encryptString(password));
            	
            	persistenceManager.update.userAuthentication(auth);
            }
        } catch (Exception e) {
            logger.error("setPassword failed");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public String getInputURI(Long dataId) {
    	String toReturn="";
    	
        try {
        	DataElement de = persistenceManager.get.dataElement(dataId);
        	
            if (de==null) {
            	return "";
            }
            
            toReturn = de.getURI();
            
            if (toReturn.contains("?format=zip")) {
            	toReturn = toReturn.substring(0, de.getURI().indexOf("?format=zip"));
            }
            
            return toReturn;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
    public String getViewURI(Long dataId) {
    	String toReturn="";
    	
        try {
        	DataElement de = persistenceManager.get.dataElement(dataId);
        	
            if (de==null) {
            	return "";
            }
            
            toReturn = de.getURI();
            
            if (toReturn.contains("/out/files/")) {
            	toReturn = toReturn.substring(0, de.getURI().indexOf("/out/files/")+11);
            }
            
            return toReturn;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
    public String getDownloadURI(Long dataId) {
    	String toReturn="";
    	
        try {
            DataElement de = persistenceManager.get.dataElement(dataId);
            
            if (de==null) {
            	return "";
            }
            
            toReturn = de.getURI();
            
            if (toReturn.contains("/out/files/")) {
            	toReturn = toReturn.substring(0, de.getURI().indexOf("/out/files/")+10);
            }
            
            return toReturn;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } 
        return "";
    }
    
    public Date getLastestUpdate(Processing processing) {
		Date update = null;
		
		for (Submission submission : processing.getSubmissions()) {
			Date this_update = submission.getLastStatus().getTimestamp();
			
			if (update == null) {
				update = this_update;
			} else if (this_update.after(update)) {
				update = this_update;
			}
		}
		
		return update;
	}
    
    @SuppressWarnings("unchecked")
	public DataElement getMatchingInput(String sessionUri, String requestedType) {
        try {
        	List<DataElement> results = persistenceManager.query.executeQuery("from DataElement where URI LIKE '%"+sessionUri+"%' AND Type = '"+requestedType+"' AND Existing=1"); // TODO: change to select any Recon Freesurfer, regardless of version?
            return results.iterator().next(); // WARNING: next() can return null if result set is empty; i.e. no matched input!
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        
        return null;
    }
    
    public String checkApplicationInput(Application app, List<List<Long>> data) {
    	String error = "";
    	
    	Collection<IOPort> ports = app.getInputPorts();
    	
    	if (data != null) {
    		for (List<Long> this_data : data) {
    			if (this_data.size() != ports.size()) {
    				error += "Warning, number of inputs: " + this_data.size() + " doesn't match number of ports: " + ports.size() + "\n";
    			}
    			
    			for (Long dataId : this_data) {
    				boolean checked = false;
    				
    				DataElement dataElement = persistenceManager.get.dataElement(dataId);
    				
    				if (dataElement != null) {
    					for (IOPort port : ports) {
    						if (dataElement.getPorts().contains(port)) {
    							checked = true;
    						}
    					}
    				}
    				
    				if (!checked) {
    					error += "Data element " + dataElement.getDbId() + " doesn't match any of the ports";
    				}
    			}
    		}
    	}
    	
    	if (error.length() < 1) {
    		return null;
    	}
    	
    	return error;
    }

    public void openSession() {
        logger.info("" + new Date() + " Opening a new session for user: " + liferayId.toString());
        
        persistenceManager.init(liferayId.toString());
        
        logger.info("" + new Date() + " Opening a new session: Finished.");
    }

    public void closeSession() {
        logger.info("Closing the session: " + new Date());
        
        persistenceManager.shutdown();
        
        logger.info("Finished closing the session: " + new Date());
    }

    public void reopenSession() {
        closeSession();
        
        openSession();
    }
    
    @SuppressWarnings("unchecked")
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
        	DataElement data =  persistenceManager.get.dataElement(dataId);
        	if (data==null)
        		return "No such data: " + dataId;
    	
        	body = body + "\tend   = drawEllipse(ctx, " + x + ", " + y + ", w, h/2,'#ffffcc');\n";
    		body = body + "\t\tdrawText(ctx, " + (x+20) + ", " + (y+5+h/4) + ",'" + txtColor + "', '" + data.getName() + "');\n";
    		dynamicStr += "\txs[" + nlinks + "] = " + (x+20) + "; \n";
    		dynamicStr += "\tys[" + nlinks + "] = " + (y+5+h/4) + "; \n";
    		dynamicStr += "\tlinks[" + nlinks + "] = \"" + data.getURI() + "\"; \n";
    		dynamicStr += "\ttxts[" + nlinks + "] = \"" + data.getName() + "\"; \n";
    		nlinks++;

    		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Subject ID: " + data.getValueByName("xnat_subject_label") + "');\n";
    		dist +=12;
    		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Scan ID:     " + data.getValueByName("xnat_scan_id") + "');\n";
    		dist +=24;
    		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Type:   " + data.getType() + "');\n";
    		dist +=12;
    		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Data Format: " + data.getFormat() + "');\n";
    		dist +=12;
    		body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Creation Date: " + data.getDate() + "');\n";
    		dist = 0;
            //body = body + "\t\tdrawText3(ctx, " + x + ", " + y + ",'" + txtColorA + "', 'Data ID: " + data.getDbId() + "');\n";
    		
    		y = y + h;
    	
    		Collection<SubmissionIO> submissionIOs = persistenceManager.query.executeQuery("from SubmissionIO where Type='Output' and DataID=" + dataId);
    	
    		if (submissionIOs.size()>0) {// && subId!=null) {
    			Iterator<SubmissionIO> iter = submissionIOs.iterator();
    			SubmissionIO submissionIO = (SubmissionIO) iter.next();
    			
    			subId = submissionIO.getSubmission().getDbId();
    		    //System.out.println("SubmissionIO_"+ subId + "<-");
    		    Submission submission = submissionIO.getSubmission();
    		    //System.out.print("subm_"+ subId + "<-");
    		    if (submission!=null && dataId!=null) {
    			    //System.out.println("Submission_"+ submission.getDbId() + "<-");
    		    	String color = getColor(submission.getLastStatus().getValue());

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
    		        body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorA + "', 'Submitter: " + submission.getProcessing().getUser().getFirstName() +
    		        		" " + submission.getProcessing().getUser().getLastName() + "');\n";
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
    		    
    		    Collection<SubmissionIO> subInputs = submissionIO.getSubmission().getSubmissionInputs();
    		    for (SubmissionIO subIn: subInputs) {
    		    	data = subIn.getDataElement();
					Collection<SubmissionIO> subOutputs = persistenceManager.query.executeQuery("from SubmissionIO where Type='Output' and DataID=" + subIn.getDataElement().getDbId());
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
    	
    					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Subject ID: " + data.getValueByName("xnat_subject_id") + "');\n";
    					dist +=12;
    					body = body + "\t\tdrawText2(ctx, " + x + ", " + (y+dist) + ",'" + txtColorE + "', 'Scan ID:     " + data.getValueByName("xnat_scan_id") + "');\n";
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
    
    @SuppressWarnings("unchecked")
    private Date getTime(Long sId, String status) {
       Collection<Status> statuses = persistenceManager.query.executeQuery("from Status where Value ='" + status + "' and SubmissionID='" + sId + "'");
       
       if (statuses == null || statuses.size() < 1) {
    	   return null;
       }
       
       Status this_status = statuses.iterator().next();
       
       if (this_status != null) {
    	   return this_status.getTimestamp();
       }
    	   
      return null;
    }
    
    public List<NsgProperty> getMetaData(DataElement dataElement) {
    	Resource xnat_resource = persistenceManager.get.resource(xnat_resource_id);
    	
        List<NsgProperty> metaData = new ArrayList<NsgProperty>();
    
		XnatClient xnat = new XnatClient(xnat_resource.getBaseURI());
		
		if (xnat.checkAvailability()) {
    		UserAuthentication userAuth = persistenceManager.get.userAuthenticationByResourceId(getUser().getDbId(), xnat_resource_id);
    		
    		BlobHandler handler = new BlobHandler();
    		
    		if (xnat.authenticateUser(userAuth.getUserLogin(), handler.decryptString(userAuth.getAuthentication()))) {
    			Project project = persistenceManager.get.project(projectDbId);
    			
    			HashMap<String, String> rawData = xnat.getXnatMetadata(project.getValueByName("xnat_project_id"), dataElement.getURI());
    			
    			Iterator<Entry<String, String>> rawResult = rawData.entrySet().iterator();
    			
    			while (rawResult.hasNext()) {
					Map.Entry<String, String> pair = (Map.Entry<String, String>) rawResult.next();
    				
    				metaData.add(new NsgProperty(pair.getKey(), pair.getValue()));
    			}
    		}
    	}

        return metaData;

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
    
    private static String getColor(String status) {
		String color ="#3333ff";
		
	    if (status!=null && status.equals("Done")) {
	        color="#33ff00";
	    } else {
	        if (status!=null && status.equals("Failed")) {
	        	color="#ff0000";
	        } else {
	            if (status!=null && status.equals("On Hold")) {
	            	color="#ff6633";
	            }
	        }
	    }
	    return color;
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
		Processing proc =  persistenceManager.get.processing(pId);
		if (proc==null)
			return "No such a Processing: " + pId;

		// query to get start/end time and duration
		List<?> result = persistenceManager.query.executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)) FROM neuroscience.Status where SubmissionID in (select SubmissionID from neuroscience.Submission where Processingid=" + pId + ")");
		
	    Iterator<?> iter = result.iterator();
		Object[] row = (Object[]) iter.next();

		body += "\t<Name>" + proc.getName() + "</Name>\n\t<Description>" + proc.getDescription() + "</Description>\n";
		body +=  "\t<Status>" + proc.getStatus() + "</Status>\n\t<Date>" + proc.getDate() + "</Date>\n";
		body += "\t<ApplicationName>" + proc.getApplication().getName() + "</ApplicationName>\n";
		body += "\t<ApplicationVersion>" + proc.getApplication().getVersion() + "</ApplicationVersion>\n";
		body += "\t<ApplicationDesc>" + proc.getApplication().getDescription() + "</ApplicationDesc>\n";
		body += "\t<User>" + proc.getUser().getFirstName() + " " + proc.getUser().getLastName() + "</User>\n";
		body += "\t<NbreTasks>" + proc.getSubmissions().size() + "</NbreTasks>\n";
		body += "\t<StartTime>" + row[0] + "</StartTime>\n";
		body += "\t<EndTime>" + row[1] + "</EndTime>\n";
		body += "\t<ElapsedTime>" + row[2] + "</ElapsedTime>\n";
		body += "\t<CPUTime>###cputime</CPUTime>\n";
		
		processes += "\t<Processes>\n";
		for (Submission sub:proc.getSubmissions()) {
			// query to get start/end time and duration
			List<?> subresult = persistenceManager.query.executeSQL("SELECT min(statustime) min, max(statustime) max, timediff(max(statustime), min(statustime)), time_to_sec(timediff(max(statustime), min(statustime))) FROM neuroscience.Status where SubmissionID=" + sub.getDbId());
			
		    Iterator<?> subiter = subresult.iterator();
			Object[] subrow = (Object[]) subiter.next();
			CPUTime += Integer.parseInt(subrow[3].toString());
			
			processes += "\t\t<Process Name=\"" + sub.getDbId() + "\" Status=\"" + sub.getLastStatus().getValue() + "\"";
			processes += " Start=\"" + subrow[0] + "\" End=\"" + subrow[1]  + "\" Duration=\"" + subrow [2] + "\">\n";
			processes += "\t\t <Statuses>\n";
			for (Status status:sub.getStatuses()) {
				processes += "\t\t  <Status Value=\"" + status.getDbId() + "\" Timestamp=\"" + status.getTimestamp() + "\" />\n";
			}
			

			processes += "\t\t </Statuses>\n\t\t</Process>\n";
			for (SubmissionIO subio:sub.getSubmissionIOs()) {
				if (subio.getType().equalsIgnoreCase("Input")) {
					inputs += "\t\t<Input Name=\"" + subio.getDataElement().getName() + "\" Date=\"" + subio.getDataElement().getDate();
					inputs += "\" Size=\"" + subio.getDataElement().getSize() + "\" ScanID=\"" + subio.getDataElement().getValueByName("xnat_scan_id") + "\" />\n";
					sizeInput += subio.getDataElement().getSize();
					in++;
				}
				if (subio.getType().equalsIgnoreCase("Output")) {
					outputs += "\t\t<Input Name=\"" + subio.getDataElement().getName() + "\" Date=\"" + subio.getDataElement().getDate();
					outputs += "\" Size=\"" + subio.getDataElement().getSize() + "\" ScanID=\"" + subio.getDataElement().getValueByName("xnat_scan_id") + "\" />\n";									
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
}
