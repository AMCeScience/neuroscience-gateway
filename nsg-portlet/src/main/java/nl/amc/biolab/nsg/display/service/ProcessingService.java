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

import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.nsg.ProcessingManager;
import nl.amc.biolab.nsg.display.data.DisplayProcessingStatus;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class ProcessingService {
	Logger logger = Logger.getLogger(ProcessingService.class);

	protected UserDataService userDataService;
	protected ProcessingManager processingManager = new ProcessingManager();

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
	public Long submit(Long prjID, Long appID, int appType,
			ArrayList<ArrayList<Long>> filesPerPorts, Long catUserID,
			Long liferayUserID, String description) {
		Long dbId = null;
		try {
			// userDataService.closeSession();
			logger.debug("call processingManager submit appID " + appID
					+ " nr ports " + filesPerPorts.size() + " nr port 0 input "
					+ filesPerPorts.get(0).size());
			// TODO dbId = processingManager.submit(prjID, appID, filesPerPorts,
			// catUserID, Long.toString(liferayUserID), description,
			// userDataService.getProcessingPersistance());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			// userDataService.openSession();
		}
		return dbId;
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
	public DisplayProcessingStatus getProcessingStatus(Processing processing,
			Long userId, Long liferayUserID, boolean refresh) {
		logger.info("liferayUserID " + liferayUserID + "/Processing "
				+ processing + "/ processing "
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
			processingStatus.setSubmissionIOs(userDataService
					.getSubmissionIO(processing.getDbId()));
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return processingStatus;

	}

	public nl.amc.biolab.datamodel.objects.Error getSubmissionError(
			long submissionId) {
		return userDataService.getSubmissionError(submissionId);
	}

	/**
	 * @param processingDbId
	 * @param submissionDbId
	 * @param userId
	 * @param liferayUserID
	 */
	public void resubmit(long processingDbId, long submissionDbId, Long userId,
			Long liferayUserID) {
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
		processingManager.shutdown();
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
