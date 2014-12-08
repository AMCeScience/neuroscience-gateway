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
package nl.amc.biolab.nsg.display.control;

import nl.amc.biolab.datamodel.objects.User;
import nl.amc.biolab.nsg.display.VaadinTestApplication;
import nl.amc.biolab.nsg.display.component.LoginUI;
import nl.amc.biolab.nsg.display.component.MainUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class MainControl {
    Logger logger = LoggerFactory.getLogger(MainControl.class);

    private VaadinTestApplication app;

    public Window mainWindow;

    private VerticalLayout layout = new VerticalLayout();

    private final MainControl mainControl = this;
    private MainUI mainUI;

    private MainControl() {
        layout.setImmediate(false);
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setMargin(false);
    }

    public MainControl(VaadinTestApplication app) {
        this();
        this.app = app;
        this.mainWindow = app.getMainWindow();
        
        layout.setWidth("100%");
        layout.setHeight("100%");
        
        User user = null;
        
        if (app.getUserDataService() != null) {
            user = app.getUserDataService().getUser();
        }
        
        init(user);
    }

    public MainControl(VaadinTestApplication app, Window mainWindow, User user) {
        this();
        this.app = app;
        this.mainWindow = mainWindow;
        init(user);
    }

    public void init(User user) {
        if (app.getUser() == null || user == null) {    // no liferay user or empty backend user 
            Label l = new Label("No XNAT user information found. Please contact the administrator.", Label.CONTENT_XHTML);
            layout.addComponent(l);
        } else if (app.getUserDataService() != null) {
            if (app.getUserDataService().checkAuthentication(user)) { 
                layout.removeAllComponents();
                
                mainUI = new MainUI(this);
                
                layout.addComponent(mainUI);
            } else { // user has no xnat password in catalog
                layout.removeAllComponents();
                
                LoginUI loginUI = new LoginUI(this);
                
                loginUI.addListener(new Listener() {
					private static final long serialVersionUID = -8728443364176948015L;

					@Override
                    public void componentEvent(Event event) { // login failed
                        mainControl.init((User) ((Button) event.getSource()).getData());
                    }
                });
                
                layout.addComponent(new LoginUI(this));
            }
        } else {
            logger.error("no UserDataService");
        }

        mainWindow.removeAllComponents();
        mainWindow.addComponent(layout);

        app.setMainWindow(mainWindow);
    }

    public void update() {
        logger.debug("MainControl.update is about to initialize the interface");
        
        if (mainUI != null) {
            mainUI.init(true);
            
            logger.debug("MainControl.update finished initialization.");
        }
    }
}
