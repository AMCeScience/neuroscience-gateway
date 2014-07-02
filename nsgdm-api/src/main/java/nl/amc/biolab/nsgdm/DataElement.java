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
This code defines the class DataElement with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class DataElement implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String Name, URI, ScanID, Subject, Type, Format, Applications;
    private Resource resource;
    protected Collection<Property> properties;
    private Collection<Project> projects;
    private Collection<SubmissionIO> submissionIOs;
    private Collection<IOPort> Ports;
    private Collection<Replica> replicas;
    private Date Date;
    private int Size;

    public Date getDate() {
        return Date;
    }
    public void setDate(Date Date) {
        this.Date = Date;
    }
    public Collection<Replica> getReplicas() {
        if (replicas == null) {
        	replicas = new ArrayList<Replica>();
        }
        return replicas;
    }
    public void setReplicas(Collection<Replica> replicas) {
        this.replicas = replicas;
    }

   public Collection<SubmissionIO> getSubmissions() {
        if (submissionIOs == null) {
        	submissionIOs = new ArrayList<SubmissionIO>();
        }
        return submissionIOs;
    }
    public void setSubmissions(Collection<SubmissionIO> submissionIOs) {
        this.submissionIOs = submissionIOs;
    }

    public Collection<Project> getProjects() {
        if (projects == null) {
        	projects = new ArrayList<Project>();
        }
        return projects;
    }

    public void setProjects(Collection<Project> projects) {
        this.projects = projects;
    }

    public Collection<Property> getProperties() {
        if (properties == null) {
        	properties = new ArrayList<Property>();
        }
        return properties;
    }

    public void setProperties(Collection<Property> properties) {
        this.properties = properties;
    }
    
    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setSubject(String Subject) {
        this.Subject = Subject;
    }

    public String getSubject() {
        return Subject;
    }

    public String getScanID() {
        return ScanID;
    }

    public void setScanID(String ScanID) {
        this.ScanID = ScanID;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public Resource getResource() {
        return resource;
    }
    public void setFormat(String Format) {
        this.Format = Format;
    }
    public String getFormat() {
        return Format;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return Name;
    }


    public void setSize(int Size) {
        this.Size = Size;
    }
    public int getSize() {
        return Size;
    }


    public void setApplications(String Applications) {
        this.Applications = Applications;
    }
    public String getApplications() {
        return Applications;
    }

    public String get(String key) {
        for (Property property : properties) {
            if (key.equalsIgnoreCase(property.getKey())) {
                return property.getValue();
            }            
        }
        return null;
    }
    public Collection<IOPort> getPorts() {
        if (Ports == null) {
        	Ports = new ArrayList<IOPort>();
        }
        return Ports;
    }

    public void setPorts(Collection<IOPort> Ports) {
        this.Ports = Ports;
    }

    
}
