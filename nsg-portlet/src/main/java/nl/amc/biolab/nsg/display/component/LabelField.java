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

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
public class LabelField extends CustomField {
	private static final long serialVersionUID = 6221940695743313274L;
	private HorizontalLayout layout = new HorizontalLayout();
	Label label = new Label("", Label.CONTENT_XHTML);

	public LabelField() {
		setCompositionRoot(layout);
		label.setWidth("100%");
		label.setHeight("100%");
		label.setStyle("");
		layout.addComponent(label);
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public void addComponent(Component c) {
		layout.addComponent(c);
	}

	/**
	 * NB: does not escape html code in arguments!
	 * @param value - the new label value
	 */
	public void setLabelValue(String value) {
		label.setValue(value);
	}
	
	/**
	 * 
	 * @return the label object showing text
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * NB: does not escape html code in arguments!
	 * @param caption
	 * @param value
	 */
	public void setLabelValue(String caption, String value) {
		//TODO escape html in caption and value
		setLabelValue("<span style='font-size: 12px;'><b>" + caption + ":</b>&nbsp;" + value + "</span>");
	}
}
