/*
 * Neuroscience Gateway Proof of Concept/Research Portlet
 * This application was developed for research purposes at the Bioinformatics Laboratory of the AMC (The Netherlands)
 *
 * Copyright (C) 2013 Bioinformatics Laboratory, Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.amc.biolab.nsg.display.data;

import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.User;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class DisplayProcessing extends Processing {
	private String user = "";
	
	public DisplayProcessing(Processing p) {
		setProject(p.getProject());
		setApplication(p.getApplication());
		setPorts(p.getPorts());
		setSubmissions(p.getSubmissions());
		setUsers(p.getUsers());
		setExperimentACLs(p.getExperimentACLs());
		setDbId(p.getDbId());
		setName(p.getName());
		setDescription(p.getDescription());
		setStatus(p.getStatus());
		setDate(p.getDate());
		
		if(getUsers().iterator().hasNext()) {
            final User owner = getUsers().iterator().next();
			user = owner.getFirstName()+" "+owner.getLastName();
			//user = getUsers().iterator().next().getLiferayID();
		}
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
