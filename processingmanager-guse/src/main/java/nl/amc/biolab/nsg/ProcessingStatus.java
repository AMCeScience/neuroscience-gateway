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

/**
 * The status messages that are shown to the user. For gUse status messages, 
 * use the class StatusConstants from ASM.
 * @author mmajid
 */
public class ProcessingStatus {
    
    /** at the moment the processing is created. */
    public final static String STAT_INIT = "In Preparation";       

    /** while transferring the data or running the workflow. */
    public final static String STAT_RUNNING = "In Progress";
   
    /** workflow finished and output transferred successfully. */
    public final static String STAT_FINISHED = "Done";
    
    /** An error occurred. It is being investigated. */
    public final static String STAT_ERROR = "On Hold";

    /** The error could not be recovered from. */
    public final static String STAT_FAILED = "Failed";
    
    /** The submission is being resumed */
    public final static String STAT_RESUME = STAT_INIT; // TODO: Do we want a new String here?

    /** The status returned by gUse is unknown. */
//    public final static String STAT_UNKNOWN = "Unknown";
    
    private int running=0, finished=0, error=0, failed=0, init=0, error_for_summary=0;
    private StringBuilder errorMessage = new StringBuilder();
    
    public void incError(String msg) {
        errorMessage.append(msg).append("\r\n");
        error_for_summary++;
        incError();
    }
        
    public void incRunning(){ running++; }
    public void incError(){ error++; }
    public void incFailed(){ failed++; }
    public void incInit(){ init++; }
    public void incFinished(){ finished++; }
    public void inc(String status) {
        if (status.equals(STAT_FINISHED)) incFinished();
        else if (status.equals(STAT_RUNNING)) incRunning();
        else if (status.equals(STAT_FAILED)) incFailed();
        else if (status.equals(STAT_INIT)) incInit();
        else { incError(); }                
    }
    
    public boolean hasFinished(){
        return running==0 && init==0 && error==0;
    }
    
    public String makeStatusSummary(){
        final String finStat = finished + " " + STAT_FINISHED;
        final String errStat = error + " " + STAT_ERROR;
        final String runStat = running + " " + STAT_RUNNING;
        final String failStat = failed + " " + STAT_FAILED;
        final String initStat = init + " " + STAT_INIT;
        String overallStatus = "";
        if (finished > 0) overallStatus = concat(overallStatus, finStat);
        if (running > 0)  overallStatus = concat(overallStatus, runStat);
        if (error > 0)    overallStatus = concat(overallStatus, errStat);
        if (failed > 0)   overallStatus = concat(overallStatus, failStat);
        if (init > 0)     overallStatus = concat(overallStatus, initStat);
        return overallStatus;
    }

    private String concat(String overallStatus, String newStat) {
        if (!overallStatus.equals("")) overallStatus += "; ";
        return overallStatus+newStat;
    }

    boolean hasError() {
        return error_for_summary!=0;
    }

    String makeErrorSummary() {
        return errorMessage.toString();
    }
}
