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

/**
This code defines the class Project with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String ProjectName;
    private String ProjectDescription;
    private String XnatID;
    private String ProjectOwner;
    
    private Collection<DataElement> ProjectData;
    private Collection<Application> ProjectApplication;
    private Collection<Processing> processings;
    private Collection<User> users;
    //private Collection<UserProject> acls;
    private User user;
    //private User user;

    public Collection<Processing> getProcessings() {
        if (processings == null) {
        	processings = new ArrayList<Processing>();
        }
        return processings;
    }

    public void setProcessings(Collection<Processing> processings) {
        this.processings = processings;
    }

    public Collection<Application> getApplications() {
        if (ProjectApplication == null) {
        	ProjectApplication = new ArrayList<Application>();
        }
        return ProjectApplication;
    }

    public void setApplications(Collection<Application> ProjectApplication) {
        this.ProjectApplication = ProjectApplication;
    }

    public Collection<DataElement> getDataElements() {
        if (ProjectData == null) {
        	ProjectData = new ArrayList<DataElement>();
        }
        return ProjectData;
    }

    public void setDataElements(Collection<DataElement> ProjectData) {
        this.ProjectData = ProjectData;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

     public String getName() {
        return ProjectName;
    }

    public void setName(String ProjectName) {
        this.ProjectName = ProjectName;
    }
    public String getDescription() {
        return ProjectDescription;
    }

    public void setDescription(String ProjectDescription) {
        this.ProjectDescription = ProjectDescription;
    }
    public String getOwner() {
        return ProjectOwner;
    }

    public void setOwner(String ProjectOwner) {
        this.ProjectOwner = ProjectOwner;
    }
    public String getXnatID() {
        return XnatID;
    }

    public void setXnatID(String XnatID) {
        this.XnatID = XnatID;
    }
    public User getUser() {
        return user;
    }
    ///*
    public Collection<User> getUsers() {
        if (users == null) {
        	users = new ArrayList<User>();
        }
    return users;
    }
//*/
/*    public void setACLts(Collection<UserProject> acls) {
        this.acls = acls;
    }
    public Collection<UserProject> getACLs() {
        if (acls == null) {
        	acls = new ArrayList<UserProject>();
        }
    return acls;
    }
*/
}
