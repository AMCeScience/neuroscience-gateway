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
import java.util.Date;

/**
This code defines the class UserActivity with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class UserActivity implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String Activity, Status;
    private java.util.Date Date;
    
    private User user;

     public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getActivity() {
        return Activity;
    }
    public void setActivity(String Activity) {
        this.Activity = Activity;
    }


    public String getStatus() {
        return Status;
    }
    public void setStatus(String Status) {
        this.Status = Status;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date Date) {
        this.Date = Date;
    }

 
}
