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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
abstract class ViewerForm<D> extends Form {
	private VerticalLayout layout = new VerticalLayout();

	private final Map<Object, Field> fields = new LinkedHashMap<Object, Field>();
	
	private final ViewerForm<D> viewerForm = this;

	private D dataSource;

	public ViewerForm() {
		super();
		
		addStyleName("bordered");
		setWidth("100%");
		setHeight("100%");
		setSizeFull();
		setImmediate(false);

		layout.setWidth("100%");
		layout.setHeight("100%");
		layout.setSizeFull();
		layout.setSpacing(false);
		layout.setMargin(true);
		setLayout(layout);

//		addButtons(createButtons());
	}
	
	protected abstract List<Component> createButtons();
	
	public void removeAllComponents() {
		layout.removeAllComponents();
		addButtons(createButtons());
	}

	protected void setDataSource(D dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * add labelField for a dataSource property; only call after setDataSource
	 * @param propertyId
	 * @param caption
	 */
	protected void addLabelField(String propertyId, String caption) {
		LabelField f = new LabelField();
		f = new LabelField();
		addField(propertyId, f);
		
		try {
			if(!propertyId.contains(".")) {
				f.setLabelValue(caption, (String) new MethodProperty<D>(dataSource, propertyId).getValue());
			} else {
				f.setLabelValue(caption, (String) new NestedMethodProperty(dataSource, propertyId).getValue());
			}
		} catch (Exception e) {
		}
		
		fields.put(propertyId, f);
	}

	/**
	 * add custom labelField
	 * @param propertyId
	 * @param caption
	 * @param value
	 */
	protected void addLabelField(String propertyId, String caption, String value) {
		LabelField f = new LabelField();
		f = new LabelField();
		f.setLabelValue("<b>" + caption + ":</b> " + value);
		addField(propertyId, f);
	}

	private void addButtons(final List<Component> buttonList) {
		if (buttonList != null && buttonList.size() != 0) {
			HorizontalLayout buttons = new HorizontalLayout();
//			buttons.setWidth("100%");
			buttons.setHeight("50px");
			buttons.setSpacing(true);
			layout.addComponent(buttons);

			for(Component b: buttonList) {
				buttons.addComponent(b);
				buttons.setComponentAlignment(b, Alignment.TOP_LEFT);
			}
			
			getLayout().addComponent(buttons);
		}
	}
}
