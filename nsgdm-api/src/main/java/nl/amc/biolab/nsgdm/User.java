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
import java.sql.Blob;
/**
This code defines the class User with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String LiferayID, Affiliation, FirstName, LastName, UserEmail;
    //private Blob Authentication;
    //private String UserID, Session;
    
    private Collection<Project> projects;
    private Collection<Application> applications;
    private Collection<Processing> processings;
    private Collection<UserProcessing> expacls;
    private Collection<Preference> preferences;
    private UserProcessing ue;

    public Collection<Application> getApplications() {
        if (applications == null) {
        	applications = new ArrayList<Application>();
        }
        return applications;
    }
    public void setApplications(Collection<Application> applications) {
        this.applications = applications;
    }
    public Collection<Processing> getProcessings() {
        if (processings == null) {
        	processings = new ArrayList<Processing>();
        }
        return processings;
    }
    public void setProcessings(Collection<Processing> processings) {
        this.processings = processings;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public Collection<UserProcessing> getExperimentACLs() {
        if (expacls == null) {
        	expacls = new ArrayList<UserProcessing>();
        }
    return expacls;
    }

    public Collection<Preference> getPreferences() {
        if (preferences == null) {
        	preferences = new ArrayList<Preference>();
        }
    return preferences;
    }

    public String getExperimentACL() {
        return ue.getACL();
    }
    public Collection<Project> getProjects() {
        if (projects == null) {
        	projects = new ArrayList<Project>();
        }
    return projects;
    }

/*    public String getUserID() {
        return UserID;
    }
    public void setUserID(String UserID) {
        this.UserID = UserID;
    }
    
*/
    public String getLiferayID() {
        return LiferayID;
    }
    public void setLiferayID(String LiferayID) {
        this.LiferayID = LiferayID;
    }
    
/*    public Blob getAuthentication() {
        return Authentication;
    }
    public void setAuthentication(Blob Authentication) {
        this.Authentication = Authentication;
    }
    
    public String getSession() {
        return Session;
    }
    public void setSession(String Session) {
        this.Session = Session;
    }
    
*/    public String getFirstName() {
        return FirstName;
    }
    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }
    
    public String getLastName() {
        return LastName;
    }
    public void setLastName(String LastName) {
        this.LastName = LastName;
    }
    
    public String getAffiliation() {
        return Affiliation;
    }

    public void setAffiliation(String Affiliation) {
        this.Affiliation = Affiliation;
    }
    
    public String getEmail() {
        return UserEmail;
    }

    public void setEmail(String UserEmail) {
        this.UserEmail = UserEmail;
    }
}
