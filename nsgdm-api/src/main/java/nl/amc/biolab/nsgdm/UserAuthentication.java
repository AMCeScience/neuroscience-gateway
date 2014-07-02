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
import java.sql.Blob;
import java.util.Date;

/**
This code defines the class UserActivity with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class UserAuthentication implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String userLogin, Session;
    private Blob Authentication;
    
    private User user;
    private Resource resource;

    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public Resource getResource() {
        return resource;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getUserLogin() {
        return userLogin;
    }
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
    
    public Blob getAuthentication() {
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
    

 
}
