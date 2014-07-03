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
package nl.amc.biolab.nsg.display.service;

import java.util.LinkedHashMap;
import java.util.Map;

import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class FieldService {
	/**
	 * java class name, (propertyId, header)
	 */
	protected Map<String, Map<String, String>> fieldHeader = new LinkedHashMap<String, Map<String, String>>();
	
	public FieldService() {
		//TODO refactor: get from backend

		Map<String, String> map = new LinkedHashMap<String, String>() {{
			put("name", "name");
			put("description", "description");
		}};
		fieldHeader.put(Project.class.getName(), map);

		map = new LinkedHashMap<String, String>() {{
			put("subject", "subject");
			put("date", "date");
			put("type", "type");
			put("scanID", "scan ID");
			put("format", "format");
			put("resource.name", "source");
		}};
		fieldHeader.put(DataElement.class.getName(), map);

		map = new LinkedHashMap<String, String>() {{
			put("date", "date");
			put("project.name", "project");
			put("description", "description");
			put("application.name", "application");
			put("status", "status");
		}};
		fieldHeader.put(Processing.class.getName(), map);

		map = new LinkedHashMap<String, String>() {{
			put("name", "name");
			put("description", "description");
		}};
		fieldHeader.put(Application.class.getName(), map);
	}

	/**
	 * 
	 * @param clazz to get fieldheaders for
	 * @return
	 */
	public Map<String, String> getFieldHeaders(String clazz) {
		return fieldHeader.get(clazz);
	}
}