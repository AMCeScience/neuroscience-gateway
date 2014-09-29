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
package nl.amc.biolab.nsg.display;

import java.util.Enumeration;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import nl.amc.biolab.nsg.display.component.LoginUI;
import nl.amc.biolab.nsg.display.control.MainControl;
import nl.amc.biolab.nsg.display.service.FieldService;
import nl.amc.biolab.nsg.display.service.ProcessingService;
import nl.amc.biolab.nsg.display.service.UserDataService;
import nl.amc.biolab.xnat.tools.ErrorMessages;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.Terminal.ErrorListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * The main (Vaadin) Application TODO rename and refactor
 *
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class VaadinTestApplication extends Application implements PortletRequestListener {

    private static final long serialVersionUID = 7208270174534740759L;

    public static final String VERSION = "0.8";

    public static final String PROCESSING_URL = "/processing";
    public static final String DATA_URL = "/data";
    public static final String PROJECTS_URL = "/projects";
    public static final String ADMIN_URL = "/admin";

    public static final String SESS_PROJECT = "proj";
    public static final String SESS_DATA = "data";
    public static final String SESS_PROCESSING = "proc";

    public static final int ADMIN = 5;
    public static final int PROCESSING = 4;
    public static final int DATA = 3;
    public static final int PROJECTS = 2;
    public static final int HOME = 1;

    private final Logger logger = Logger.getLogger(VaadinTestApplication.class);

    private UserDataService userDataService;
    private FieldService fieldService = new FieldService();
    private ProcessingService processingService;

    private final Application app = this;

    private int page = HOME;

    private MainControl mainControl;

    private PortletSession portletSession;

    private boolean adminURL;

    @Override
    public void init() {
        logger.setLevel(Level.DEBUG);
        setErrorHandler(new ErrorListener() {
            @Override
            public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
                if (event.getThrowable().getCause() instanceof InvalidValueException) {
                    return;
                }
                logger.error("Error occured");
                event.getThrowable().printStackTrace();
//				if (userDataService != null) {
//					try {
//						userDataService.closeSession();
//					} catch (Exception e) {
//					}
//					try {
//						userDataService.openSession();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
            }
        });

        app.setLogoutURL("/");
        app.setTheme("nsg_theme");

        setMainWindow(new Window("AMC nsg portlet"));
        getMainWindow().setWidth("100%");
        getMainWindow().setHeight("300px");

        if (getUser() == null) {
            userDataService = null;
            return;
        }

        if (userDataService == null || userDataService.xnatLogin() == null) {
        	ErrorMessages errors = new ErrorMessages();
        	
            try {
                setUserDataService(((User) getUser()).getScreenName(), ((User) getUser()).getUserId(), false);
//				if(getPage() == DATA && this.getUserDataService() != null && this.getUserDataService().getUserId() == 0L && getUserDataService().getProjectDbId() == null) {
//					this.getUserDataService().xnatLogin();
//				} 
            } catch (RuntimeException e) { // nsgdm throws RuntimeExceptions
                if (e == null || e.getMessage() == null || !(e.getMessage().equals(errors.noPassword()) || e.getMessage().equals(errors.wrongPassword()) || e.getMessage().equals(errors.noUser()))) {
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setWidth("100%");
                    layout.setHeight("300px");
                    Label label = new Label("Server error. Please contact the administrator");
                    layout.removeAllComponents();
                    layout.addComponent(label);
                    getMainWindow().removeAllComponents();
                    getMainWindow().addComponent(layout);
                    return;
                } else if (e.getMessage().equals(errors.noPassword()) || e.getMessage().equals(errors.wrongPassword())) {
                    getMainWindow().removeAllComponents();
                    getMainWindow().showNotification("Please enter your XNAT password");
                } else if (e.getMessage().equals(errors.noUser())) { //nsgdm api not returning this message?
                    getMainWindow().showNotification(errors.noUser());
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setWidth("100%");
                    layout.setHeight("300px");
                    Label label = new Label("Loading page.");
                    layout.addComponent(label);
                    label = new Label("Please contact the administrator for XNAT user setup");
                    layout.removeAllComponents();
                    layout.addComponent(label);
                    getMainWindow().removeAllComponents();
                    getMainWindow().addComponent(layout);
                    return;
                }
            }
//		} else {
//			userDataService.openSession();  // TODO: it is initialized in the contructor already I suppose. To check if it works like this.
        }
        logger.debug("Creating processing service object.");
        processingService = new ProcessingService(userDataService);

        logger.debug("Created vaadin application. Proceed to create display components.");
        getMainWindow().removeAllComponents();
        this.mainControl = new MainControl(this);
        logger.debug("Finished creating the application and the components.");
    }

    @Override
    public void close() {
        logger.info("Shutting down an instance of NSG portlet application.");
        if (userDataService != null) {
            try {
                userDataService.closeSession();
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        if (processingService != null) {
            try {
                processingService.shutdown();
            } catch (RuntimeException e) {
                logger.debug(e.getMessage());
            }
        }
        super.close();
    }

    @Override
    public void onRequestStart(PortletRequest request, PortletResponse response) {
        this.portletSession = request.getPortletSession();
//        logger.debug("There is a request: ");
        final Object phase = request.getAttribute("javax.portlet.lifecycle_phase");
//                final Enumeration<String> attributeNames = request.getAttributeNames();
//                while (attributeNames.hasMoreElements()) {
//                    final String attr = attributeNames.nextElement();
//                    logger.debug("Attribute: ("+attr+", "+request.getAttribute(attr)+")");
//                }
//                final Enumeration<String> paramNames = request.getParameterNames();
//                while (paramNames.hasMoreElements()) {
//                    final String param = paramNames.nextElement();
//                    logger.debug("Attribute: ("+param+", "+request.getParameter(param)+")");
//                }
        logger.debug("Lifecycle phase is: " + phase);
        if (getUser() == null) {
            logger.debug("User is null, so checking theme display.");
            PermissionChecker permissionChecker = null;
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            if (themeDisplay != null) {
                permissionChecker = themeDisplay.getPermissionChecker();
            }

            if (permissionChecker == null || !permissionChecker.isSignedIn()) {
                setUser(null);
                return;
            }

            setUser(themeDisplay.getUser());
        }

        logger.debug("Setting the URL and page.");
        if (PortalUtil.getCurrentURL(request).contains(PROJECTS_URL)) {
            this.page = PROJECTS;
        } else if (PortalUtil.getCurrentURL(request).contains(DATA_URL)) {
            this.page = DATA;
        } else if (PortalUtil.getCurrentURL(request).contains(PROCESSING_URL)) {
            this.page = PROCESSING;
        } else if (PortalUtil.getCurrentURL(request).contains(ADMIN_URL)) {
            this.page = PROCESSING;
            this.adminURL = true;
        }

        if (getUser() != null && getUserDataService() != null) {
            HttpServletRequest req = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request));

            if (!(PortalUtil.getCurrentURL(request).contains(PROJECTS_URL)
                    || PortalUtil.getCurrentURL(request).contains(DATA_URL)
                    || PortalUtil.getCurrentURL(request).contains(PROCESSING_URL)
                    || PortalUtil.getCurrentURL(request).contains(ADMIN_URL))) {
                return;
            }

            @SuppressWarnings("unchecked")
			Enumeration<String> en = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)).getAttributeNames();
            while (en.hasMoreElements()) {
                String n = en.nextElement();
            }

            try {
                if (portletSession.getAttribute(SESS_PROJECT, PortletSession.APPLICATION_SCOPE) != null) {
                    final Long projectId = (Long) portletSession.getAttribute(SESS_PROJECT, PortletSession.APPLICATION_SCOPE);
                    getUserDataService().setProjectDbId(projectId);
                    logger.debug("Selected Project is: " + projectId);
                }

                if (portletSession.getAttribute(SESS_PROCESSING, PortletSession.APPLICATION_SCOPE) != null) {
                    getUserDataService().setProcessingDbId((Long) portletSession.getAttribute(SESS_PROCESSING, PortletSession.APPLICATION_SCOPE));
                    logger.debug("Processing session is read.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (PortalUtil.getCurrentURL(request).contains("wsver") && mainControl != null) {
            	ErrorMessages errors = new ErrorMessages();
            	
                try {
                    if (getPage() == DATA && (this.getUserDataService().getUserId() == 0L || getUserDataService().getProjectDbId() != null)) {
                        this.getUserDataService().xnatLogin();
                    }
                    mainControl.update();
                } catch (RuntimeException e) { // nsgdm throws RuntimeExceptions
                    if (e == null || e.getMessage() == null || !(e.getMessage().equals(errors.noPassword()) || e.getMessage().equals(errors.wrongPassword()) || e.getMessage().equals(errors.noUser()))) {
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.setWidth("100%");
                        layout.setHeight("300px");
                        Label label = new Label("Server error. Please sign out and sign in again, and if the problem persists, contact the administrator.");
                        layout.removeAllComponents();
                        layout.addComponent(label);
                        getMainWindow().removeAllComponents();
                        getMainWindow().addComponent(layout);
                        e.printStackTrace();
                        return;
                    } else if (e.getMessage().equals(errors.noPassword()) || e.getMessage().equals(errors.wrongPassword())) {
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.removeAllComponents();
                        LoginUI loginUI = new LoginUI(mainControl);
                        loginUI.addListener(new Component.Listener() {
                            @Override
                            public void componentEvent(Component.Event event) { // login failed
                                mainControl.init((nl.amc.biolab.datamodel.objects.User) ((Button) event.getSource()).getData());
                            }
                        });
                        layout.addComponent(new LoginUI(mainControl));

                        getMainWindow().removeAllComponents();
                        getMainWindow().showNotification("Your XNAT password was not recognized.");
                        getMainWindow().addComponent(layout);

                    } else if (e.getMessage().equals(errors.noUser())) { //nsgdm api not returning this message?
                        getMainWindow().showNotification(errors.noUser());
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.setWidth("100%");
                        layout.setHeight("300px");
                        Label label = new Label("Loading page.");
                        layout.addComponent(label);
                        label = new Label("Please contact the administrator for XNAT user setup");
                        layout.removeAllComponents();
                        layout.addComponent(label);
                        getMainWindow().removeAllComponents();
                        getMainWindow().addComponent(layout);
                        return;
                    }
                }
            }
        }
    }

    public void setSessionVar(String key, Object value) {
        portletSession.setAttribute(key, value, PortletSession.APPLICATION_SCOPE);
    }

    @Override
    public void onRequestEnd(PortletRequest request, PortletResponse response) {
    }

    /**
     *
     * @return userDataService for current user
     */
    public UserDataService getUserDataService() {
        return userDataService;
    }

    /**
     *
     * set userDataService for current user</br>
     *
     * @param liferayId
     */
    public void setUserDataService(String screenName, Long liferayId, boolean xnatLogin) {
        if (this.getUserDataService() != null) {
            this.getUserDataService().closeSession();
        }
        this.userDataService = new UserDataService(screenName, liferayId, xnatLogin);   // will open a new session inside
    }

    public FieldService getFieldService() {
        return fieldService;
    }

    public void setFieldService(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    public ProcessingService getProcessingService() {
        return processingService;
    }

    public void setProcessingService(ProcessingService processingService) {
        this.processingService = processingService;
    }

    public void showNotification(MainControl mainControl, String notification) {
        getMainWindow().showNotification(notification);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getVersion() {
        return "1.0";
    }

    public boolean isAdminURL() {
        return adminURL;
    }

    /**
     * @param screenName
     * @return 0 if liferayId not found else liferayId
     */
    public long getLiferayId(String screenName) {
        try {
            int userCount = UserLocalServiceUtil.getUsersCount();
            List<User> users;
            users = UserLocalServiceUtil.getUsers(0, userCount);

            for (User user : users) {
                if (user.getScreenName().equals(screenName)) {
                    return user.getUserId();
                }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
