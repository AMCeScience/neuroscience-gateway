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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.amc.biolab.datamodel.objects.Application;
import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.datamodel.objects.Project;
import nl.amc.biolab.nsg.dataobjects.NsgDataElement;
import nl.amc.biolab.nsg.dataobjects.NsgProject;
import nl.amc.biolab.nsg.dataobjects.NsgProperty;
import nl.amc.biolab.nsg.display.VaadinTestApplication;
import nl.amc.biolab.nsg.display.control.MainControl;
import nl.amc.biolab.nsg.display.data.DisplayProcessingStatus;
import nl.amc.biolab.nsg.display.service.FieldService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class MainUI extends CustomComponent {
	Logger logger = Logger.getLogger(MainUI.class);

	private VerticalLayout mainLayout;

	private HorizontalSplitPanel mainSplitPanel;

	// private Form editor;
	private AbstractComponent editor;

	private HorizontalLayout topPanel;
	private HorizontalLayout pagePanel;

	private NativeButton projectsButton;
	private NativeButton elementsButton;
	private NativeButton processingsButton;
	private NativeButton processingButton;
    private NativeButton dataElementButton;

	private Button searchButton;
	private Button newSearchButton;
	private Button allSearchButton;

	private Label label_1;

	private TextField searchTextField;
	private Select searchSelect;
	private Label searchLabel;

	private static final long serialVersionUID = -541581055112181398L;

	private FieldService fieldService = new FieldService();

	private ItemList<?> itemList;
	private Property.ValueChangeListener itemListChangeListner;

	private VaadinTestApplication app = (VaadinTestApplication) getApplication();

	@SuppressWarnings("unused")
	private MainUI() {
	}

	public MainUI(MainControl mainControl) {
		logger.setLevel(Level.DEBUG);

		buildMainLayout();

		setCompositionRoot(mainLayout);
	}

	public void init(boolean showNotification) {
		setPageMenu();

		app.getUserDataService().closeSession();
		app.getUserDataService().openSession();

		if (app.getPage() == VaadinTestApplication.PROJECTS) {
//			logger.debug("We are in projects page. Setting project item list contents.");
			setProjectItemList();

			if (app.getUserDataService().getProjectDbId() == null) {
				if (showNotification) {
					app.getMainWindow().showNotification("Please select a project");
				}
			}

			hideEditor();
		} else if (app.getPage() == VaadinTestApplication.DATA) {
//			logger.debug("We are in data page");

			if (app.getUserDataService().getProjectDbId() == null) {
//				logger.debug("Setting project item list contents.");
				setProjectItemList();

				if (showNotification) {
					app.getMainWindow().showNotification("Please select a project");
				}

				hideEditor();

				return;
			}

			hideEditor();

//			logger.debug("Setting data element item list contents.");
			setDataElementItemList(app.getUserDataService().getProjectDbId());
		} else if (app.getPage() == VaadinTestApplication.PROCESSING) {
//			logger.debug("We are in processing page");

			if (app.getUserDataService().getProcessingDbId() != null) {
//				logger.debug("Setting processing status editor contents");
				setProcessingStatusEditor(app.getUserDataService().getProcessingDbId());
			}

//			logger.debug("Setting processing item list contents");
			setProccessingItemList();
		}

//		logger.debug("Finished initializing the page.");
	}

	// page menus
	public void setPageMenu() {
		dataElementButton.setVisible(false);
        dataElementButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -7033937988476912073L;

            public void buttonClick(ClickEvent event) {
                if (itemList.getValue() == null) {
                    return;
                }
                
                setDataElementEditor(((NsgDataElement) itemList.getValue()).getDataElement());
            }
        });
		
		processingButton.setVisible(false);
		processingButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -7033937988476912073L;

			public void buttonClick(ClickEvent event) {
				if (itemList.getValue() == null) {
					return;
				}

				dataElementsChange(((NsgDataElement) itemList.getValue()).getDataElement(), true);
			}
		});
	}

	public void setSearchSelect(final Map<String, String> fields) {
		searchSelect.removeAllItems();

		for (String f : fields.keySet()) {
			searchSelect.addItem(f);
			searchSelect.setItemCaption(f, fields.get(f));
		}

		searchSelect.setNewItemsAllowed(false);
		searchSelect.setValue(fields.keySet().toArray()[0]);
		searchSelect.setNullSelectionAllowed(false);

		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -7267940907574707650L;
			Filter filter = null;

			@Override
			public void buttonClick(ClickEvent event) {
				String value = (String) searchSelect.getValue();
				if (value == null) {
					return;
				}
				filter = new SimpleStringFilter(value, (String) searchTextField.getValue(), true, false);
				itemList.addFilter(filter);
			}
		});

		newSearchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -1950425791001262832L;
			@SuppressWarnings("unused")
			Filter filter = null;

			@Override
			public void buttonClick(ClickEvent event) {
				itemList.removeAllFilters();
			}
		});

		allSearchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874089475452661331L;
			Filter filter = null;

			@Override
			public void buttonClick(ClickEvent event) {
				String value = (String) searchSelect.getValue();

				if (value == null) {
					return;
				}

				SimpleStringFilter[] ssf = new SimpleStringFilter[fields.keySet().size()];

				int i = 0;

				for (String k : fields.keySet()) {
					ssf[i++] = new SimpleStringFilter(k, (String) searchTextField.getValue(), true, false);
				}

				filter = new Or(ssf);
				itemList.addFilter(filter);
			}
		});
	}

	// left content
	public void setProjectItemList() {
		Map<String, String> fields = fieldService.getFieldHeaders(Project.class.getName());
		setSearchSelect(fields);

		dataElementButton.setVisible(false);
		processingButton.setVisible(false);

		setItemList("projects", new ItemList<NsgProject>(app.getUserDataService().getProjects(), null, fields, NsgProject.class));
		itemList.setMultiSelect(false);
		itemList.setSortContainerPropertyId("name");
		itemList.setSortAscending(true);
		itemList.sort();

		itemList.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 4297368879381823674L;

			@Override
			public void itemClick(ItemClickEvent event) {
			}
		});

		itemListChangeListner = new Property.ValueChangeListener() {
			private static final long serialVersionUID = -2238113264863971216L;

			public void valueChange(ValueChangeEvent event) {
				NsgProject project = (NsgProject) itemList.getValue();
//				logger.debug("Selected a project: " + project);
				if (project != null) {
					app.getUserDataService().setProjectDbId(project.getDbId());
					logger.debug("Project ID: " + project.getDbId());
					app.getUserDataService().setDataElementDbIds(null);
					app.getUserDataService().setProcessingDbId(null);
					app.setSessionVar(VaadinTestApplication.SESS_PROJECT, project.getDbId());
					app.getMainWindow().open(new ExternalResource("data"));
				}
			}
		};
		itemList.addListener(itemListChangeListner);
	}

	public void setDataElementItemList(Long projectId) {
		Map<String, String> fields = fieldService.getFieldHeaders(DataElement.class.getName());
		
		setSearchSelect(fields);
		
		List<NsgDataElement> elements = new ArrayList<NsgDataElement>(app.getUserDataService().getProjectData(projectId));
		
		// last selected
		Set<Long> selectedDbIds = app.getUserDataService().getDataElementDbIds();

		// itemList
		setItemList("data", new ItemList<NsgDataElement>(elements, selectedDbIds, fields, NsgDataElement.class));

		itemList.setSortContainerPropertyId("subject");
		itemList.setSortAscending(true);
		itemList.sort();

		if (elements != null && elements.size() != 0 && selectedDbIds != null && selectedDbIds.size() != 0) {
			NsgDataElement nsgDe = (NsgDataElement) itemList.getValue();
			DataElement de = null;
			
			if (nsgDe != null) {
				de = nsgDe.getDataElement();
			}
			
			dataElementsChange(de, false);
		}

		if (elements == null || elements.size() == 0) {
			app.getMainWindow().showNotification("No items found");
		}

		// listeners
		itemList.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 1256387838372891429L;

			@Override
			public void itemClick(ItemClickEvent event) {
//				logger.debug("itemclicklistener");
				
				dataElementButton.setVisible(true);
				processingButton.setVisible(true);
			}
		});

		itemListChangeListner = new Property.ValueChangeListener() {
			private static final long serialVersionUID = -5847738449171911282L;

			@Override
			public void valueChange(ValueChangeEvent event) {
//				logger.debug("valuechangeevent");

				dataElementsChange(((NsgDataElement) itemList.getValue()).getDataElement(), false);
			}
		};

		itemList.addListener(itemListChangeListner);

//		logger.debug("finished setting data element item list");
	}

	/**
	 * ItemList valueChange
	 *
	 * @param changed
	 *            values
	 */
	private void dataElementsChange(DataElement dataElement, boolean use_data_btn) {
//		logger.debug("in dataelementschange");

		Set<DataElement> values = new HashSet<DataElement>();

		values.add(dataElement);

		dataElementButton.setVisible(true);
		processingButton.setVisible(true);
		
		if (values == null || values.size() == 0) {
			hideEditor();

			dataElementButton.setVisible(false);
			processingButton.setVisible(false);
		} else if (values.size() == 1 && !use_data_btn && (mainSplitPanel.getComponentCount() == 1 || mainSplitPanel.getSecondComponent() instanceof DataElementForm)) {
			// This is the first selected dataElement
			// There is only one panel OR the second panel is the dataElementForm
			
            setDataElementEditor((DataElement) values.iterator().next());
		} else if (values.size() != 0 && mainSplitPanel.getComponentCount() == 2) {
			// For any size of values
			// There are two panels
			
			// If editor is null or editor is not processingForm
			if (editor == null || !(editor instanceof ProcessingForm)) {
				// Get intersection of available applications for selected values
				List<Application> check_apps = _getApplicationIntersection(values);
				
				System.out.println(check_apps);
				
				// If the application list is empty
				if (check_apps == null) {
					// If there is one dataElement selected
					if (values.size() == 1) {
						// The dataElement has no applications bound to it so return that error
						app.getMainWindow().showNotification("This data item has no applications assigned to it.");
						
						return;
					}
				}
				
				// Create the processingEditor
				setProcessingEditor(values);
			} else if (app != null && app.getUserDataService() != null) {
				// Create list with old dataElements
				values = new HashSet<DataElement>();
				
				values.addAll((Collection<DataElement>) ((ProcessingForm) editor).getDataElements());
				
				// Create list with new dataElement added to it
				Set<DataElement> values_check = new HashSet<DataElement>();
				
				values_check.addAll((Collection<DataElement>) ((ProcessingForm) editor).getDataElements());
				values_check.add(dataElement);
				
				// Set all apps as the default
				List<Application> apps = app.getUserDataService().getAllApplications();
				
				// If at least one dataElement is selected, check applications
				if (values_check != null && values_check.size() != 0) {
					// Get intersection of applications with old and new dataElements
					List<Application> check_apps = _getApplicationIntersection(values_check);
					
					// If this list is null the new dataElement resulted in an empty intersection
					if (check_apps == null) {
						// If the length of the values_check list is 1 this was the first dataElement
						if (values_check.size() == 1) {
							// Also this dataElement did not have any applications bound to it so return that error
							app.getMainWindow().showNotification("This data item has no applications assigned to it.");
						} else {
							// There were more dataElements in the list
							// We return the list of applications for the old dataElements
							apps = _getApplicationIntersection(values);
							
							app.getMainWindow().showNotification("No more matching applications, removing data from list."); // NOTE: may also be trigger when not all input ports of the Tracula application can be matched!
						}
					} else {
						// The check_apps was not null which means that there is at least one application in the intersection
						// Add the dataElement to the list
						values.add(dataElement);
						
						// Return the apps
						apps = _getApplicationIntersection(values);
					}
				}
				
				((ProcessingForm) editor).setProcessing(values, apps);
			}
		} else {
			hideEditor();
		}

		if (values != null) {
			app.getUserDataService().setDataElementDbIds(itemList.getSelectedDbIds());
		}
	}
	
	private List<Application> _getApplicationIntersection(Set<DataElement> des) {
		ArrayList<Application> usable_apps = new ArrayList<Application>();
		
		for (DataElement de : des) {
			List<Application> these_apps = app.getUserDataService().getApplications(de);
			
			if (these_apps.size() == 0) {
				return null;
			}
			
			if (usable_apps.size() == 0) {
				usable_apps.addAll(these_apps);
				
				continue;
			}
			
			these_apps.retainAll(usable_apps);
			
			if (these_apps.size() == 0) {
				return null;
			}
		}
		
		return usable_apps;
	}

	public void setProccessingItemList() {
		Map<String, String> fields = fieldService.getFieldHeaders(Processing.class.getName());
		setSearchSelect(fields);

		dataElementButton.setVisible(false);
		processingButton.setVisible(false);

		// last selected
		Long dbId = app.getUserDataService().getProcessingDbId();

		Set<Long> selectedDbIds = new HashSet<Long>();

		if (dbId != null) {
			selectedDbIds.add(dbId);
		}

		ItemList<?> il = null;

		il = new ItemList<Processing>(app.getUserDataService().getCurrentProcessing(), selectedDbIds, fields, Processing.class);

		setItemList("processing", il);

		itemList.setMultiSelect(false);
		itemList.setSortContainerPropertyId("date");
		itemList.setSortAscending(false);
		itemList.sort();

		itemList.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 2915674098884996672L;

			@Override
			public void itemClick(ItemClickEvent event) {
			}
		});

		itemListChangeListner = new Property.ValueChangeListener() {
			private static final long serialVersionUID = -4618532987941924650L;

			public void valueChange(ValueChangeEvent event) {
				Processing processing = (Processing) itemList.getValue();
				app.getUserDataService().setProcessingDbId(processing.getDbId());
				if (processing != null) {
					setProcessingStatusEditor(processing.getDbId());

					showEditor();
				}
			}
		};

		itemList.addListener(itemListChangeListner);
	}
	
	private void setDataElementEditor(DataElement dataElement) {
        DataElementForm def = new DataElementForm(app.getFieldService());
        
        if (dataElement != null) {
            final List<NsgProperty> metaData = app.getUserDataService().getMetaData(dataElement);
            
            if (metaData != null) {
                def.setDataElement(dataElement, metaData);
                
                setEditor(def);
            }
        }
    }

	private void setProcessingEditor(Set<DataElement> dataElements) {
//		logger.debug("in setprocessingeditor");

		final ProcessingForm pf = new ProcessingForm();

		List<Application> apps = null;

		if (dataElements != null && dataElements.size() != 0) {
			apps = app.getUserDataService().getApplications(((DataElement) dataElements.toArray()[0]));
		} else {
			apps = app.getUserDataService().getAllApplications();
		}

		pf.setProcessing(dataElements, apps);

		pf.addListener(new Listener() {
			private static final long serialVersionUID = -2040377891185846134L;

			@Override
			public void componentEvent(Event event) {
				if (((Button) event.getSource()).getCaption().equals(ProcessingForm.SUBMIT)) {
					Processing processing = (Processing) ((Button) event.getSource()).getData();
					
					JSONArray submits = new JSONArray();

					submits = app.getProcessingService().prepareSubmission(processing,
							((VaadinTestApplication) getApplication()).getUserDataService().getDataElementDbIds());

					// TODO check disabled
					// String message =
					// app.getUserDataService().checkApplicationInput(processing.getApplication(),
					// submits);
					//
					// if (message != null) {
					// logger.debug("Error before submit.");
					//
					// app.getMainWindow().showNotification(message.replaceAll("\n",
					// "<br />"), Notification.TYPE_ERROR_MESSAGE);
					// } else {
					Long processingDbId = app.getProcessingService().submit(app.getUserDataService().getUserId(),
							app.getUserDataService().getProjectDbId(), processing, submits);

					if (processingDbId != null) {
						app.getUserDataService().setProcessingDbId(processingDbId);

						app.getMainWindow().open(new ExternalResource("processing"));
					} else {
						app.getMainWindow().showNotification("Failed to submit processing");
					}
					// }
				}
			}
		});

		setEditor(pf);
	}

	private void setProcessingStatusEditor(Long processingDbId) {
		logger.info("statusForm for processingDbId " + ((processingDbId != null) ? processingDbId : "null"));
		final ProcessingStatusForm psf = new ProcessingStatusForm(app.getUserDataService(), app.getProcessingService());

		if (processingDbId != null) {
			psf.setProcessingStatus(app.getProcessingService().getProcessingStatus(app.getUserDataService().getProcessing(processingDbId),
					app.getUserDataService().getUserId(), app.getUserDataService().getLiferayId(), false));
			setEditor(psf);
		} else {
			hideEditor();
		}

		psf.addListener(new Listener() {
			private static final long serialVersionUID = 3916793368393788845L;

			@Override
			public void componentEvent(Event event) {
				DisplayProcessingStatus processingStatus = (DisplayProcessingStatus) ((AbstractComponent) event.getSource()).getData();
//				logger.debug(processingStatus);
				
				if (processingStatus != null) {
					psf.setProcessingStatus(app.getProcessingService().getProcessingStatus(
							app.getUserDataService().getProcessing(processingStatus.getProcessing().getDbId()), app.getUserDataService().getUserId(),
							app.getUserDataService().getLiferayId(), true));
					setEditor(psf);

					logger.debug("About to attach to " + psf);
				}
			}
		});
	}

	public void setItemList(String name, ItemList<?> itemList) {
		Project project = ((VaadinTestApplication) getApplication()).getUserDataService().getProject();

		if (name != null && !name.equals("projects") && !name.equals("processing") && project != null) {
			mainSplitPanel.setCaption(project.getName() + " " + name);
		}

		this.itemList = itemList;

		mainSplitPanel.removeAllComponents();
		mainSplitPanel.addComponent(itemList);
		mainSplitPanel.setSplitPosition(100);
	}

	// editor
	public void setEditor(AbstractComponent form) {
		mainSplitPanel.removeComponent(editor);
		editor = form;
		showEditor();
	}

	private void hideEditor() {
		if (mainSplitPanel.getComponentCount() == 2) {
			mainSplitPanel.removeComponent(editor);
		}
		mainSplitPanel.setSplitPosition(100);
	}

	private void showEditor() {
		if (mainSplitPanel.getComponentCount() != 2) {
			mainSplitPanel.addComponent(editor);
			mainSplitPanel.setSplitPosition(50);
		}
	}

	@Override
	public void attach() {
		super.attach();

		this.app = (VaadinTestApplication) getApplication();
	}

	// build components
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// topPanel
		topPanel = buildTopPanel();
		mainLayout.addComponent(topPanel);
		mainLayout.setExpandRatio(topPanel, 1.0f);
		mainLayout.setComponentAlignment(topPanel, new Alignment(33));

		// horizontal rule
		Label hr = new Label("<hr/>", Label.CONTENT_XHTML);
		hr.addStyleName("horizontal-separator");
		hr.setWidth("100%");
		hr.setHeight("2px");
		mainLayout.addComponent(hr);
		mainLayout.setComponentAlignment(hr, new Alignment(33));

		// mainSplitPanel
		mainSplitPanel = buildMainSplitPanel();
		mainLayout.addComponent(mainSplitPanel);
		mainLayout.setExpandRatio(mainSplitPanel, 1.0f);

		return mainLayout;
	}

	private HorizontalLayout buildTopPanel() {
		topPanel = new HorizontalLayout();
		topPanel.setStyleName("topPanel");
		topPanel.setImmediate(true);
		topPanel.setWidth("100%");
		topPanel.setHeight("32px");
		topPanel.setMargin(false);
		topPanel.setSpacing(false);

		HorizontalLayout topPanelLeft = buildTopPanelLeft();
		topPanel.addComponent(topPanelLeft);
		topPanel.setComponentAlignment(topPanelLeft, Alignment.MIDDLE_LEFT);

		HorizontalLayout topPanelMid = buildTopPanelMid();
		topPanel.addComponent(topPanelMid);
		topPanel.setComponentAlignment(topPanelMid, Alignment.MIDDLE_CENTER);

		pagePanel = buildTopPanelRight();
		topPanel.addComponent(pagePanel);
		topPanel.setComponentAlignment(pagePanel, Alignment.MIDDLE_RIGHT);

		return topPanel;
	}

	private HorizontalLayout buildTopPanelLeft() {
		HorizontalLayout topPanelLeft = new HorizontalLayout();
		topPanelLeft.setImmediate(true);
		topPanelLeft.setWidth("-1px");
		topPanelLeft.setHeight("-1px");
		topPanelLeft.setMargin(false);
		topPanelLeft.setSpacing(false);

		// updateCatButton = new NativeButton();
		// updateCatButton.setCaption("synchronize data");
		// updateCatButton.setVisible(false);
		// topPanelLeft.addComponent(updateCatButton);
		// topPanelLeft.setComponentAlignment(updateCatButton, new
		// Alignment(33));

		projectsButton = new NativeButton();
		projectsButton.setImmediate(true);
		projectsButton.setWidth("-1px");
		projectsButton.setHeight("-1px");
		projectsButton.setHtmlContentAllowed(true);

		elementsButton = new NativeButton();
		elementsButton.setCaption("data");
		elementsButton.setImmediate(true);
		elementsButton.setWidth("-1px");
		elementsButton.setHeight("-1px");
		elementsButton.setHtmlContentAllowed(true);

		processingsButton = new NativeButton();
		processingsButton.setImmediate(true);
		processingsButton.setWidth("-1px");
		processingsButton.setHeight("-1px");
		processingsButton.setHtmlContentAllowed(true);

		return topPanelLeft;
	}

	private HorizontalLayout buildTopPanelMid() {
		// common part: create layout
		HorizontalLayout topPanelMid = new HorizontalLayout();
		topPanelMid.setImmediate(false);
		topPanelMid.setWidth("-1px");
		topPanelMid.setHeight("-1px");
		topPanelMid.setMargin(false);
		topPanelMid.setSpacing(true);

		// searchLabel
		searchLabel = new Label();
		searchLabel.setImmediate(false);
		searchLabel.setWidth("-1px");
		searchLabel.setHeight("-1px");
		searchLabel.setValue("search ");
		topPanelMid.addComponent(searchLabel);
		topPanelMid.setComponentAlignment(searchLabel, Alignment.MIDDLE_CENTER);

		// searchSelect
		searchSelect = new Select();
		searchSelect.setImmediate(false);
		searchSelect.setWidth("-1px");
		searchSelect.setHeight("-1px");
		topPanelMid.addComponent(searchSelect);
		topPanelMid.setComponentAlignment(searchSelect, Alignment.MIDDLE_CENTER);

		// label_1
		label_1 = new Label();
		label_1.setImmediate(false);
		label_1.setWidth("-1px");
		label_1.setHeight("-1px");
		label_1.setValue(" with ");
		topPanelMid.addComponent(label_1);
		topPanelMid.setComponentAlignment(label_1, Alignment.MIDDLE_CENTER);

		// searchTextField
		searchTextField = new TextField();
		searchTextField.setImmediate(false);
		searchTextField.setWidth("-1px");
		searchTextField.setHeight("-1px");
		topPanelMid.addComponent(searchTextField);
		topPanelMid.setComponentAlignment(searchTextField, Alignment.MIDDLE_CENTER);

		searchButton = new NativeButton();
		searchButton.setCaption("refine search");
		topPanelMid.addComponent(searchButton);
		topPanelMid.setComponentAlignment(searchButton, Alignment.MIDDLE_CENTER);

		allSearchButton = new NativeButton();
		allSearchButton.setCaption("search in all columns");
		topPanelMid.addComponent(allSearchButton);
		topPanelMid.setComponentAlignment(allSearchButton, Alignment.MIDDLE_CENTER);

		newSearchButton = new NativeButton();
		newSearchButton.setCaption("new search");
		topPanelMid.addComponent(newSearchButton);
		topPanelMid.setComponentAlignment(newSearchButton, Alignment.MIDDLE_CENTER);

		return topPanelMid;
	}

	private HorizontalLayout buildTopPanelRight() {
		// common part: create layout
		pagePanel = new HorizontalLayout();
		pagePanel.setStyleName("topPanel");
		pagePanel.setImmediate(false);
		pagePanel.setWidth("-1px");
		pagePanel.setHeight("-1px");
		pagePanel.setSpacing(true);
		
		// dataElementButton
        dataElementButton = new NativeButton();
        dataElementButton.setCaption("view");
        dataElementButton.setImmediate(true);
        dataElementButton.setWidth("-1px");
        dataElementButton.setHeight("-1px");
        pagePanel.addComponent(dataElementButton);
        pagePanel.setComponentAlignment(dataElementButton, new Alignment(34));
        dataElementButton.setVisible(false);

		// processingButton
		processingButton = new NativeButton();
		processingButton.setCaption("use data");
		processingButton.setImmediate(true);
		processingButton.setWidth("-1px");
		processingButton.setHeight("-1px");
		pagePanel.addComponent(processingButton);
		pagePanel.setComponentAlignment(processingButton, new Alignment(34));

		return pagePanel;
	}

	private HorizontalSplitPanel buildMainSplitPanel() {
		// common part: create layout
		mainSplitPanel = new HorizontalSplitPanel();
		mainSplitPanel.setImmediate(false);
		mainSplitPanel.setWidth("100.0%");
		mainSplitPanel.setHeight("100.0%");
		mainSplitPanel.setMargin(true);
		mainSplitPanel.setSplitPosition(60);

		// itemList
		mainSplitPanel.addComponent(new Table());

		// editor
		editor = new Form();
		editor.setImmediate(false);
		editor.setWidth("100.0%");
		editor.setHeight("100.0%");
		mainSplitPanel.addComponent(editor);

		return mainSplitPanel;
	}
}
