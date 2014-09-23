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

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.amc.biolab.datamodel.objects.Application;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.datamodel.objects.Project;
import nl.amc.biolab.datamodel.objects.User;

import org.apache.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * 
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 * @param <T>
 */
public class ItemList<T> extends Table {
	private static final long serialVersionUID = -8373501943404597857L;

	Logger logger = Logger.getLogger(ItemList.class);

	private Class<T> clazz;

	private Set<Long> selectedDbIds;

	private ValueChangeListener valueChangeListener;

	private final ItemList<T> itemList = this;

	private ItemList() {
	}

	public ItemList(List<T> items, Set<Long> selectedDbIds, Map<String, String> fields, Class<T> clazz) {
		this();
		
		this.clazz = clazz;
		this.selectedDbIds = selectedDbIds;

		setImmediate(true);
		setWidth("100.0%");
		setHeight("550px");
		setSelectable(true);
		setMultiSelect(false);
		setNullSelectionAllowed(false);
		setDescription("click on a header to sort");

		final BeanItemContainer<T> bic = new BeanItemContainer<T>(clazz);
		Iterator<String> iter = fields.keySet().iterator();
		while(iter.hasNext()) {
			String f = iter.next();
			if (!f.contains(".")) {
				addContainerProperty(f, String.class, "");
			} else {
				bic.addNestedContainerProperty(f);

			}
			setColumnHeader(f, fields.get(f));
            // TODO: See if this gives all columns the same width!
            setColumnExpandRatio(f, 1);
		}
        
        setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			@Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                if (! (itemId instanceof Processing)){
                    return null;
                }
                if ("date".equals(propertyId)) {
                    return "Start date: "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(((Processing) itemId).getDate());
                } else if ("project.name".equals(propertyId)) {
                    final Project project = ((Processing) itemId).getProject();
                    return project.getName() +": "+ project.getDescription();
                } else if ("description".equals(propertyId)) {
                    return ((Processing) itemId).getDescription();
                } else if ("application.name".equals(propertyId)) {
                    final Application application = ((Processing) itemId).getApplication();
                    return application.getName() +" ("+ application.getDescription()+")";
                } else if ("status".equals(propertyId)) {
                    return ((Processing) itemId).getStatus();
                } else if ("user".equals(propertyId)) {
                    final User owner = ((Processing) itemId).getUser();
                    String user = owner.getFirstName() + " " + owner.getLastName();
                    return user;
                }  
                return null;
            }
        });

		bic.addAll(items);
		setContainerDataSource(bic);
		setVisibleColumns(fields.keySet().toArray());

		// selected
		if (items != null && items.size() != 0 && this.selectedDbIds != null && this.selectedDbIds.size() != 0) {
			try {
				Method m = clazz.getMethod("getDbId"); //TODO get nsgdm API to provide the public method
					for(Object de: items) {
						if(this.selectedDbIds.contains(m.invoke(de))) {
							this.select(de);
						}
					}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		if(valueChangeListener != null) {
			removeListener(valueChangeListener);
		}
		valueChangeListener = new Property.ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				itemList.selectedDbIds = new HashSet<Long>();
				try {
					Method m = itemList.clazz.getMethod("getDbId"); //TODO let NSGDM api provide public method
					if(getValue() instanceof Set<?>) {
						for(Object o: ((Set<T>) getValue()).toArray()) {
							itemList.selectedDbIds.add((Long) m.invoke(o));
						}
					} else if(getValue() != null) {
						itemList.selectedDbIds.add((Long) m.invoke((T) getValue()));
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		};
		addListener(valueChangeListener);
	}

	public Set<Long> getSelectedDbIds() {
		return selectedDbIds;
	}

	public void setSelectedDbIds(Set<Long> selected) {
		this.selectedDbIds = selected;
	}

	@SuppressWarnings("unchecked")
	public void removeAllFilters() {
		((BeanItemContainer<T>) getContainerDataSource()).removeAllContainerFilters();
	}

	@SuppressWarnings("unchecked")
	public void removeFilter(Filter filter) {
		((BeanItemContainer<T>) getContainerDataSource()).removeContainerFilter(filter);
	}

	@SuppressWarnings("unchecked")
	public void addFilter(Filter filter) {
		((BeanItemContainer<T>) getContainerDataSource()).addContainerFilter(filter);
	}
}
