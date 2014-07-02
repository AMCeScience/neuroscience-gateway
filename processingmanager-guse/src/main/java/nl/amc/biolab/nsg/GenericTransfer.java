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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import nl.amc.biolab.Tools.PersistenceManager;
import static nl.amc.biolab.nsg.ProcessingManager.report;
import nl.amc.biolab.nsg.errors.*;
import nl.amc.biolab.nsg.errors.ErrorCode.Problem;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.UserAuthentication;
import nl.amc.biolab.util.streams.StreamGobbler;

/**
 *
 * @author mmajid
 */
public class GenericTransfer {
    static List<String> errTxt, output;
    
    public enum Protocol { http, lcg, scp };
    
    // Download scripts
    private static String DOWN_GRID = "/home/guse/nsgShellScripts/downloadFromGrid.sh";
    private static String DOWN_XNAT = "/home/guse/nsgShellScripts/downloadFromXnat.sh";
    private static String DOWN_HTTP = "/home/guse/nsgShellScripts/httpDownload.sh";
    private static String DOWN_SCP  = "/home/guse/nsgShellScripts/scpDownload.sh";
    // upload scripts
    private static String UP_GRID = "/home/guse/nsgShellScripts/upload2Grid.sh";
    private static String UP_HTTP = "/home/guse/nsgShellScripts/httpUpload.sh";
    private static String UP_SCP  = "/home/guse/nsgShellScripts/scpUpload.sh";
    private static String UP_XNAT = "/home/guse/nsgShellScripts/upload2Xnat.sh";
    // check and bring online
    private static String CHECK_GRID = "/home/guse/nsgShellScripts/checkAndBringOnline.sh";

    private static String createTempFileName() {
        return "nsg"+UUID.randomUUID();
    }
    
    
    /**
     * Checks if the given URI is indeed available on the given resource
     * @param inputURI A String pointing to a file on the given resource
     * @param resource The Resource on which the file must exist
     * @return 
     */
    static boolean isAvailable(String inputURI, Protocol protocol) {
        List<String> command = new ArrayList<String>();
        switch (protocol) {
            case lcg:  command.add(CHECK_GRID); break;
            case http: return true; // TODO
            case scp:  return true; // TODO
        }
        command.add(inputURI);
        String message = "I am going to fire this command: "+ command;
        report(message, Level.INFO);
        int exitCode = fireCommand(command);
        if (exitCode == 0) {
            return true;
        } else if (exitCode == 404) {
            return false;
        } else {
            int index = errTxt.size()-1;    // the last error message is usually "invalid argument"
            // the useful information is located in different parts of the log
            if (protocol == Protocol.lcg && index >= 2) index -= 2;
            if (protocol == Protocol.http && index >= 1) index -= 1;
            throw new ProcessingManagerException(new ErrorCode(Problem.In_File_Transfer, null), null, -1L, errTxt.get(index));
        }
    }
    
    /**
     * Copies a file from/to a remote server to/from local machine. 
     * @param protocol The file transfer protocol that is supported by the remote resource.
     * @param srcUri A String pointing to the source file.
     * @param dstUri A String showing where the file should be copied to.
     * @param remoteCreds The credentials for accessing the remote resource.
     * @param upload If this boolean is true, this method will upload the local 
     * source file to the designated remote server. If it is false, this method
     * will download the source file from the remote server onto local machine.
     */
    public static void copyFile(Protocol protocol, String srcUri, String dstUri, Credentials remoteCreds, boolean upload) {
        List<String> command = new ArrayList<String>();
        ErrorCode errorCode = new ErrorCode(Problem.In_File_Transfer, null);
        switch (protocol) {
            case http: 
                command.add(upload ? UP_HTTP : DOWN_HTTP); 
                errorCode.setProblem(upload ? Problem.In_HTTP_Upload : Problem.In_HTTP_Download);
                break;
            case lcg:  
                command.add(upload ? UP_GRID : DOWN_GRID); 
                errorCode.setProblem(upload ? Problem.In_Grid_Upload : Problem.In_Grid_Download);
                break;
            case scp:  
                command.add(upload ? UP_SCP  : DOWN_SCP); 
                errorCode.setProblem(upload ? Problem.In_SCP_Upload : Problem.In_SCP_Download);
                break;
        }
        command.add(srcUri);
        command.add(dstUri);
        String message = "I am going to fire this command: "+ command;
        final String username = remoteCreds.getUsername();
        if (username != null) {
            // if no user-pass is specified, we pass nothing to the script
            command.add(username);
            command.add(remoteCreds.getPassword());
            message += " with username: "+username;
        } else if (remoteCreds.getProxy() != null) {
            throw new ProcessingManagerException(errorCode, null, -1L, "Download using X509 proxy is not supported yet.");
        } else if (remoteCreds.getToken() != null) {
            throw new ProcessingManagerException(errorCode, null, -1L, "Download using (http session) token is not supported yet.");
        } else if (!remoteCreds.canUseRobot()) {
            throw new ProcessingManagerException(errorCode, null, -1L, "No usable credentials provided for download, and robot is not enabled.");
        } else if (remoteCreds.canUseRobot()) {
            message += " using robot credentials.";
        } 
        report(message, Level.INFO);
        int exitCode = fireCommand(command);
        if (exitCode != 0) {
            int index = errTxt.size()-1;    // the last error message is usually "invalid argument"
            if (index <= 0) {
                throw new ProcessingManagerException(errorCode, null, -1L, "No logs available.");
            }
            // the useful information is located in different parts of the log
            if (protocol == Protocol.lcg && index >= 2) index -= 2;
            if (protocol == Protocol.http && index >= 1) index -= 1;
            throw new ProcessingManagerException(errorCode, null, -1L, errTxt.get(index));
        }
    }
    
    /**
     * Copy a remote file to another remote location. The source and destination
     * servers may be accessed with different protocols.
     * @param srcProtocol The file transfer protocol supported by the source server.
     * @param srcUri A String pointing to the source file.
     * @param srcCreds Credentials for accessing the source server.
     * @param dstProtocol The file transfer protocol supported by the destination server.
     * @param dstUri A String showing where the file should be copied to.
     * @param dstCreds Credentials for accessing the destination server.
     */
    public static void copyFile(Protocol srcProtocol, String srcUri, Credentials srcCreds, Protocol dstProtocol, String dstUri, Credentials dstCreds) {
        String tempFile = createTempFileName();
        try {
            // download remote source to a local temp file
            copyFile(srcProtocol, srcUri, tempFile, srcCreds, false);
            // upload the local temp file to the remote destination
            copyFile(dstProtocol, tempFile, dstUri, dstCreds, true);
        } finally {
            try {
                // remove the temp file whether success or failure
                Runtime.getRuntime().exec("rm "+tempFile);
            } catch (IOException ex) {
                report("Could not remove temp file '"+tempFile+"', because: "+ex.getMessage(), Level.SEVERE);
            }
        }
    }

    static void localTransfer(String srcPath, String relativeDestPath) {
        List<String> command = new ArrayList<String>();
        command.add("cp");
        command.add(srcPath);
        command.add(relativeDestPath);
        report("I am going to fire this command: " + command, Level.INFO);
        int exitCode = fireCommand(command);
        if (exitCode != 0) {
            String msg = "Exit Code: " + exitCode;
            int index = errTxt.size() - 2;    // the last error message is usually "invalid argument"
            if (index < 0) {
                index = 0;
            }
            throw new ProcessingManagerException(new ErrorCode(Problem.In_File_Transfer, null), null, -1L, msg + errTxt.get(index));
        }
    }

    static int fireCommand(List<String> command){
//        SynchronousProcessingManager.report("I am going to fire this command: "+ command);
        errTxt = new ArrayList<String>();
        output = new ArrayList<String>();
        
        try {
            Process p = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
            // TODO: implement better error handling
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), errTxt);
            errorGobbler.start();

            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), output);
            outputGobbler.start();
            
            final int exitCode = p.waitFor();
            outputGobbler.join();
            
            // TODO
            report("Command errors: "+errTxt, Level.INFO);
            report("Command outputs: "+output, Level.INFO);
            
            return exitCode;
        } catch (Exception ex) {
            final String message = ex.getMessage();
            errTxt.add(message);
            throw new ProcessingManagerException(new ErrorCode(Problem.In_CommandLine_Invocation, null), null, -1L, errTxt.toString()+" in running: "+command.get(0), ex); 
        }
    }
    
    protected static Credentials getCredentials(Long userID, Resource resource, PersistenceManager pm) {
        UserAuthentication userAuth = pm.getAuthentication(userID, resource.getDbId());
        Credentials cred = new Credentials();
        if (userAuth == null) {
            if (resource.getRobot()) {
                cred.useRobot(true);
            } else {
                return null;
            }
        } else {
            // TODO: if no robot is set, there must be username and password?
            cred.setUserPass(userAuth.getUserLogin(), PersistenceManager.decryptString(userAuth.getAuthentication()));
        }
        return cred;
    }
    
}
