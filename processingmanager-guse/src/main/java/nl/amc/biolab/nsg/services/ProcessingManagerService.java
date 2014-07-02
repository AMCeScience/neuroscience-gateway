package nl.amc.biolab.nsg.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import nl.amc.biolab.Tools.PersistenceManager;
import nl.amc.biolab.nsg.ProcessingManager;

/**
 *
 * @author m.jaghouri@amc.uva.nl
 */
@WebService(serviceName = "ProcessingManagerService")
public class ProcessingManagerService {
    ProcessingManager prm = new ProcessingManager();    // TODO: See where to shut it down?!

    @WebMethod(operationName = "refreshAll")
    @Oneway
    public void refreshAll() {
        System.out.println(new Date().toString()+" Refreshing the status of all ongoing processings and submissions.");
        PersistenceManager pm = new PersistenceManager();
        pm.init();
        prm.updateStatus(pm);
        pm.shutdown();
   }
    
    /**
     * Updates the status of a given processing. It basically checks if the running
     * workflows have finished, and will transfer their output if they have finished.
     * The status of each submission is updated in the catalog, and the caller of 
     * this method should retrieve the new statuses from the catalog.
     * 
     * @param processingID A Long value representing the processing that must be updated.
     */
    @WebMethod(operationName = "updateStatus")
    @Oneway
    public void updateStatus(@WebParam(name = "processingID") Long processingID) {
        System.out.println(new Date().toString()+" Refreshing the status of a processing: "+processingID);
        PersistenceManager pm = new PersistenceManager();
        pm.init();
        prm.updateStatus(processingID, pm);
        pm.shutdown();
   }
    
    /**
     * Submits one/multiple instance(s) of the given application with the inputs provided.
     *
     * @param prjID A Long value representing the identifier of a project entity in the database, to which the selected data elements belong.
     * @param appID A Long value representing the identifier of an application entity in the database, that will be started in this method.
     * @param filesPerPorts This is a list of file ID's that will be assigned to the ports of the given application. If there are more than 
     * one submission considered, the lists of file ID's should come one after the other. For example, if the selected application has 2 input 
     * ports and 4 files are provided here, there will be 2 submission considered.
     * @param userID A Long value representing the identifier of a user entity in the database
     * @param liferayUserID String representation of the liferay user ID which is in fact an integer (needed to submit workflows using ASM)
     * @param description A String containing a description of the workflow given by the user. It will be stored in the database for this processing.
     */
    @WebMethod(operationName = "submit")
    @Oneway
    public void submit(
            @WebParam(name="prjID") final Long prjID, 
            @WebParam(name="appID") final Long appID, 
            @WebParam(name="filesPerPorts") final Long[] filesPerPorts, 
            @WebParam(name="userID") final Long userID, 
            @WebParam(name="liferayUserID") final String liferayUserID, 
            @WebParam(name="description") final String description) {
        System.out.print("Received a request to submit");
        System.out.print(" prjID: "+prjID);
        System.out.print(" appID: "+appID);
        System.out.print(" files: (");
        for (int i=0; i<filesPerPorts.length; i++) {
            System.out.print(filesPerPorts[i]+" ");
        }
        System.out.print(") userID: "+userID);
        System.out.print(" liferayUserID: "+liferayUserID);
        System.out.println(" description: "+description);
        PersistenceManager pm = new PersistenceManager();
        pm.init();        
        int portCount = pm.getApplicationInputPorts(appID).size();
        System.out.println("Application has "+portCount+" ports and number of files submitted are "+filesPerPorts.length);
        List<List<Long>> files = new ArrayList();
        int fileCounter = 0;
        for (;;) {
            List<Long> perPort = new ArrayList(portCount);
            for (int i=0; i<portCount; i++) {
                if (fileCounter > filesPerPorts.length)
                    throw new IllegalArgumentException("Invalid number of inputs for application "+appID);
                perPort.add(filesPerPorts[fileCounter++]);
            }
            files.add(perPort);
            if (fileCounter == filesPerPorts.length) break;
        }        
        prm.submit(prjID, appID, files, userID, liferayUserID, description, pm);
        pm.shutdown();
    }
}
