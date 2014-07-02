/*
 * Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.


 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.amc.biolab.nsg.emailer;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.amc.biolab.nsg.ProcessingStatus;
import nl.amc.biolab.nsg.errors.ProcessingManagerException;
import nl.amc.biolab.nsgdm.Error;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Submission;
import nl.amc.biolab.nsgdm.SubmissionIO;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.util.emailer.Emailer;

/**
 *
 * @author mahdi
 */
public class NSGEmailer {
    private Emailer emailer;
    
    // TODO: read from config file or from catalog
    private static final String ADMIN_PANEL = "https://neuro.ebioscience.amc.nl/portal/web/nsg/admin";
    private static final int poolSize = 5;
    private final String NSG_ADMIN_EMAIL; // = "NSG <m.jaghouri@amc.uva.nl>"; //"NSG Support Team <support@ebioscience.amc.nl>";
    
    private ExecutorService pool = Executors.newFixedThreadPool(poolSize);

    public NSGEmailer(String adminEmail, String mailServer) {
        NSG_ADMIN_EMAIL = adminEmail;
        try {
            this.emailer = new Emailer(mailServer); // "mail.amc.nl"
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendToAdmin (final List<ProcessingManagerException> exceptions) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Processing proc = null;
                String subjectConcat = "";
                StringBuilder message = new StringBuilder();
                StringBuilder subject = new StringBuilder();
                message.append("Dear Neuroscience Gateway administrator,");
                for (ProcessingManagerException pe : exceptions) {
                    if (proc == null || proc != pe.getProcessing()) {
                        proc = pe.getProcessing();
                        User user = proc.getUsers().iterator().next();
                        subject.append(subjectConcat).append("processing "+proc.getDbId()+" from " + user.getFirstName()+" "+user.getLastName());
                        subjectConcat = " and ";
                        message.append("\r\n\r\nThe processing '").append(proc.getDescription()).append("' by user ").append(user.getFirstName()).append(" ").append(user.getLastName())
                           .append("' has faced a problem.\r\n\r\n");
                    }
                    message.append("* Submission ID=").append(pe.getSubmissionId()).append(": Error ").append(pe.getErrorCode().getIntValue()).append(" during '").append(pe.getErrorCode().getOccasion().getDescription()).append("'\r\n")
                           .append(pe.getMessage()).append("\r\n\r\n");
                }
                message.append("You can access the admin page by clicking on\r\n    " + ADMIN_PANEL); //) + "?processingID=").append(proc.getDbId()).append("&userID=").append(user.getDbId());
                message.append("\r\n\r\nGood luck.");
                emailer.postMail(NSG_ADMIN_EMAIL, NSG_ADMIN_EMAIL, "[NSG] Problems in "+subject, message.toString());
            }            
        });
    }
    
    public void notifyUser (final Processing pr) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                User user = pr.getUsers().iterator().next();
                final String name = user.getFirstName()+" "+user.getLastName();
                StringBuilder message = new StringBuilder();
                message.append("Dear ").append(name).append(",\r\n\r\n");
                message.append("The processing \"").append(pr.getDescription()).append("\" with application "+pr.getApplication().getName()+" has finished with the following results:\r\n\r\n");
                for (Submission sub : pr.getSubmissions()) {
                    // TODO change sub.getName to something more user-friendly
                    // TODO add the reason for failed submissions:             sub.getErrors().get(0).getDescription();
                    // TODO add colors
                    final String status = sub.getStatus();
                    message.append("    * ");
                    char separator = '(';
                    for (SubmissionIO io : sub.getSubmissionIOs()) {
                        if (io.getPort().getIOType().equalsIgnoreCase("input")) {
                            message.append(separator).append(io.getDataElement().getName());
                            separator = ',';
                        }
                    }
                    message.append("): ").append(status).append("\r\n");
                    if (status.equals(ProcessingStatus.STAT_FAILED)) {
                        final List<Error> errors = sub.getErrors();
                        int last = errors.size()-1;
                        message.append("      Reason: ").append(errors.get(last).getDescription()).append("\r\n\r\n");
                    }
                }
                message.append("\r\nKind Regards.");
                emailer.postMail(NSG_ADMIN_EMAIL, name+" <"+user.getEmail()+">", 
                        "[NSG] Status update for \""+pr.getDescription()+"\"" , message.toString());
            }
        });
    }

    public void shutdown() {
        pool.shutdown();
    }
}
