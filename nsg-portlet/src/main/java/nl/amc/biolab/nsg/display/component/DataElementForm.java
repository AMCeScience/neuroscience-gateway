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
package nl.amc.biolab.nsg.display.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.nsg.display.service.FieldService;

import com.vaadin.ui.Component;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 */
class DataElementForm extends ViewerForm<DataElement> {
	private static final long serialVersionUID = -4998746988286376354L;
	private FieldService fieldService;
	
	public DataElementForm(FieldService fieldService) {
		super();
		this.fieldService = fieldService;
	}

	public void setDataElement(DataElement dataElement/*,  List<Property> metadata*/) {
		if (dataElement == null) {
			return;
		}

		removeAllComponents();

		setDataSource(dataElement);

		Map<String, String> fields = fieldService.getFieldHeaders(DataElement.class.getName());
		for(String k: fields.keySet()) {
			addLabelField(k, fields.get(k));
		}

		//metadata
//		if (metadata != null){
//			Label space = new Label("<div>&nbsp;</div>", Label.CONTENT_XHTML);
//			space.setWidth("100%");
//			space.setHeight("10px");
//			getLayout().addComponent(space);
//
//			Table table = new Table();
//			table.setWidth("100%");
//			table.setHeight("360px");
//			table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
//			table.setSelectable(false);
//			table.setCaption("properties");
//			table.addContainerProperty("key", String.class,  null);
//			table.addContainerProperty("value",  String.class,  null);
//			for (Property p: metadata) {
//				table.addItem(new Object[] {p.getDescription(),p.getValue()}, p);
//			}
//			getLayout().addComponent(table);
//		}
	}
	
	@Override
	protected List<Component> createButtons() {
		List<Component> buttons = new ArrayList<Component>();
				
		return buttons;
	}
}
