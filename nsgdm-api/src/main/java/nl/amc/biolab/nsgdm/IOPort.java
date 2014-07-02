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
This code defines the class IOPort with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class IOPort implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
    private int PortNumber;
    private String PortName, DisplayName, IOType, DataType, DataFormat, OutputApps;
    private boolean Visible;
    
    private Application application;
    private Collection<DataElement> Elements;
    private Collection<SubmissionIO> submissionIOs;
    private Resource resource;

    public void setVisible(boolean Visible) {
        this.Visible = Visible;
    }
    public boolean isVisible() {
        return Visible;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public Resource getResource() {
        return resource;
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

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public int getPortNumber() {
        return PortNumber;
    }
    public void setPortNumber(int PortNumber) {
        this.PortNumber = PortNumber;
    }

    public String getIOType() {
        return IOType;
    }
    public void setIOType(String IOType) {
        this.IOType = IOType;
    }


    public String getDataType() {
        return DataType;
    }
    public void setDataType(String DataType) {
        this.DataType = DataType;
    }

    public String getPortName() {
        return PortName;
    }
    public void setPortName(String PortName) {
        this.PortName = PortName;
    }

    public String getDisplayName() {
        return DisplayName;
    }
    public void setDisplayName(String DisplayName) {
        this.DisplayName = DisplayName;
    }

    public String getDataFormat() {
        return DataFormat;
    }
    public void setDataFormat(String DataFormat) {
        this.DataFormat = DataFormat;
    }
    public Collection<DataElement> getInputData() {
        if (Elements == null) {
        	Elements = new ArrayList<DataElement>();
        }
        return Elements;
    }

    public void setInputData(Collection<DataElement> Elements) {
        this.Elements = Elements;
    }
    public Collection<SubmissionIO> getProcessings() {
        if (submissionIOs == null) {
        	submissionIOs = new ArrayList<SubmissionIO>();
        }
        return submissionIOs;
    }
    public void setProcessings(Collection<SubmissionIO> submissionIOs) {
        this.submissionIOs = submissionIOs;
    }

    public String getOutputApps() {
        return OutputApps;
    }
    public void setOutputApps(String OutputApps) {
        this.OutputApps = OutputApps;
    }


}
