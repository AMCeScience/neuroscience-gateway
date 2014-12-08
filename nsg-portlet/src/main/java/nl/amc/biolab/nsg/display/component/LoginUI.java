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

import nl.amc.biolab.datamodel.objects.User;
import nl.amc.biolab.nsg.display.VaadinTestApplication;
import nl.amc.biolab.nsg.display.control.MainControl;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.PasswordField;

/**
 * 
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class LoginUI extends CustomComponent {
	private static final long serialVersionUID = 7111231961055959642L;
//	private static Logger logger = LoggerFactory.getLogger(LoginUI.class);

	private VaadinTestApplication app = (VaadinTestApplication) getApplication();

	private GridLayout layout = new GridLayout();
	private Form form = new Form();
//	private final LoginUI loginUI = this;

	public LoginUI(final MainControl mainControl) {
		setWidth("100%");
		setHeight("300px");

		layout.setWidth("100%");
		layout.setHeight("300px");
		layout.addComponent(form);
		
		setCompositionRoot(layout);

		final PasswordField xnatPassword = new PasswordField("Please enter your XNAT password");
		
		xnatPassword.setRequired(true);
		
		form.addField("xnatPassword", xnatPassword);

		final Button okButton = new Button("ok");
		
		okButton.setClickShortcut(KeyCode.ENTER);
		okButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -6535226372165482804L;

			public void buttonClick(ClickEvent event) {
				User user = null;
				
				user = login((String) xnatPassword.getValue());
				xnatPassword.setValue("");
				
				if (user == null) {
					return;
				}
				
				okButton.setData(user);
				mainControl.init(user);
				
				app.getMainWindow().executeJavaScript("window.location.reload();");
			}
		});

		form.getFooter().addComponent(okButton);
	}

	/**
	 * 
	 * @param password
	 * @return User object if login ok
	 */
	private User login(String password) {
		if (app == null) {
			return null;
		}

		String screenName = ((com.liferay.portal.model.User) app.getUser()).getScreenName();
		
		if (screenName == null) {
			return null;
		}
		
		try {
			app.getUserDataService().setPassword(password);
			app.getUserDataService().xnatLogin();
		} catch (RuntimeException e) {
			if (e.getMessage().equals("No Password.") || e.getMessage().equals("Wrong Password.")) {
				app.getMainWindow().showNotification("Please (re)enter your XNAT password");
				
				return null;
			}
		}

		return (app.getUserDataService() != null) ? app.getUserDataService().getUser() : null;
	}

	@Override
	public void attach() {
		super.attach();
		
		this.app = (VaadinTestApplication) getApplication();
		
		if (app.getUser() == null) {
			layout.removeAllComponents();
		}
	}
}
