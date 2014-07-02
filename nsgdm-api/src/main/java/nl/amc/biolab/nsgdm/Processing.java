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
import java.util.Date;

/**
This code defines the class Processing with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Processing implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String ProcessingName;
    private String ProcessingDescription;
    private String ProcessingStatus;
    //private String ProcessingDevelopers;
    private Date ProcessingDate, ProcessingLastUpdate;
    
    //private Collection<SubmissionInput> inputs;
    private Collection<IOPort> Ports;
    //private Collection<DataElement> ProcessingOutput;
    private Collection<User> users;
    private Collection<UserProcessing> acls;
    private Collection<Submission> submissions;
    private Project project;
    private Application application;

    public Project getProject() {
        if (project == null) {
        	project = new Project();
        }
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
    
    public Application getApplication() {
        if (application == null) {
        	application = new Application();
        }
        return application;
    }
    public void setApplication(Application application) {
        this.application = application;
    }
    
/*    public Collection<SubmissionInput> getInputData() {
        if (inputs == null) {
        	inputs = new ArrayList<SubmissionInput>();
        }
        return inputs;
    }

    public void setInputData(Collection<SubmissionInput> inputs) {
        this.inputs = inputs;
    }
*/
    
    public Collection<IOPort> getPorts() {
        if (Ports == null) {
        	Ports = new ArrayList<IOPort>();
        }
        return Ports;
    }

    public void setPorts(Collection<IOPort> Ports) {
        this.Ports = Ports;
    }

    
/*    public Collection<DataElement> getOutputData() {
        if (ProcessingOutput == null) {
        	ProcessingOutput = new ArrayList<DataElement>();
        }
        return ProcessingOutput;
    }

    public void setOutputData(Collection<DataElement> ProcessingOutput) {
        this.ProcessingOutput = ProcessingOutput;
    }

*/    public void setSubmissions(Collection<Submission> submissions) {
        this.submissions = submissions;
    }

    public Collection<Submission> getSubmissions() {
        if (submissions == null) {
        	submissions = new ArrayList<Submission>();
        }
        return submissions;
    }

    public void setUsers(Collection<User> users) {
    	this.users=users;
    }
    public Collection<User> getUsers() {
        if (users == null) {
        	users = new ArrayList<User>();
        }
    return users;
    }

    public void setExperimentACLs(Collection<UserProcessing> acls) {
    	this.acls=acls;
    }
    public Collection<UserProcessing> getExperimentACLs() {
        if (acls == null) {
        	acls = new ArrayList<UserProcessing>();
        }
    return acls;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

     public String getName() {
        return ProcessingName;
    }

    public void setName(String ProcessingName) {
        this.ProcessingName = ProcessingName;
    }
    
    public String getDescription() {
        return ProcessingDescription;
    }
    public void setDescription(String ProcessingDescription) {
        this.ProcessingDescription = ProcessingDescription;
    }
/*
    public String getDevelopers() {
        return ProcessingDevelopers;
    }
    public void setDevelopers(String ProcessingDevelopers) {
        this.ProcessingDevelopers = ProcessingDevelopers;
    }

*/    public String getStatus() {
        return ProcessingStatus;
    }
    public void setStatus(String ProcessingStatus) {
        this.ProcessingStatus = ProcessingStatus;
    }

    public Date getDate() {
        return ProcessingDate;
    }
    public void setDate(Date ProcessingDate) {
        this.ProcessingDate = ProcessingDate;
    }
    public Date getLastUpdate() {
        return ProcessingLastUpdate;
    }
    public void setLastUpdate(Date ProcessingLastUpdate) {
        this.ProcessingLastUpdate = ProcessingLastUpdate;
    }
}
