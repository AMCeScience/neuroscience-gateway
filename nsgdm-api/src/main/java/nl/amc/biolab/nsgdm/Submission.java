/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
Created on : 26 November 2012, 16:59
Copyright (C) 2013  Academic Medical Center of the University of Amsterdam
Author: a.benabdelkader@amc.uva.nl

*/
package nl.amc.biolab.nsgdm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
This code defines the class Submission with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Submission implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String Name, status;
    private Processing processing;
    private boolean results;
    private Collection<Status> statuses;
    private List<SubmissionIO> submissionIOs;
    private List<Error> errors;

    public Collection<Status> getStatuses() {
        if (statuses == null) {
        	statuses = new ArrayList<Status>();
        }
        return statuses;
    }
    public void setStatuses(Collection<Status> statuses) {
        this.statuses = statuses;
    }

    public boolean getResults() {
        return results;
    }
    public void setResults(boolean results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Processing getProcessing() {
        if (processing == null) {
        	processing = new Processing();
        }
        return processing;
    }
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }
    public List<SubmissionIO> getSubmissionIOs() {
        if (submissionIOs == null) {
        	submissionIOs = new ArrayList<SubmissionIO>();
        }
        return submissionIOs;
    }

    public void setSubmissionIOs(List<SubmissionIO> submissionIOs) {
        this.submissionIOs = submissionIOs;
    }
    public List<Error> getErrors() {
        if (errors == null) {
        	errors = new ArrayList<Error>();
        }
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }


    

}
