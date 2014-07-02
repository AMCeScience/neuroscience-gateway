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

/**
This code defines the class UserProcessing with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class UserProcessing implements Serializable {
	private static final long serialVersionUID = 1L;
	//public class ProjectData extends Data_Element implements Serializable {
    private long dbId;
    private Processing processing;
    private User user;
    private String acl;
    
    public Processing getProcessing() {
        return processing;
    }

    public void setProcessing(Processing processing) {
        this.processing = processing;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getACL() {
        return acl;
    }
    public void setACL(String acl) {
        this.acl = acl;
    }
    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }
   
}
