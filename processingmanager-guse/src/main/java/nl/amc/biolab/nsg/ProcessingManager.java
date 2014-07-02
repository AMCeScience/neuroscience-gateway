/*
 * Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.


 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.amc.biolab.nsg;

import hu.sztaki.lpds.pgportal.services.asm.ASMService;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amc.biolab.Tools.PersistenceManager;
import static nl.amc.biolab.nsg.AsmHelper.callAsmRescue;
import nl.amc.biolab.nsg.emailer.NSGEmailer;
import nl.amc.biolab.nsg.errors.ErrorCode;
import nl.amc.biolab.nsg.errors.ErrorCode.During;
import nl.amc.biolab.nsg.errors.ErrorCode.Problem;
import nl.amc.biolab.nsg.errors.ProcessingManagerException;
import nl.amc.biolab.nsgdm.*;


/**
 *
 * @author mmajid
 */
public class ProcessingManager {
    
    // currently, take one thread to avoid possible race conditions on ASM side (one is for the waiting thread)
    private static final int submitPoolSize = 1; // TODO make configurable
    private static final int waitPoolSize = 2; // TODO make configurable
    private final ExecutorService submitPool = Executors.newFixedThreadPool(submitPoolSize);
    private final ExecutorService waitPool = Executors.newFixedThreadPool(waitPoolSize);
    protected static final ASMService asmService = ASMService.getInstance();
    private final NSGEmailer nsgEmailer;
    private final WorkflowStatusUpdater workflowStatusUpdater = new WorkflowStatusUpdater();
    
    {
        PersistenceManager pm = new PersistenceManager();
        pm.init();
        nsgEmailer = new NSGEmailer(pm.getAdminEmail(), pm.getEmailHost());
        pm.shutdown();
    }
   
    /**
     * This method should be called when the processing manager is not needed anymore,
     * otherwise the thread pools created for asynchronous workflow submission and sending
     * emails will not be closed. In a web application, a reasonable place might be in 
     * ServletContextListener.contextDestroyed() - see: http://stackoverflow.com/questions/12815614/
     */
    public void shutdown() {
        nsgEmailer.shutdown();
        submitPool.shutdown();
        waitPool.shutdown();
    }
    
    /**
     * Submits one/multiple instance(s) of the given application with the inputs provided.
     *
     * @param prjID A Long value representing the identifier of a project entity in the database, to which the selected data elements belong.
     * @param appID A Long value representing the identifier of an application entity in the database, that will be started in this method.
     * @param filesPerPorts Each element of this parameter is a set of files that form one submission, i.e., one file per port.
     * For example [[file1],[file2]] is for two submissions of an application with one port, while [[file1,file2]] is for one
     * submission of an application with two ports. The order of files should match with the order of ports as returned by the
     * catalog.
     * @param userID A Long value representing the identifier of a user entity in the database
     * @param liferayUserID String representation of the liferay user ID which is in fact an integer (needed to submit workflows using ASM)
     * @param description A String containing a description of the workflow given by the user. It will be stored in the database for this processing.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     * @return the ID of a new processing entity that is stored in the catalog, representing the current application submission(s)
     */
    public Long submit(final Long prjID, final Long appID, final List<? extends List<Long>> filesPerPorts, 
            final Long userID, final String liferayUserID, final String description, final PersistenceManager pm) {
        report("Started a new processing at "+new Date(), Level.INFO);
        // register the new "processing" in the catalog 
        final Long processingID = pm.getNewProcessingID(prjID, appID);
        final Application application = pm.getApplication(appID);
        final String appName = application.getInternalName();
        
        // store in catalog
        pm.storeProcessing(processingID, appName, description, userID, ProcessingStatus.STAT_INIT);
        final Processing proc = pm.getProcessing(processingID);
        report("Processing ID = " + processingID, Level.INFO);
        
        // submit the application instance(s) and store the submission(s) in the catalog        
//        final ProcessingStatus ps = new ProcessingStatus();
        final List<WorkflowSubmitter> submitters = new LinkedList();
//        if (pm.getSubmissionType(appID) == 1) { // TODO: change 1 to a constant
            int count = 0;
            for (List<Long> filesPerPort : filesPerPorts) {
                count++;
                String wfName = appName + "_NSG_" + processingID + count;
                Long sid = pm.storeSubmission(processingID, wfName, ProcessingStatus.STAT_INIT, Collections.singletonList(filesPerPort));
                report("Initial storage in Catalog: SubmissionName=" + wfName, Level.INFO);
                submitters.add(new WorkflowSubmitter(wfName, application, liferayUserID, filesPerPort, userID, sid, proc));
            }
            
//        } else {
//            // treat as a parameter sweep submission
//            String wfName = appName + "_NSG_" + processingID;
//            Long sid = pm.storeSubmission(processingID, wfName, ProcessingStatus.STAT_INIT, filesPerPorts);
//            report("Initial storage in Catalog: SubmissionName=" + wfName, Level.INFO);
//            try {
//                wfName = submitParameterSweepWorkflow(wfName, application, liferayUserID, filesPerPorts);
//                pm.storeSubmissionStatus(sid, ProcessingStatus.STAT_RUNNING, null);
//                pm.storeError(sid, ErrorCodeOld.getIntValue(ErrorCodeOld.NO_ERROR, ErrorCodeOld.SUBMISSION_TIME), ProcessingStatus.STAT_RUNNING, "Initial submission. Refresh status to see the latest gUSE status.");
//                pm.updateSubmissionName(sid, wfName);
//                report("Successful submission stored in catalog: SubmissionName=" + wfName+", submissionID="+sid, Level.INFO);
//            } catch (ProcessingManagerException se) {
//                pm.storeSubmissionStatus(sid, ProcessingStatus.STAT_ERROR, null);
//                pm.storeError(sid, ErrorCodeOld.getIntValue(se, ErrorCodeOld.SUBMISSION_TIME), se.getMessage(), se.getAdditionalInfo());
//                se.printStackTrace();  
//                hasError = true;
//                errorSummary.append(se.getMessage()).append(se.getAdditionalInfo()).append("\r\n");
//                report("Submission failure: SubmissionName=" + wfName+". Stored in catalog submissionID="+sid, Level.WARNING);
//            }
//        }
        // The status of the processing based on its individual submissions must be set automatically by the database
//        pm.storeProcessingStatus(processingID, ps.makeStatusSummary());
//                } catch (ProcessingManagerException se) {
//                    pm.storeSubmissionStatus(sid, ProcessingStatus.STAT_ERROR, null);
//                    pm.storeError(sid, ErrorCodeOld.getIntValue(se, ErrorCodeOld.SUBMISSION_TIME), se.getMessage(), se.getAdditionalInfo());
//                    se.printStackTrace();  
//                    final String msg = "Submission "+count+": "+se.getMessage()+": "+se.getAdditionalInfo();
//                    hasError = true;
//                    errorSummary.append(msg).append("\r\n");
//                    report(msg, Level.SEVERE);
//                    report("Submission failure: SubmissionName=" + wfName+". Stored in catalog submissionID="+sid, Level.WARNING);
//                }
            
            waitPool.execute(new SubmitAndWait(submitters, proc));
        return processingID;
    }
    
    protected String submitParameterSweepWorkflow(String wfName, Application app, String liferayUserID, List<List<Long>> filesPerPorts) {
        report("Should still implement parameter sweep workflow submission.", Level.SEVERE);
        // TODO  markFailed(sid, "Not yet implemented: submitting parameter sweep workflows. This is probably because of a misconfiguration in the catalog.");
        throw new ProcessingManagerException(new ErrorCode(Problem.In_Catalog_Configuration, During.Submission_Time), null, -1L, "Parameter-sweep workflows are not supported.");
    }
    
    /**
     * start a new processing based on a given processing
     * @param processingId
     * @param pm
     * @return 
     */
    public Long restart(Long processingId, PersistenceManager pm) {
        Processing proc = pm.getProcessing(processingId);
        Long prjId = proc.getProject().getDbId();
        Long appId = proc.getApplication().getDbId();
        ArrayList<ArrayList<Long>> files = new ArrayList<ArrayList<Long>>();
        for (Submission sub : proc.getSubmissions()) {
            files.add(getFileIDs(sub.getSubmissionIOs()));
        }
        User submitter = proc.getUsers().iterator().next();
        String liferayUserId = submitter.getLiferayID();
        Long catUserId = submitter.getDbId();
        return submit(prjId, appId, files, catUserId, liferayUserId, proc.getDescription()+" (Resubmission)" , pm);
    } 
    
    /**
     * Stops all submissions in progress and marks the status of them to Failed.
     * @param processingId A Long value representing a processing entity in the database. The 
     * status of all submissions related to this processing will be marked as failed.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void markFailed(Long processingId, PersistenceManager pm) { 
        report("Aborting the processing with ID: "+processingId, Level.INFO);
        for (Submission sub: pm.getProcessingSubmissions(processingId)) {
            markFailed(sub.getDbId(), "Cancelled by user.", pm);
        }
    }

    /**
     * When it is decided that a submission should be aborted (usually because it has faced an 
     * unrecoverable problem), this method can be called to change its status to Failed. Marking
     * one submission as failed does not incur sending an email to the user. S/He will be 
     * notified only when all submissions in a processing have finished (i.e., either failed or
     * successfully done). If the submission is still in progress, the associated workflow
     * will be aborted.
     * 
     * @param submissionID A Long value representing a submission entity in the database. The 
     * status of this submission will be marked as failed.
     * @param message A String containing a message that will be stored in the database along 
     * with this submission. This message should be shown to the user as the reason why the
     * submission has been aborted.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void markFailed(Long submissionID, String message, PersistenceManager pm) { 
//        pm.init();
        report("Marking submission '"+submissionID+"' as failed and updating the status summary.", Level.INFO);
        if (message == null || message.equals("")) message="Processing aborted. Contact the support team for more details."; 
        final long processingID = pm.getSubmission(submissionID).getProcessing().getDbId();
        Processing proc = pm.getProcessing(processingID);
        for (Submission instance : proc.getSubmissions()) {
            if (instance.getDbId() == submissionID) {
                // TODO: cancel if it was running?
                if (instance.getStatus().equals(ProcessingStatus.STAT_RUNNING)) {
                    try {
                        AsmHelper.abortWorkflow(proc.getUsers().iterator().next().getLiferayID(), instance.getName());
                    } catch (ProcessingManagerException e) {
                        // it may fail to abort for example if workflow has already finished?
                        report(e.getMessage(), Level.WARNING);    // even if aborting fails, it will continue
                    }
                }
                pm.storeSubmissionStatus(submissionID, ProcessingStatus.STAT_FAILED, null);
                pm.storeError(submissionID, new ErrorCode(Problem.None, During.None).getIntValue() , "Aborted by admin.", message);
            }
        }
//        pm.shutdown();
    }
    
    /**
     * This method can resubmit the submissions in a processing that are currently
     * "on hold" because of an error. It recalculates the status summary for the
     * processing afterwards. It does not, however, refresh the status of the
     * running submissions.
     * 
     * @param processingID A Long value representing a processing entity in the database, 
     * the submissions of which shall be resubmitted. 
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void resubmit(Long processingID, PersistenceManager pm){
//        pm.init();
        Processing proc = pm.getProcessing(processingID);
        final User user = proc.getUsers().iterator().next();
        String liferayUserID = user.getLiferayID();
        Long catUserID = user.getDbId();
        Collection<Submission> subs = proc.getSubmissions();
        int submissionCount = subs.size();
        report("Resubmit: Submission count = " + submissionCount+", liferayUserID="+liferayUserID+", catUSerID="+catUserID, Level.INFO);
        if (submissionCount == 0) {
            throw new IllegalArgumentException("No submissions found for processingID: " + processingID);
        }
        
        for (Submission instance : subs) {
            resubmit(proc, instance, catUserID, liferayUserID, pm);
        }
//        pm.shutdown();
    }
    
    
    /**
     * This method resubmits one submission and additionally recalculates the 
     * status summary for the containing processing. It does not, however, 
     * refresh the status of the running submissions.
     * 
     * @param processingID A Long value representing the processing entity in the database that contains the submission which shall be resubmitted.
     * @param submissionID A Long value representing the submission entity in the database that shall be resubmitted.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void resubmit(Long processingID, Long submissionID, PersistenceManager pm) {
//        pm.init();
        Processing proc = pm.getProcessing(processingID);
        final User user = proc.getUsers().iterator().next();
        String liferayUserID = user.getLiferayID();
        Long catUserID = user.getDbId();
        report("Resubmit: ProceesingID="+processingID+", submissionID="+submissionID+", liferayUserID="+liferayUserID+", catUSerID="+catUserID, Level.INFO);
        final Submission instance = pm.getSubmission(submissionID);
        resubmit (proc, instance, catUserID, liferayUserID, pm);
        // No need to send an email to admin upon failure at resubmisison time
//        pm.shutdown();
    }

    private void resubmit(Processing proc, Submission instance, Long catUserID, String liferayUserID, PersistenceManager pm) {
        String status = instance.getStatus();
        if (status.equals(ProcessingStatus.STAT_ERROR)) {
            final String submissionName = instance.getName();
            final long submissionID = instance.getDbId();
            final List<nl.amc.biolab.nsgdm.Error> errors = instance.getErrors();
            final int size = errors.size();
            During happenedAt;
            if (size <= 0)  {
                report("No error code found for "+submissionID+". Performing full resubmission.", Level.INFO);
                happenedAt = During.Submission_Time;
            } else { 
                happenedAt = ErrorCode.decode(errors.get(size-1).getCode()).getOccasion();
            }
            try {
                pm.storeSubmissionStatus(submissionID, ProcessingStatus.STAT_RESUME, null);
                switch (happenedAt) {
                    case Execution_Time:
                        pm.storeError(submissionID, new ErrorCode(Problem.None, happenedAt).getIntValue(), ProcessingStatus.STAT_RESUME, "");
                        report("The unsuccessful workflow " + submissionName + " is being rescued!", Level.INFO);
                        callAsmRescue(instance, liferayUserID, status, pm);
                        break;
                    case Upload_Time:
                        pm.storeError(submissionID, new ErrorCode(Problem.None, happenedAt).getIntValue(), ProcessingStatus.STAT_RESUME, "");
                        report("Trying again to upload outputs for " + submissionName, Level.INFO);
                        workflowStatusUpdater.callUploadOutputFiles(instance, catUserID, liferayUserID, pm);
                        break;
                    default:
                        report("Unknown occasion for error code: " + happenedAt.getDescription() + ". Resubmitting anyway.", Level.SEVERE);
                    // fall through to resubmitting from scratch
                    case Submission_Time:
                        // TODO: There is not enough nsgdm support for doing this
                        pm.storeError(submissionID, new ErrorCode(Problem.None, During.Submission_Time).getIntValue(), ProcessingStatus.STAT_RESUME, "");
                        report("Resubmission from scratch", Level.INFO);
                        List<Long> filesPerPort = getFileIDs(instance.getSubmissionIOs());
                        List<List<Long>> allFiles = Collections.singletonList(filesPerPort);    // TODO: In case of parameter sweep this should change
                        final Application app = proc.getApplication();
                        String wfName;
                        if (pm.getSubmissionType(app.getDbId()) == 1) { // TODO: change 1 to a constant
                            WorkflowSubmitter submitter = new WorkflowSubmitter(submissionName, app, liferayUserID, filesPerPort, catUserID, submissionID, proc);
                            waitPool.execute(new SubmitAndWait(Collections.singletonList(submitter), proc));
                        } else {
                            wfName = submitParameterSweepWorkflow(submissionName, app, liferayUserID, allFiles);// TODO: allFiles should be properly initialized
                        }
                        break;
                }
            } catch (ProcessingManagerException pe) {
                report(pe.getMessage(), Level.SEVERE);
                pm.storeSubmissionStatus(instance.getDbId(), ProcessingStatus.STAT_ERROR, null);
                pm.storeError(instance.getDbId(), pe.getErrorCode().getIntValue(), pe.getMessage(), "");
            }
        }
    }


    /**
     * update the status for all processings that are still running.
     * This is typically called as a service remotely.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void updateStatus(PersistenceManager pm){
        final Collection<Processing> prInProgress = pm.getProcessingsInProgress();
        for (Processing p : prInProgress) {
                report("In progress: '"+p.getDescription()+"' submitted by "+p.getUsers().iterator().next().getLastName(), Level.INFO);
        }
        for (Processing p : prInProgress) {
            try {
                report("Updating status of '"+p.getDescription()+"' submitted by "+p.getUsers().iterator().next().getLastName(), Level.INFO);
                updateProcessingStatus(p, p.getDbId(), pm);
            } catch (RuntimeException re) {
                report("Failed getting status for processing '"+p.getDescription()+"'. Continue to next processing.", Level.SEVERE);
            }
        }
    }

    /**
     * Updates the status of a given processing. It basically checks if the running
     * workflows have finished, and will transfer their output if they have finished.
     * The status of each submission is updated in the catalog, and the caller of 
     * this method should retrieve the new statuses from the catalog.
     * 
     * @param processingID A Long value representing the processing that must be updated.
     * @param pm An instance of PersistenceManager with an open session that can be used to store relevant information in the database
     */
    public void updateStatus(Long processingID, PersistenceManager pm) {
//        pm.init();
        // TODO: If no grid proxy, you get: Middleware not supported! Shouldn't be a problem if ran with robot. But might be a problem if similar message is reported for other reasons
        Processing proc = pm.getProcessing(processingID);
        updateProcessingStatus(proc, processingID, pm);
//        pm.shutdown();
    }

    /**
     * Produce logs with given message.
     * @param msg A String representing the message to be logged.
     * @param level Severity of the log message
     */
    static void report(String msg, Level level) {
        System.out.println("Processing Manager: " + msg);
        Logger.getLogger(ProcessingManager.class.getName()).log(level, msg);
    }

    private ArrayList<Long> getFileIDs(Collection<SubmissionIO> submissionInputs) {
        ArrayList<Long> files = new ArrayList();
        for (SubmissionIO subIO : submissionInputs) {
            files.add(subIO.getDataElement().getDbId());
        }
        return files;
    }

    private void updateProcessingStatus(Processing proc, Long processingID, PersistenceManager pm) throws IllegalArgumentException {
        final User user = proc.getUsers().iterator().next();
        Collection<Submission> subs = proc.getSubmissions();
        int submissionCount = subs.size();
        report("updateStatus: Submission count = " + submissionCount, Level.INFO);
        if (submissionCount == 0) {
            throw new IllegalArgumentException("No submissions found for processingID: " + processingID);
        }

        boolean hasFinished = true;
        boolean hasError = false;
        List<ProcessingManagerException> exceptions = new LinkedList();
        for (Submission instance : subs) {
            try {
                if (!workflowStatusUpdater.updateSubmissionStatus(instance, user, pm)) {
                    hasFinished = false;
                }
            } catch (ProcessingManagerException pe) {
                pm.storeSubmissionStatus(instance.getDbId(), ProcessingStatus.STAT_ERROR, null);
                pm.storeError(instance.getDbId(), pe.getErrorCode().getIntValue(), pe.getMessage(), "");
                hasError = true;
                hasFinished = false;
                exceptions.add(pe);
            }
        }
        if (hasFinished) {
            nsgEmailer.notifyUser(proc); 
        } else if (hasError) {
            nsgEmailer.sendToAdmin(exceptions);
        }
    }

    private class SubmitAndWait implements Runnable {

        private final List<WorkflowSubmitter> submitters;
        private final Processing processing;

        public SubmitAndWait(List<WorkflowSubmitter> submitters, Processing processing) {
            this.submitters = submitters;
            this.processing = processing;
        }

        @Override
        public void run() {
            List<ProcessingManagerException> exceptions = new LinkedList();
            boolean hasError = false;
            try {
                report("Submitting "+submitters.size()+" instances and waiting for the submission to complete.", Level.INFO);
                Iterator<WorkflowSubmitter> submissionIterator = submitters.iterator();
                for (Future<String> f : submitPool.invokeAll(submitters)) {
                    WorkflowSubmitter submissionWf = submissionIterator.next();
                    try {
                        if (f.get() == null) hasError = true;
                    } catch (ExecutionException ex) {
                        final Throwable cause = ex.getCause();
                        if (cause instanceof ProcessingManagerException) {
                            ProcessingManagerException pme = (ProcessingManagerException) cause;
                            exceptions.add(pme);
                        } else {
                            exceptions.add(new ProcessingManagerException(new ErrorCode(Problem.Unknown, During.Submission_Time), processing, submissionWf.submissionId, "Submission failed.", ex));
                        }
                        hasError = true;
                    }
                }
            } catch (InterruptedException ex) {
                exceptions.add(new ProcessingManagerException(new ErrorCode(Problem.Unknown, During.Submission_Time), processing, -1L, "Warning: Workflow submission thread was interrupted. Some submissions might have succeeded.", ex));
                hasError = true;
            } catch (RuntimeException ex) {
                exceptions.add(new ProcessingManagerException(new ErrorCode(Problem.Unknown, During.Submission_Time), processing, -1L, "Warning: Unknown error during workflow submission. Some submissions might have succeeded.", ex));
                hasError = true;
            }
            if (hasError) {
                report("Sending email to admin.", Level.INFO);
                nsgEmailer.sendToAdmin(exceptions);
            }
        }
    }

}
