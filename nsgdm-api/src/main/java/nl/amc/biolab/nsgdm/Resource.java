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
This code defines the class Resource with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class Resource implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private String Name, Description, BaseURI, protocol;
    private Collection<DataElement> data;
    private boolean storage, computing, robot;
    private Collection<Replica> replicas;
    private Collection<IOPort> ports;

    public Collection<IOPort> getIOPorts() {
        if (ports == null) {
        	ports = new ArrayList<IOPort>();
        }
        return ports;
    }
    public void setIOPorts(Collection<IOPort> ports) {
        this.ports = ports;
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

    
    public long getDbId() {
        return dbId;
    }
    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public boolean getStorage() {
        return storage;
    }
    public void setStorage(boolean storage) {
        this.storage = storage;
    }

    public boolean getComputing() {
        return computing;
    }
    public void setComputing(boolean computing) {
        this.computing = computing;
    }

    public String getName() {
        return Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    
    
    public String getBaseURI() {
        return BaseURI;
    }
    public void setBaseURI(String BaseURI) {
        this.BaseURI = BaseURI;
    }
    
    public String getDescription() {
        return Description;
    }
    public void setDescription(String Description) {
        this.Description = Description;
    }
    
    public Collection<DataElement> getDataElements() {
        if (data == null) {
        	data = new ArrayList<DataElement>();
        }
        return data;
    }

    public void setDataElements(Collection<DataElement> data) {
        this.data = data;
    }

    public boolean getRobot() {
        return robot;
    }
    public void setRobot(boolean robot) {
        this.robot = robot;
    }
    
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
}
