/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amc.biolab.nsg;

import java.sql.Blob;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import nl.amc.biolab.Tools.PersistenceManager;
import static nl.amc.biolab.nsg.AsmHelper.asmImportWorkflow;
import static nl.amc.biolab.nsg.AsmHelper.asmSubmit;
import static nl.amc.biolab.nsg.AsmHelper.setInputPath;
import static nl.amc.biolab.nsg.AsmHelper.setOutputLocation;
import static nl.amc.biolab.nsg.AsmHelper.setWorkingDir;
import nl.amc.biolab.nsg.GenericTransfer.Protocol;
import static nl.amc.biolab.nsg.ProcessingManager.report;
import nl.amc.biolab.nsg.errors.ErrorCode;
import nl.amc.biolab.nsg.errors.ErrorCode.During;
import nl.amc.biolab.nsg.errors.ErrorCode.Problem;
import nl.amc.biolab.nsg.errors.ProcessingManagerException;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.IOPort;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Resource;

/**
 *
 * @author m.jaghouri@amc.uva.nl
 */
public class WorkflowSubmitter implements Callable<String> {
    protected static final String sep = "/";   // TODO: chose resource-aware separators instead of "/"

    final String submissionName;
    final Application app;
    final String liferayUserID;
    final List<Long> filesPerPort;
    final Long userID;
    final PersistenceManager pm = new PersistenceManager();
    final Long submissionId;
    final Processing proc;

    /**
     *
     * @param submissionName
     * @param app
     * @param liferayUserID
     * @param filesPerPort
     * @param userID
     * @param submissionId
     */
    public WorkflowSubmitter(String submissionName, Application app, String liferayUserID, 
            List<Long> filesPerPort, Long userID, Long submissionId, Processing proc) {
        this.submissionName = submissionName;
        this.app = app;
        this.liferayUserID = liferayUserID;
        this.filesPerPort = filesPerPort;
        this.userID = userID;
        this.submissionId = submissionId;
        this.proc = proc;
    }

    /**
     * The input is taken from the master resource, e.g., in case of NSG, it is 
     * XNAT. Then, it is transfered to the resource associated to the corresponding
     * input port. 
     * @param inputFileID
     * @param destination
     * @param pm
     * @return the URI of the uploaded input on destination resource
     */
    private String uploadInput(Long inputFileID, Resource destination, PersistenceManager pm){
        final Resource srcResource = pm.getDataElement(inputFileID).getResource();
        String inputDestURI = pm.getReplicaURI(inputFileID, destination.getDbId());
        // check if a replica is registered in catalog and if the replica actually exists 
        // in case of grid, the replica is brought online
        if (inputDestURI == null || !GenericTransfer.isAvailable(inputDestURI, Protocol.valueOf(destination.getProtocol()))) {
            inputDestURI = makeInputURI(destination, inputFileID);
            Credentials srcCred = GenericTransfer.getCredentials(userID, srcResource, pm);
            Credentials dstCred = GenericTransfer.getCredentials(userID, destination, pm);
            // TODO: valueOf() throws IllegalArugumentException
            GenericTransfer.copyFile(Protocol.valueOf(srcResource.getProtocol()), pm.getMasterURI(inputFileID), srcCred,
                                     Protocol.valueOf(destination.getProtocol()), inputDestURI, dstCred);
            pm.storeReplica(inputFileID, destination.getDbId(), inputDestURI);
            report("A new replica for file " + inputFileID + " is stored on: " + inputDestURI, Level.INFO);
        }
        return inputDestURI;
    }

    private String getOutputBaseURI(Resource resource) {
        return resource.getBaseURI()+sep+"outputs"+sep; 
    }

    private String makeInputURI(Resource resource, long fileID) {
        return resource.getBaseURI()+sep+"inputs"+sep+fileID;
    }

    @Override
    public String call() throws Exception {
        try {
            pm.init();
//            if (pm.getSubmissionStatuses(submissionId).) if (status == failed) do not submit actually
            final String result = submitWorkflow();
            return result;
        } catch (RuntimeException re) {
            re.printStackTrace();
            if (! (re instanceof ProcessingManagerException)) {
                re = new ProcessingManagerException(new ErrorCode(Problem.Unknown, During.Submission_Time), proc, submissionId, "Submission failure.", re);
            }
            ProcessingManagerException pe = (ProcessingManagerException)re;
            pe.addOccasion(During.Submission_Time);
            pm.storeSubmissionStatus(submissionId, ProcessingStatus.STAT_ERROR, null);
            pm.storeError(submissionId, pe.getErrorCode().getIntValue(), pe.getMessage(), "");
            final String msg = "Submission (" +submissionId + ", " + submissionName + "): " + pe.getMessage();
            report(msg, Level.SEVERE);
            throw pe;
        } finally {
            pm.shutdown();
        }
    }

    private String submitWorkflow(){
        Long appID = app.getDbId();
        final Collection<IOPort> inputPorts = pm.getApplicationInputPorts(appID);
        final Collection<IOPort> outputPorts = pm.getApplicationOutputPorts(appID);
        // we assume all input data comes from the same resource, in this case XNAT
        // TODO: allow other types of sources
//        Credentials srcCred = getXnatCredentials(userID, pm);

        // prepare for submission: import the application
        String selected_wf = asmImportWorkflow(liferayUserID, submissionName, app);

        // set up the output locations
        for (IOPort outport : outputPorts) {
            setOutputLocation(outport, getOutputBaseURI(outport.getResource()) + submissionName, liferayUserID, selected_wf);
        }

        Iterator<Long> fileIdIterator = filesPerPort.iterator();
        for (IOPort port : inputPorts) {
            final String portType = port.getDataType();
            if (portType.equals("File")) {
                if (!fileIdIterator.hasNext()) {
                    final String msg = "Not enough files selected. No file associated to port '" + port.getPortName() + "' of '" + app.getName() + "'.";
                    report(msg, Level.SEVERE);
                    throw new ProcessingManagerException(new ErrorCode(Problem.In_Catalog_Configuration, During.Submission_Time), proc , submissionId, msg);
                }
                Long fileID = fileIdIterator.next();
                String inputDestURI = uploadInput(fileID, port.getResource(), pm);
                setInputPath(port, liferayUserID, selected_wf, inputDestURI);        
            } else if (portType.equals("outputDir")) {
                setWorkingDir(getOutputBaseURI(port.getResource()) + submissionName, liferayUserID, selected_wf, port);
            } else {
                final String msg = "Invaild port type '" + portType + "' stored in catalog for '" + app.getName() + "'";
                report(msg, Level.SEVERE);
                throw new ProcessingManagerException(new ErrorCode(Problem.In_Catalog_Configuration, During.Submission_Time), proc , submissionId, msg);
            }
        }
        asmSubmit(selected_wf, liferayUserID, submissionName);
        pm.storeSubmissionStatus(submissionId, ProcessingStatus.STAT_RUNNING, null);
        pm.storeError(submissionId, new ErrorCode(Problem.None, During.Submission_Time).getIntValue(), ProcessingStatus.STAT_RUNNING, "Initial submission. Refresh status to see the latest gUSE status.");
        pm.updateSubmissionName(submissionId, selected_wf);
        report("Submitted to ASM: wf=" + selected_wf + "; submissionName=" + submissionName, Level.INFO);
        return selected_wf;
    }
}
