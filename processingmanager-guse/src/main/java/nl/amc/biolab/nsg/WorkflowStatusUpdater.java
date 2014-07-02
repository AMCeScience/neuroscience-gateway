/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amc.biolab.nsg;

import hu.sztaki.lpds.pgportal.services.asm.constants.StatusConstants;
import java.util.Collection;
import java.util.logging.Level;
import nl.amc.biolab.Tools.PersistenceManager;
import static nl.amc.biolab.nsg.AsmHelper.callAsmRescue;
import static nl.amc.biolab.nsg.AsmHelper.getOutputPath;
import static nl.amc.biolab.nsg.AsmHelper.getWorkflowStatus;
import nl.amc.biolab.nsg.GenericTransfer.Protocol;
import static nl.amc.biolab.nsg.ProcessingManager.report;
import nl.amc.biolab.nsg.errors.ErrorCode;
import nl.amc.biolab.nsg.errors.ErrorCode.During;
import nl.amc.biolab.nsg.errors.ErrorCode.Problem;
import nl.amc.biolab.nsg.errors.ProcessingManagerException;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.Submission;
import nl.amc.biolab.nsgdm.User;

/**
 *
 * @author m.jaghouri@amc.uva.nl
 */
public class WorkflowStatusUpdater {
    private final static StatusConstants statC = new StatusConstants();

    /**
     * The caller has to catch ProcessingManagerException and store the error if it happens
     * 
     * @param instance
     * @param catUserID
     * @param liferayUserID
     * @param pm 
     */
    void callUploadOutputFiles(Submission instance, Long catUserID, String liferayUserID, PersistenceManager pm) {
        final long submissionID = instance.getDbId();
        try {
            uploadOutputFiles(instance, catUserID, liferayUserID, pm);
            pm.storeSubmissionStatus(submissionID, ProcessingStatus.STAT_FINISHED, null);
        } catch (RuntimeException re) {
            if (re instanceof ProcessingManagerException) {
                throw ((ProcessingManagerException)re).addOccasion(During.Upload_Time);
            }
            throw new ProcessingManagerException(new ErrorCode(Problem.In_XNAT_Upload, During.Upload_Time), instance.getProcessing(), instance.getDbId(), "Upload failed.", re);
        }
    }

    private void uploadOutputFiles(Submission instance, Long userID, String liferayUserID, PersistenceManager pm) {
        String submissionName = instance.getName();
        final Collection<IOPort> outputPorts = pm.getApplicationOutputPorts(instance.getProcessing().getApplication().getDbId());
        for (IOPort outPort : outputPorts) {
            // TODO: Change this check on visibility to a check on output port type
            if (!outPort.isVisible()) continue;
            // get the external location of outputs
            final String portName = outPort.getPortName();
            String[] job_port = portName.split("@");
            String selected_job = job_port[1];
            String selected_port = job_port[0];
            // asmService.getASMWorkflow(liferayUserID, submissionName);
            String portURI = getOutputPath(liferayUserID, submissionName, selected_job, selected_port);
            // get credentials for XNAT and move the file from Grid using robot proxy 
            
            // TODO: handle "generator" outputs, i.e., more than one file on the same output
            // gUse always adds _0 to remote outputs 
            portURI += "_0";
            String xnatURI = pm.getMasterOutputURI(instance.getDbId(), userID, outPort);
            if (xnatURI == null) {
                final String msg = "No XNAT URI could be made. The call to getMasterOutputURI returned null.";
                report(msg, Level.INFO);
                throw new ProcessingManagerException(new ErrorCode(Problem.In_XNAT_Upload, During.Upload_Time),instance.getProcessing(), instance.getDbId() , msg);
            }

            // get the resource associated to an input of this submission and use it as the master resource in order to move the outputs to it.
            final Resource masterResource = pm.getSubmissionInputs(instance.getDbId()).iterator().next().getDataElement().getResource();  
            final Resource srcResource = outPort.getResource(); 

            Credentials srcCred = GenericTransfer.getCredentials(userID, srcResource, pm);
            Credentials dstCred = GenericTransfer.getCredentials(userID, masterResource, pm);
            
            
            report ("Transfering output from "+portURI+ " to "+xnatURI, Level.INFO);
            // TODO: valueOf() throws IllegalArgumentsException
            GenericTransfer.copyFile(Protocol.valueOf(srcResource.getProtocol()), portURI, srcCred, 
                                     Protocol.valueOf(masterResource.getProtocol()), xnatURI, dstCred);
            
            // store in catalog the availability of the files on XNAT and external resource
            Long dataID = pm.storeSubmissionOutput(instance.getDbId(), outPort.getDbId(), xnatURI);
            pm.storeReplica(dataID, outPort.getResource().getDbId(), portURI);
        }
        // Mark in the catalog that the outputs for this submission have successfully been transferred to XNAT
        pm.setSubmissionResults(instance.getDbId(), true); // TOOD: What if this fails
//        return ProcessingStatus.STAT_FINISHED;
    }

    /**
     * 
     * @param instance
     * @param user
     * @param dstCred
     * @param pm
     * @return true if the submission has completed (whether successful or not), false otherwise
     * @throws ProcessingManagerException if something goes wrong while updating the status or if the submission has finished with error
     */
    boolean updateSubmissionStatus(Submission instance, final User user, PersistenceManager pm) throws ProcessingManagerException {
        String liferayUserID = user.getLiferayID();
        final String prevStatus = instance.getStatus();
        report("Previous status for " + instance.getName() + " is " + prevStatus, Level.INFO);
        if (prevStatus.equals(ProcessingStatus.STAT_FINISHED) || prevStatus.equals(ProcessingStatus.STAT_FAILED)) {
            return true;
        } else if (!prevStatus.equals(ProcessingStatus.STAT_RUNNING)) {
            return false;
        } // covers all errors before becoming FAILED (backward compatibility: we used to store error logs into status)
        // update only if running?
        final String submissionName = instance.getName();
        String guseStatus = ""; //, subStatus =  ""; //ProcessingStatus.STAT_UNKNOWN;    // unknown status is set to ERROR
        final long submissionId = instance.getDbId();
        boolean completed = false;
        try {
            report("Calling asmService.getWorkflowStatus(" + liferayUserID + "," + submissionName + ")", Level.INFO);
            guseStatus = getWorkflowStatus(liferayUserID, submissionName);
            report("asmService.getWorkflowStatus(" + liferayUserID + "," + submissionName + ") returned: " + guseStatus, Level.INFO);
            if (guseStatus.equals(statC.getStatus(StatusConstants.FINISHED))) {
                callUploadOutputFiles(instance, user.getDbId(), liferayUserID, pm);  // additionally stores the status into catalog
                // TODO: Move the stdout and stderr and other logs
                // asmService.DeleteWorkflow(liferayUserID, submissionName);   // TODO: double check if it is safe to delete here!
                completed = true;
            } else if (guseStatus.equals(statC.getStatus(StatusConstants.WORKFLOW_SUSPENDING))) {
                report("Suspended workflow " + submissionName + " is being rescued!", Level.INFO);
                callAsmRescue(instance, liferayUserID, guseStatus, pm);
            } else if (guseStatus.equals(statC.getStatus(StatusConstants.ERROR))) {
//                pm.storeSubmissionStatus(submissionId, ProcessingStatus.STAT_ERROR, null);
//                String errorPaths = getFailedJobPath(liferayUserID, submissionName); TODO
                // TODO which log directory?
                String logMsg = /*"Check error logs at "+*/ pm.getGuseOutputPath() + liferayUserID + "/" + submissionName;
//                logMsg += "; or at "+errorPaths;
                throw new ProcessingManagerException(new ErrorCode(Problem.In_Workflow_Execution, During.Execution_Time), instance.getProcessing(), instance.getDbId(), logMsg);
//                ps.incError("Submission " + instance.getName() + ": " + ErrorCodeOld.getMessage(ErrorCodeOld.WORKFLOW_ERROR) + ": " + logMsg);
                // TODO: fetchErrorLogs(liferayUserID, submissionName);
            } else {
                pm.storeSubmissionStatus(submissionId, ProcessingStatus.STAT_RUNNING, null);
                // TODO check if the same status is already stored.
                pm.storeError(submissionId, new ErrorCode(Problem.None, During.Execution_Time).getIntValue(), ProcessingStatus.STAT_RUNNING, "gUSE returned: " + guseStatus);
            }
        } catch (ProcessingManagerException pe) {
            throw pe;
        } catch (Exception ex) {
            report("Failed to get workflow status for '" + instance.getProcessing().getDescription() + "': " + instance.getName() + " because: ", Level.INFO);
            ex.printStackTrace();
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Execution_Time), instance.getProcessing(), instance.getDbId(), "Failed to get workflow status", ex);
//            ps.incError("Failed to get workflow status for '" + instance.getProcessing().getDescription() + "': " + instance.getName());
        }
        return completed;
    }

}
