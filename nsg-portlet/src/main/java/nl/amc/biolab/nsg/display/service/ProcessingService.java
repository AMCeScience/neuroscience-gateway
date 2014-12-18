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

import java.util.HashMap;
import java.util.Set;

import nl.amc.biolab.config.manager.ConfigurationManager;
import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.datamodel.objects.IOPort;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.nsg.display.data.DisplayProcessingStatus;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class ProcessingService {
	Logger logger = Logger.getLogger(ProcessingService.class);

	protected UserDataService userDataService;

	// protected ProcessingManager processingManager = new ProcessingManager();

	public ProcessingService() {
	}

	public ProcessingService(UserDataService userDataService) {
		logger.setLevel(Level.DEBUG);
		this.userDataService = userDataService;
	}

	/**
	 * 
	 * @param prjID
	 * @param appID
	 * @param filesPerPorts
	 * @param catUserID
	 * @param liferayUserID
	 * @param description
	 * @return processing dbId
	 */
	@SuppressWarnings("unchecked")
	public Long submit(Long userId, Long projectId, Processing processing, JSONArray submits) {

		Long dbId = null;

		try {
			userDataService.closeSession();

			JSONObject submission = new JSONObject();

			submission.put("applicationId", processing.getApplication().getDbId());
			submission.put("description", processing.getName());
			submission.put("userId", userId);
			submission.put("projectId", projectId);
			submission.put("submission", submits);

			ClientConfig clientConfig = new DefaultClientConfig();
			clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(clientConfig);

			// Logging
			client.addFilter(new LoggingFilter(System.out));

			WebResource webResource = client.resource(ConfigurationManager.read.getStringItem("nsg", "processing_url"));

			ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, submission);

			int response_code = response.getStatus();
			
			if (response_code == 200) {
				JSONObject body = response.getEntity(JSONObject.class);
				
				dbId = Long.valueOf((Integer) body.get("processingId"));
			} else {
				logger.debug("Error in REST #########################################");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			userDataService.openSession();
		}

		return dbId;
	}

	@SuppressWarnings("unchecked")
	public JSONArray prepareSubmission(Processing processing, Set<Long> dataElementIds) {
		JSONArray submits = new JSONArray();

		int appType = processing.getApplication().getType();
		
		for (Long dbId : dataElementIds) {
			JSONArray wrapper = new JSONArray();

			DataElement de = userDataService.getDataElement(dbId);

			// find matching inputs -> for tracula
			if (appType == 2) {   
	            String sessionUri = _getSessionUri(de.getURI());
	            
	            logger.debug("Finding Matching inputs; session URI determined as: " + sessionUri);
	            
	            for (IOPort port : processing.getApplication().getInputPorts()) {
	                if (!port.isVisible()) {    
	                	// ignore the visible ports that should be filled already by the UI (there should be one visible port, though)
	                	DataElement el = userDataService.getMatchingInput(sessionUri, port.getDataFormat());
	                	
	                    wrapper.add(_createSubmissionMap(port.getPortNumber(), el.getName(), el.getURI()));
	                }
	            }
			}
			
			int inputPortId = processing.getApplication().getVisibleInputPorts().iterator().next().getPortNumber();
			IOPort outputPort = processing.getApplication().getOutputPorts().iterator().next();
			HashMap<String, String> outputData = _getOutputString(de, processing, outputPort);
			
			// Input
			wrapper.add(_createSubmissionMap(inputPortId, de.getName(), de.getURI()));
			// Output
			wrapper.add(_createSubmissionMap(outputPort.getPortNumber(), outputData.get("name"), outputData.get("uri")));

			submits.add(wrapper);
		}
		
		return submits;
	}
	
	private String _getSessionUri(String dataElementUri) {
        final String sessionDescriptor = "/experiments/";
        
        int sessionIndex = dataElementUri.indexOf(sessionDescriptor) + sessionDescriptor.length();
        int endIndex = dataElementUri.indexOf("/", sessionIndex);
        
        if (sessionIndex == -1 || endIndex == -1) {
        	throw new IllegalArgumentException("No session information could be found in the provided URI.");
        }
        
        return dataElementUri.substring(0, endIndex);
	}

	private HashMap<String, Object> _createSubmissionMap(int portId, String name, String dataUri) {
		HashMap<String, Object> submit_map = new HashMap<String, Object>();

		submit_map.put("portId", portId);
		submit_map.put("name", name);
		submit_map.put("data", dataUri);

		return submit_map;
	}

	private HashMap<String, String> _getOutputString(DataElement dataElement, Processing processing, IOPort outputPort) {
		String reconString = null;
		String returnURI = null;
		String returnName = null;

		System.out.println(userDataService.getProject().getValueByName("xnat_project_id"));
		
		String xnatID = userDataService.getProject().getValueByName("xnat_project_id");
		String baseDataType = dataElement.getType();
		// unique id
		String subject = dataElement.getValueByName("xnat_subject_id");
		String scanID = dataElement.getValueByName("xnat_scan_id");
		String applicationName = processing.getApplication().getInternalName();
		String reconstructionType = outputPort.getDataFormat().replace(" ", "_");

		reconString = dataElement.getURI();

		if (reconString.indexOf("/scans/") > 0) {
			reconString = reconString.substring(0, reconString.indexOf("/scans/"));
			reconString = reconString.replaceAll("/data/experiments/", "/data/archive/projects/" + xnatID + "/subjects/" + subject + "/experiments/");
		} else if (reconString.indexOf("/reconstructions/") > 0) {
			reconString = reconString.substring(0, reconString.indexOf("/reconstructions/"));
		} else {
			return null;
		}
		
		returnURI = "base_string " + reconString.replace(" ", "_") + " xnat_project_id " + xnatID.replace(" ", "_") 
				+ " base_data_type " + baseDataType.replace(" ", "_") + " xnat_subject_id " + subject.replace(" ", "_") 
				+ " xnat_scan_id " + scanID.replace(" ", "_") + " application_name " + applicationName.replace(" ", "_")
				+ " reconstruction_type " + reconstructionType.replace(" ", "_");

		returnName = subject + ".Recon." + scanID + "." + reconstructionType;
		
		System.out.println("Output: " + returnURI);

		HashMap<String, String> returnVal = new HashMap<String, String>();
		
		returnVal.put("name", returnName.replace(" ", "_"));
		returnVal.put("uri", returnURI);
		
		return returnVal;
	}

	/**
	 * 
	 * @param processing
	 * @param userId
	 * @param liferayUserID
	 * @param refresh
	 *            processing status
	 * @return
	 */
	public DisplayProcessingStatus getProcessingStatus(Processing processing, Long userId, Long liferayUserID, boolean refresh) {
		logger.info("liferayUserID " + liferayUserID + "/Processing " + processing + "/ processing "
				+ ((processing != null) ? processing.getDbId() : "null"));
		DisplayProcessingStatus processingStatus = new DisplayProcessingStatus();
		processingStatus.setProcessing(processing);
		String status = "";
		try {
			// userDataService.closeSession();
			if (refresh) {
				// TODO processingManager.updateStatus(processing.getDbId(),
				// userDataService.getProcessingPersistance());
			}
			status = processing.getStatus();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			// userDataService.openSession();
		}
		processingStatus.setStatus(status);

		try {
			processingStatus.setSubmissionIOs(userDataService.getSubmissionIO(processing.getDbId()));
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return processingStatus;

	}

	public nl.amc.biolab.datamodel.objects.Error getSubmissionError(long submissionId) {
		return userDataService.getSubmissionError(submissionId);
	}

	/**
	 * @param processingDbId
	 * @param submissionDbId
	 * @param userId
	 * @param liferayUserID
	 */
	public void resubmit(long processingDbId, long submissionDbId, Long userId, Long liferayUserID) {
		try {
			// userDataService.closeSession();
			// TODO processingManager.resubmit(processingDbId, submissionDbId,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			// userDataService.openSession();
		}
	}

	public void resubmit(long processingDbId) {
		try {
			// userDataService.closeSession();
			// TODO processingManager.resubmit(processingDbId,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			// userDataService.openSession();
		}
	}

	/**
	 * 
	 * @param submissionDbId
	 * @param text
	 *            with fail remarks
	 */
	public void markFailed(long submissionDbId, String text) {
		try {
			// TODO processingManager.markFailed(submissionDbId, text,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
		}
	}

	public void markFailed(long processingId) {
		try {
			// TODO processingManager.markFailed(processingId,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void restart(long processingId) {
		try {
			// TODO processingManager.restart(processingId,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void shutdown() {
		// TODO processingManager.shutdown();
	}
	//
	// private String getStatus(Processing processing) {
	// HashMap<String, Integer> map = new HashMap();
	// for (Submission sub:processing.getSubmissions()) {
	// final String subStatus = sub.getStatus();
	// if (map.containsKey(subStatus)) {
	// map.put(subStatus, new Integer(map.get(subStatus)+1));
	// } else {
	// map.put(subStatus, new Integer(1));
	// }
	// }
	// StringBuffer statusSummary = new StringBuffer();
	// for (String aStatus : map.keySet()) {
	// statusSummary.append(map.get(aStatus)).append(" ").append(aStatus).append("; ");
	// }
	// final int length = statusSummary.length();
	// if (length<3) return "No Submissions";
	// return statusSummary.substring(0, length-2);
	// }
}
