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
This code defines the class Preference with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Preference implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String PrefKey, PrefValue;
    private String PrefDesc;
    
    private Collection<User> users;

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

     public String getKey() {
        return PrefKey;
    }

    public void setKey(String PrefKey) {
        this.PrefKey = PrefKey;
    }
    public String getDescription() {
        return PrefDesc;
    }

    public void setDescription(String PrefDesc) {
        this.PrefDesc = PrefDesc;
    }
    public String getValue() {
        return PrefValue;
    }

    public void setValue(String PrefValue) {
        this.PrefValue = PrefValue;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }
    public Collection<User> getUsers() {
        if (users == null) {
        	users = new ArrayList<User>();
        }
    return users;
    }
}
