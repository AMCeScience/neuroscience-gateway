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
import hu.sztaki.lpds.pgportal.services.asm.ASMWorkflow;
import hu.sztaki.lpds.pgportal.services.asm.constants.RepositoryItemTypeConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amc.biolab.Tools.PersistenceManager;
import static nl.amc.biolab.nsg.ProcessingManager.report;
import static nl.amc.biolab.nsg.WorkflowSubmitter.sep;
import nl.amc.biolab.nsg.errors.ErrorCode;
import nl.amc.biolab.nsg.errors.ErrorCode.During;
import nl.amc.biolab.nsg.errors.ErrorCode.Problem;
import nl.amc.biolab.nsg.errors.ProcessingManagerException;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.Submission;

/**
 *
 * @author m.jaghouri@amc.uva.nl
 */
public class AsmHelper {

    protected static final ASMService asmService = ASMService.getInstance();

    protected static void abortWorkflow (String liferayUserId, String submissionName) {
        String msg = "abort(" + liferayUserId + "," + submissionName + "): ";
        report(msg, Level.INFO);
        try {
            asmService.abort(liferayUserId, submissionName);
        } catch (Exception ex) {
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, null), null, -1L, msg, ex);
       }
    }
    
    protected static String asmImportWorkflow(String userID, String submissionName, Application app) {
        final String executable = app.getExecutable(); // workflow-developer ID in repository
        report("workflow and its owner: "+executable, Level.INFO);
        String[] wf_ownerID = executable.split(";");
        String wfID = wf_ownerID[0];
        String ownerID = wf_ownerID[1];
       // prepare for submission: import the application
        String impWfType = RepositoryItemTypeConstants.Application;
        report("ImportWorkflow(userID= "+userID+", submissionName= "+submissionName+", ownerID= "+ownerID+", wfID= "+wfID, Level.INFO);
        String selected_wf;
        try {
            selected_wf = asmService.ImportWorkflow(userID, submissionName, ownerID, impWfType, wfID);
        } catch (Exception ex) {
            String msg = "importWorkflow(" + userID + "," + submissionName + "," + ownerID + "," + wfID + ")";
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), null, -1L, msg, ex);
        }
        return selected_wf;
    }

    protected static void callAsmRescue(Submission instance, String liferayUserID, String prevStatus, PersistenceManager pm) {
        final long submissionID = instance.getDbId();
        String submissionName = instance.getName();
        try {
            report("calling asmRescue("+liferayUserID+", "+submissionName+")", Level.INFO);
            asmService.rescue(liferayUserID, submissionName);
            pm.storeSubmissionStatus(submissionID, ProcessingStatus.STAT_RUNNING, null);
            pm.storeError(submissionID, new ErrorCode(Problem.None, During.Submission_Time).getIntValue(), ProcessingStatus.STAT_RUNNING, "After asm rescue. Previous status was: "+prevStatus);
        } catch (Exception ex) {
            String msg = "Failed to rescue "+submissionName;
            report(msg+": "+ex.getMessage(), Level.SEVERE);
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), instance.getProcessing(), instance.getDbId(), msg, ex);
//            pm.storeSubmissionStatus(submissionID, ProcessingStatus.STAT_ERROR, null);  
        }
    }

    protected static void setWorkingDir(String baseDir, String userID, String selected_wf, IOPort inputPort) {
        String[] in_job_port = inputPort.getPortName().split("@");
        String selected_in_job = in_job_port[1];
        String selected_in_port = "3"; // port number for output dir
        try {
            String outputDir = baseDir+"/"+selected_in_job;
            report("setInputValue(outputDir="+outputDir+", userID="+userID+", wf="+selected_wf+", job="+selected_in_job+", port="+selected_in_port+")", Level.INFO);
            asmService.setInputValue(outputDir, userID, selected_wf, selected_in_job, selected_in_port);
        } catch (Exception ex) {
            final String message = "setting working dir failed";
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), null, -1L, message, ex);
        }
        
    }
    
    protected static String setOutputLocation(IOPort outport, String baseURI, String userID, String selected_wf) {
        final String portName = outport.getPortName();
        String[] out_job_port = portName.split("@");
        String selected_out_job = out_job_port[1];
        String selected_out_port = out_job_port[0];
        final Resource out_resource = outport.getResource();
        String outputURI = baseURI +sep+ selected_out_job +sep+ selected_out_port; 
        report("setRemoteOutputPath(userID= " + userID+", wf= " + selected_wf+", job= " + selected_out_job+", port= " + selected_out_port+", outputURI= " + outputURI+")", Level.INFO);
        try {
            asmService.setRemoteOutputPath(userID, selected_wf, selected_out_job, selected_out_port, outputURI);
        } catch (Exception ex) {
            String msg = "setRemoteOutputPath(" + userID+"," + selected_wf+"," + selected_out_job+"," + selected_out_port+"," + outputURI+")";
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), null, -1L, msg, ex);
        }
        return outputURI;
    }

    static void setInputPath(IOPort port, String userID, String selected_wf, String inputURI) throws ProcessingManagerException {
        String[] job_port = port.getPortName().split("@");
        String selected_job = job_port[1];
        String selected_port = job_port[0];
        report("setRemoteInputPath(userID= " + userID + ", selected_wf= " + selected_wf + ", selected_job= " + selected_job + ", selected_port= " + selected_port + ", inputURI= " + inputURI, Level.INFO);
        try {
            asmService.setRemoteInputPath(userID, selected_wf, selected_job, selected_port, inputURI);
        } catch (Exception ex) {
            String msg = "setRemoteInputPath(" + userID + "," + selected_wf + "," + selected_job + "," + selected_port + "," + inputURI + ")";
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), null, -1L, msg, ex);
        }
    }


    static void asmSubmit(String selected_wf, String userId, String submissionName) throws ProcessingManagerException {
        // submit a workflow per batch
        try {
            report("Trying to submit: " + userId + " " + selected_wf + " " + submissionName, Level.INFO);
            asmService.submit(userId, selected_wf, submissionName, "");
        } catch (Exception ex) {
            String msg = "submit(" + userId + "," + selected_wf + "," + submissionName + ")";
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Submission_Time), null, -1L, msg, ex);
        }
    }
    
    
    static String getOutputPath(String liferayUserID, String submissionName, String selected_job, String selected_port) throws ProcessingManagerException {
        String portURI;
        final String msg1 = "getRemoteOutputPath("+liferayUserID+", "+submissionName+", "+selected_job+", "+selected_port+")";
        try {
            report("calling "+msg1, Level.INFO);
            portURI = asmService.getRemoteOutputPath(liferayUserID, submissionName, selected_job, selected_port);
        } catch (Exception ex) {
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Upload_Time), null, -1L, msg1, ex);
        }
        return portURI;
    }
    
    static String getWorkflowStatus(String liferayUserID, String submissionName) {
        final String msg = "getWorkflowStatus("+liferayUserID+", "+submissionName+").getStatus()";
        report(msg, Level.INFO);
        try {
            return asmService.getWorkflowStatus(liferayUserID, submissionName).getStatus();
        } catch (Exception ex) {
            throw new ProcessingManagerException(new ErrorCode(Problem.In_ASM_Call, During.Execution_Time), null, -1L, msg, ex);
        }
    }


    // TODO
/*    private void fetchLogs(String liferayUserID, String submissionName) {
        final WorkflowInstanceBean details = asmService.getDetails(liferayUserID, submissionName);
        for (RunningJobDetailsBean job : details.getJobs()) {
            for (ASMJobInstanceBean jobInstance : job.getInstances()) {
                Transfer.localTransfer(jobInstance.getStdErrorPath(), wwwPath+submissionName+"/"+job.getName()+"_"+jobInstance.getJobId());
                Transfer.localTransfer(jobInstance.getStdOutputPath(), wwwPath+submissionName+"/"+job.getName()+"_"+jobInstance.getJobId());
                Transfer.localTransfer(jobInstance.getLogBookPath(), wwwPath+submissionName+"/"+job.getName()+"_"+jobInstance.getJobId());
            }
        }
    }
    
    private String getFailedJobPath(String liferayUserID, String submissionName) {
        String errorPath = "";
        try {
        report("Calling asmService.getDetails("+liferayUserID+", "+submissionName+")", Level.INFO);
        final WorkflowInstanceBean details = asmService.getDetails(liferayUserID, submissionName);
        for (RunningJobDetailsBean job : details.getJobs()) {
//            if (0 < job.getStatisticsBean().getNumberOfJobsInError()) {
                for (ASMJobInstanceBean jobInstance : job.getInstances()) {
                    report("Status of "+jobInstance.getJobId()+"."+jobInstance.getId()+" is: "+jobInstance.getStatus(), Level.INFO);
                    if (jobInstance.getStatus().equals(StatusConstants.ERROR)) // if asm status is error
                        errorPath += jobInstance.getStdErrorPath() + "\r\n";
                }
//            }
        }
        } catch (Throwable ex) {
            throw new ProcessingManagerException(ErrorCodeOld.ASM_ERROR, ex.getMessage(), ex);
        }
        return errorPath;
    }*/

    private void cleanup (String liferayUserID) {
        report("clean up some wrokflows", Level.INFO);
        for (ASMWorkflow wf : asmService.getASMWorkflows(liferayUserID)) {
            final String workflowName = wf.getWorkflowName();
            if (workflowName.contains("wf")) {  // which workflows to clean up
                report("removing name: "+workflowName, Level.INFO);
                asmService.DeleteWorkflow(liferayUserID, workflowName);
            }
        }
    }
        
}
