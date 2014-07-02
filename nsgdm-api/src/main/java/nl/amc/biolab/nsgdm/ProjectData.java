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
This code defines the class ProjectData with its members and methods.
This is part of the data management API for the Neuroscience gateway.
**/
public class ProjectData extends DataElement implements Serializable {
	private static final long serialVersionUID = 1L;
	private Project ProjectID;
    private DataElement DataID;
    
    public Project getProject() {
        return ProjectID;
    }

    public void setProject(Project Project) {
        this.ProjectID = Project;
    }
    public DataElement getDataElement() {
        return DataID;
    }
    public void setDataElement(DataElement Element) {
        this.DataID = Element;
    }
}
