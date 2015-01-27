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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.amc.biolab.datamodel.objects.Application;
import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.nsg.display.VaadinTestApplication;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class ProcessingForm extends Form {
	private static final long serialVersionUID = -7848610407155766662L;

	protected static final String SUBMIT = "start";

	private VerticalLayout layout = new VerticalLayout();
	private Select app = new Select();
	private TextField name = new TextField("name");
	private ListSelect inputData = new ListSelect();
	private LabelField appDescription;

	private Processing processing = new Processing();
	private Set<DataElement> dataElements = new HashSet<DataElement>();
	private Application tempApp = null;

	private final Map<String, Field> fields = new HashMap<String, Field>();

	private final ProcessingForm form = this;

	public ProcessingForm() {
		addStyleName("bordered");
		setWidth("100%");
		setHeight("100%");
		setImmediate(true);
		layout.setWidth("100%");
		layout.setSpacing(true);
		layout.setMargin(true);
		setLayout(layout);

		setProcessingFields();
		setButtons();
	}

	public void setProcessingFields() {
		Map<String, Field> fields = new HashMap<String, Field>();

		app.setImmediate(false);
		app.setWidth("79%");
		app.setHeight("-1px");
		app.setRequired(true);
		app.setRequiredError("Application is required");
		app.setCaption("Application");
		fields.put("application", app);

		appDescription = new LabelField();
		appDescription.addStyleName("bordered");
		fields.put("description", appDescription);

		name.setWidth("97%");
		name.setRequired(true);
		name.setRequiredError("Description is required");
		name.setCaption("Description (max 50 characters)");
		name.setNullRepresentation("");
		name.addValidator(new RegexpValidator(".{1,50}", "description is too long"));
		fields.put("name", name);

		inputData.setMultiSelect(true);
		inputData.setWidth("97%");
		inputData.setHeight("200px");
		inputData.setRequired(true);
		inputData.setRequiredError("Input is required");
		inputData.setCaption("Inputs");
		fields.put("submissions", inputData);

		setFields(fields);
	}

	public void setFields(Map<String, Field> fields) {
		this.fields.putAll(fields);
		setFormFieldFactory(new FormFieldFactory() {
			private static final long serialVersionUID = -832396402143639278L;

			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				return form.fields.get((String) propertyId);
			}
		});
	}

	public Processing getProcessing() {
		return processing;
	}

	public void setProcessing(Set<DataElement> inputDataList, List<Application> applications) {
		this.processing = new Processing();

		processing.setName((String) name.getValue());

		setItemDataSource((Item) new BeanItem<Processing>(processing), fields.keySet());

		// applications
		//Select app = new Select();

//		app2.setImmediate(false);
//		app2.setWidth("97%");
//		app2.setHeight("-1px");
//		app2.setRequired(true);
//		app2.setRequiredError("Application is required 123");
//		app2.setCaption("Application");

//		layout.replaceComponent(app, app2);
//		layout.removeComponent(app);

//		app = app2;

//		fields.put("application", app);

		app.setNullSelectionAllowed(false);
		app.setNewItemsAllowed(false);
		app.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		app.setItemCaptionPropertyId("name");

		BeanItemContainer<Application> abic = new BeanItemContainer<Application>(Application.class);
		abic.addAll(applications);

		app.setContainerDataSource(abic);

		// application description
		if (applications != null && applications.size() != 0) {
			tempApp = applications.get(0);

			app.setValue(applications.get(0).getDbId());
			app.select(applications.get(0));

			appDescription.setLabelValue(applications.get(0).getDescription());

			app.addListener(new Property.ValueChangeListener() {
				private static final long serialVersionUID = -5315052115453471609L;

				@Override
				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
					LabelField lf = new LabelField();

					lf.addStyleName("bordered");
					lf.setLabelValue(tempApp.getDescription());

					layout.replaceComponent(appDescription, lf);

					appDescription = lf;

					fields.put("description", appDescription);
				}
			});
		}

		// dateElements
		if (inputDataList != null) {
			for (DataElement de : inputDataList) {
				dataElements.add(de);
			}
		}

		setInputData(dataElements);
	}

	public void setInputData(Set<DataElement> inputDataList) {
		BeanItemContainer<DataElement> dbic = new BeanItemContainer<DataElement>(DataElement.class);
		if (inputDataList != null && inputDataList.size() != 0) {
			dbic.addAll(Arrays.asList(inputDataList.toArray(new DataElement[0])));
		}
		inputData.setContainerDataSource(dbic);
		inputData.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		inputData.setItemCaptionPropertyId("name");
		inputData.setMultiSelect(true);
		for (Object iid : inputData.getItemIds()) {
			inputData.select(iid);
		}
	}

	public void setButtons() {
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setHeight("40px");
		buttons.setSpacing(true);
		getLayout().addComponent(buttons);

		final Button startButton = new NativeButton();
		
		startButton.setCaption(SUBMIT);
		startButton.setImmediate(true);
		startButton.setWidth("-1px");
		startButton.setHeight("-1px");
		startButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1906358615316029946L;

			public void buttonClick(ClickEvent event) {
				System.out.println(app.getValue());
				
				Set<Long> inputDbIds = new HashSet<Long>();
				
				for (Object iid : inputData.getItemIds()) {
					inputData.select(iid);
					
					inputDbIds.add(((DataElement) iid).getDbId());
				}
				
				((VaadinTestApplication) getApplication()).getUserDataService().setDataElementDbIds(inputDbIds);
				
				form.commit();
				
				processing.setApplication((Application) app.getValue());
				
				startButton.setData(processing);
				
				form.fireEvent(new Event(startButton));
			}
		});
		
		buttons.addComponent(startButton);
		buttons.setComponentAlignment(startButton, Alignment.TOP_RIGHT);

		final Button delButton = new NativeButton();
		delButton.setCaption("remove inputs");
		delButton.setImmediate(true);
		delButton.setWidth("-1px");
		delButton.setHeight("-1px");
		delButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3377452914254101817L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				if (inputData.getValue() != null) {
					for (DataElement de : (Set<DataElement>) inputData.getValue()) {
						inputData.removeItem(de);
						dataElements.remove(de);
					}
				}
			}
		});
		buttons.addComponent(delButton);
		buttons.setComponentAlignment(delButton, Alignment.TOP_RIGHT);
	}

	public Set<DataElement> getDataElements() {
		return dataElements;
	}

	public Collection<?> getApplications() {
		return app.getContainerDataSource().getItemIds();
	}
}
