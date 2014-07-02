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
import nl.amc.biolab.nsgdm.User;

/**
This code defines the class Application with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Application implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String Name;
    private String Description, Developers, OS, Version, Platform, ReleaseDate, Executable, InternalName, Applications;
    private int Type;
    
    private Collection<Project> ApplicationProjects;
    private Collection<User> users;
    private Collection<IOPort> ioports;
    private Collection<Processing> processings;
    private Collection<Replica> replicas;

    public Collection<Replica> getReplicas() {
        if (replicas == null) {
        	replicas = new ArrayList<Replica>();
        }
        return replicas;
    }
    public void setReplicas(Collection<Replica> replicas) {
        this.replicas = replicas;
    }

    public long getDbId() {
        return dbId;
    }
    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public int getType() {
        return Type;
    }
    public void setType(int Type) {
        this.Type = Type;
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

   public void setProjects(Collection<Project> projects) {
       this.ApplicationProjects = projects;
   }
   public Collection<Project> getProjects() {
        if (ApplicationProjects == null) {
        	ApplicationProjects = new ArrayList<Project>();
        }
        return ApplicationProjects;
    }

   public void setIOPorts(Collection<IOPort> ioports) {
       this.ioports = ioports;
   }
   public Collection<IOPort> getIOPorts() {
        if (ioports == null) {
        	ioports = new ArrayList<IOPort>();
        }
        return ioports;
    }

     public String getName() {
        return Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        this.Description = description;
    }
    
    public String getDevelopers() {
        return Developers;
    }
    public void setDevelopers(String Developers) {
        this.Developers = Developers;
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
    public String getOS() {
        return OS;
    }
    public void setOS(String OS) {
        this.OS = OS;
    }
    
    public String getVersion() {
        return Version;
    }
    public void setVersion(String version) {
        this.Version = version;
    }
    
    public String getPlatform() {
        return Platform;
    }
    public void setPlatform(String platform) {
        this.Platform = platform;
    }
    
    public String getReleaseDate() {
        return ReleaseDate;
    }
    public void setReleaseDate(String releasedate) {
        this.ReleaseDate = releasedate;
    }
    
    public String getExecutable() {
        return Executable;
    }
    public void setExecutable(String executable) {
        this.Executable = executable;
    }
    
    public String getInternalName() {
        return InternalName;
    }
    public void setInternalName(String InternalName) {
        this.InternalName = InternalName;
    }
    
    public String getApplications() {
        return Applications;
    }
    public void setApplications(String Applications) {
        this.Applications = Applications;
    }
    
    
}
