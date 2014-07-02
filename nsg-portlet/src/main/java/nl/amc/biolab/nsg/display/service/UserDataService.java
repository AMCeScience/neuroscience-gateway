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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.amc.biolab.nsgdm.Error;
import nl.amc.biolab.Tools.PersistenceManager;
//import nl.amc.biolab.Tools.ProcessingPersistence;
//import nl.amc.biolab.Tools.SynchXNAT;
import nl.amc.biolab.Tools.XNATRestClient;
import nl.amc.biolab.nsg.display.VaadinTestApplication;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.Property;
import nl.amc.biolab.nsgdm.Submission;
import nl.amc.biolab.nsgdm.SubmissionIO;
import nl.amc.biolab.nsgdm.User;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleLocalServiceUtil;
import java.util.Date;

/**
 * UserDataService per user<br />
 * Connects to backend data management services<br />
 * Has current user data selections (projectDbId, ...)<br />
 *
 * TODO refactor, split into components, reorder code, etc.<br />
 *
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class UserDataService {

    private static final long XNAT_DATASOURCE_ID = 1L;

    Logger logger = Logger.getLogger(UserDataService.class);

    public static final String NO_PASSWORD = PersistenceManager.NO_PASSWORD;
    public static final String WRONG_PASSWORD = PersistenceManager.WRONG_PASSWORD;
    public static final String NO_USER = PersistenceManager.NO_USER;

    private PersistenceManager persistenceManager = new PersistenceManager();
    private PersistenceManager synchXNAT = persistenceManager;
    private PersistenceManager processingPersistance = persistenceManager; // new ProcessingPersistence();

    // current user objects
    private String liferayScreenName;
    private Long liferayId;
    private Long userId; // backend eCat user id

    private Long projectDbId;
    private Set<Long> dataElementDbIds;
    private Long processingDbId;

    protected UserDataService() {
        logger.setLevel(Level.DEBUG); //TODO move to log4j.xml
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
    public UserDataService(String liferayScreenName, Long liferayId, boolean xnatLogin) {
        this();

        if (liferayScreenName == null) {
            userId = 0L;
            return;
        }

        this.liferayId = liferayId;

        this.liferayScreenName = liferayScreenName;
        User user = null;
        openSession();

        logger.debug("Checking if we should login to XNAT");
        if (xnatLogin && isDataSourceAlive()) {
            xnatLogin();
            user = getXnatUser();
        }

        if (user == null) {
            userId = 0L;
        }

        logger.debug("Finalizing the creation of the user data service object.");
        try {
            user = persistenceManager.getUser(liferayId.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        userId = (user == null) ? 0L : user.getDbId();
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
            user = persistenceManager.getUser(userId);
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

    public PersistenceManager getProcessingPersistance() {
        return processingPersistance;
    }

    /**
     * @return current project
     */
    public Project getProject() {
        Project project = null;
        try {
            project = persistenceManager.getProject(projectDbId);
        } catch (Exception e) {
            logger.error("getProject failed");
        }

        return project;
    }

    /**
     *
     * @return projects of current user
     */
    public List<Project> getProjects() {
        List<Project> projects = null;
        try {
            projects = persistenceManager.getProjects();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (projects == null) {
            projects = new ArrayList<Project>();
        }

        return (projects != null) ? projects : new ArrayList<Project>();
    }

    @SuppressWarnings("unchecked")
    public List<DataElement> getProjectData(Long projectDbId) {
        List<DataElement> dataElements = new ArrayList<DataElement>();

        if (projectDbId == null) {
            return dataElements;
        }

        try {
            dataElements = persistenceManager.getProjectData(projectDbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (dataElements == null) {
            dataElements = new ArrayList<DataElement>();
        }

        return dataElements;
    }

    public DataElement getDataElement(Long dbId) {
        DataElement de = null;
        try {
            de = persistenceManager.getDataElement(dbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return de;
    }

    public List<Property> getMetaData(DataElement dataElement) {
        List<Property> metaData = null;
        try {
            metaData = synchXNAT.getXnatMetadata(userId, projectDbId, dataElement.getDbId(), dataElement.getResource().getDbId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (metaData == null) {
            metaData = new ArrayList<Property>();
        }

        return metaData;

    }

    /**
     *
     * @return applications of current user
     */
    public List<Application> getAllApplications() {
        List<Application> applications = null;
        try {
            applications = persistenceManager.getApplications();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (applications == null) {
            applications = new ArrayList<Application>();
        }

        return applications;
    }

    /**
     * get applications who can process dataElement
     *
     * @param dataElementDbId
     * @return
     */
    public List<Application> getApplications(long dataElementDbId) {
        List<Application> applications = null;
        try {
            applications = persistenceManager.getApplications(dataElementDbId);
        } catch (Exception e) {
            logger.error(e.getMessage());

        }
        if (applications == null) {
            applications = new ArrayList<Application>();
        }

        return applications;
    }

    /**
     *
     * @param applicationId
     * @param inputData dbIds
     * @returns error string for check failure else null
     */
    public String checkApplicationInput(long applicationId, List<List<Long>> inputData) {
        return persistenceManager.checkApplicationInput(applicationId, inputData);
    }

    /**
     *
     * @return processings of current user
     */
    public List<Processing> getCurrentProcessing() {
        List<Processing> processings = null;
        try {
            processings = persistenceManager.getProcessings();
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
            processings = persistenceManager.getAllProcessings();
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
            p = persistenceManager.getProcessing(dbId);
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
            for (Submission sub : processingPersistance.getProcessingSubmissions(processingDbId)) {
                logger.debug("\tsubmission: " + sub.getDbId() + ": "
                        + sub.getName());
                Map<List<SubmissionIO>, List<SubmissionIO>> map = new HashMap<List<SubmissionIO>, List<SubmissionIO>>();
                List<SubmissionIO> input = new ArrayList<SubmissionIO>();
                List<SubmissionIO> output = new ArrayList<SubmissionIO>();
                for (SubmissionIO io : processingPersistance.getSubmissionInputs(sub.getDbId())) {
                    input.add(io);
                    logger.debug("\tinput: "
                            + io.getDataElement().getURI() + " on port "
                            + io.getPort().getPortNumber() + ", named "
                            + io.getPort().getPortName() + ", type:  "
                            + io.getType());
                }

                for (SubmissionIO io : processingPersistance.getSubmissionOutputs(sub.getDbId())) {
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

    public Error getSubmissionError(long submissionId) {
        Error err = null;
        try {
            err = persistenceManager.getSubmissionError(submissionId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return err;
    }

    public String getViewURI(Long dataElementDbId) {
        String s = "";
        try {
            s = persistenceManager.getViewURI(dataElementDbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return s;
    }
    
    public String getDataHistory(Long dataID){
        String s = "";
        try {
            s = persistenceManager.getDataHistory(dataID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return s;
    }
    
    public String getProcessingReport(Long processingId){
        String s = "";
        try{
            s = persistenceManager.getProcessingReport(processingId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return s;
    }
    
    public String getDownloadURI(Long dataElementDbId) {
        String s = "";
        try {
            s = persistenceManager.getDownloadURI(dataElementDbId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return s;
    }

    public void updateCatalogue() {
        logger.error("This should not be called, because synchronization is done offline.");
//		try {
//			persistenceManager.updateCatalogue(userId);
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//		}
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

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }
//
//	public SynchXNAT getSynchXNAT() {
//		return synchXNAT;
//	}
//
//	public void setSynchXNAT(SynchXNAT synchXNAT) {
//		this.synchXNAT = synchXNAT;
//	}

    /**
     * is the DataSource (xnat) alive?
     *
     * @return
     */
    public boolean isDataSourceAlive() {
        try {
            return persistenceManager.isDataSourceAlive(XNAT_DATASOURCE_ID);
        } catch (Exception e) {
            logger.error("isDataSourceAlive failed");
        }

        return false;
    }

    /**
     * check xnat user login
     *
     * @throws SynchXNAT checkUser RuntimeException
     */
    public void xnatLogin() {
        synchXNAT.checkUser(liferayId.toString());
    }
    
    public boolean checkAuthentication(User user) {
        return persistenceManager.getAuthentication(user.getDbId(), XNAT_DATASOURCE_ID).getAuthentication() != null;
    }

    /**
     * @return user or null if not found
     */
    public User getXnatUser() {
        if (liferayId == null) {
            return null;
        }
        try {
            return persistenceManager.getUser(liferayId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * set xnat password
     *
     * @param password
     */
    public void setPassword(String password) {
        if (liferayId == null) {
            return;
        }
        try {
            String username = persistenceManager.getAuthentication(userId, XNAT_DATASOURCE_ID).getUserLogin();
            persistenceManager.setUserPassword(getXnatUser().getDbId(), username, password, XNAT_DATASOURCE_ID);
        } catch (Exception e) {
            logger.error("setPassword failed");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void openSession() {
        logger.info("" + new Date() + " Opening a new session.");
        persistenceManager.init(liferayId.toString());
        //processingPersistance.init();
        logger.info("" + new Date() + " Opening a new session: Finished.");
    }

    public void closeSession() {
        logger.info("Closing the session: " + new Date());
        persistenceManager.shutdown();
//            processingPersistance.shutdown();
        logger.info("Finished closing the session: " + new Date());
    }

//	public void close() {
//		closeSession();
//		userId = null;
//		liferayId = null;
//	}

    public void reopenSession() {
        // TODO: Maybe a better solution by hibernate?
        closeSession();
        openSession();
    }
}
