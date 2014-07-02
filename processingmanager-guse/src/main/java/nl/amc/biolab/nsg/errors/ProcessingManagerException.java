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

package nl.amc.biolab.nsg.errors;

import nl.amc.biolab.nsg.errors.ErrorCode.During;
import nl.amc.biolab.nsgdm.Processing;

/**
 *
 * @author mahdi
 */
public class ProcessingManagerException extends RuntimeException{
    
    protected ErrorCode errorCode;
    Processing processing;
    Long submissionId;
    
//    public ProcessingManagerException(Long submissionId, int errorCode) {
//        this(submissionId, errorCode, "", null);
//    }

//    public ProcessingManagerException(Long submissionId, int errorCode) {
//        this(submissionId, errorCode, additionalInfo, null);
//    }
    public ProcessingManagerException(ErrorCode errorCode, Processing processing, Long submissionId, String additionalInfo) {
        this (errorCode, processing, submissionId, additionalInfo, null);
    }

    public ProcessingManagerException(ErrorCode errorCode, Processing processing, Long submissionId, String additionalInfo, Throwable cause) {
        super(errorCode.problem.description+"\n"+additionalInfo+((cause==null)?"":"\n"+cause.getMessage()), cause);
        this.errorCode = errorCode;
        this.processing = processing;
        this.submissionId = submissionId;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Processing getProcessing() {
        return processing;
    }

    public Long getSubmissionId() {
        return submissionId;
    }
    
    public ProcessingManagerException addOccasion(During time) {
        this.errorCode.setOccasion(time);
        return this;
    }
}
