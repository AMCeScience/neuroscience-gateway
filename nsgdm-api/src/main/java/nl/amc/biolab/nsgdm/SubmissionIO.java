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
This code defines the class SubmissionIO with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class SubmissionIO implements Serializable {
	private static final long serialVersionUID = 1L;
	private long dbId;
	private String Type;
    private IOPort ioport;
    private DataElement dataelement;
    private Submission submission;

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public IOPort getPort() {
        if (ioport == null) {
        	ioport = new IOPort();
        }
        return ioport;
    }

    public void setPort(IOPort ioport) {
        this.ioport = ioport;
    }

    public DataElement getDataElement() {
        if (dataelement == null) {
        	dataelement = new DataElement();
        }
        return dataelement;
    }
    public void setDataElement(DataElement dataelement) {
        this.dataelement = dataelement;
    }

    public Submission getSubmission() {
        if (submission == null) {
        	submission = new Submission();
        }
        return submission;
    }
    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

}
