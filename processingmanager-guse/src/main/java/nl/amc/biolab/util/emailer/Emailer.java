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

package nl.amc.biolab.util.emailer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * based on http://www.developerfusion.com/code/1975/sending-email-using-smtp-and-java/
 * @author mahdi
 */
public class Emailer {

    private static final String MAILER = "AMC Gateway mailer";
    private Socket smtpSocket = null;
    private DataOutputStream os = null;
    private DataInputStream is = null;
    InetAddress mailHostName;
    int mailPort;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException {
        // this is a test
        Emailer emailer = new Emailer("mail.amc.nl"); 
        emailer.postMail("Gateway Admin <test@ebioscience.amc.nl>", "test@ebioscience.amc.nl", "Test email short", "This is the message.\r\n Second line.");
    }

    public Emailer(String mailHostName, int mailPort) throws UnknownHostException {
        this.mailHostName = InetAddress.getByName(mailHostName);
        this.mailPort = mailPort;
    }

    public Emailer(String mailHostName) throws UnknownHostException {
        this (mailHostName, 25);
    }
    
    public void postMail(String from, String to, String subject, String message) {
        postMail(from, to, "", "", subject, message, from, to);
    }

    public void postMail(String from, String to, String cc, String bcc, String subject,
            String message, String displayedFrom, String displayedTo) {
        DateFormat dFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);

        try { // Open port to server
            smtpSocket = new Socket(mailHostName, mailPort);
            os = new DataOutputStream(smtpSocket.getOutputStream());
            is = new DataInputStream(smtpSocket.getInputStream());
        } catch (Exception e) {
            System.out.println("Host " + mailHostName + "unknown");
            e.printStackTrace();
            return;
        }

        if (smtpSocket != null && os != null && is != null) { // Connection was made.  Socket is ready for use.
            try {
                os.writeBytes("HELO\r\n");
                // You will add the email address that the server you are using knows you as.
                if (!from.equals("")) os.writeBytes("MAIL From: <" + from + ">\r\n");
                if (!to.equals("")) os.writeBytes("RCPT To: <" + to + ">\r\n");
                if (!cc.equals("")) os.writeBytes("RCPT Cc: <" + cc + ">\r\n");

                // Now we are ready to add the message and the 
                // header of the email to be sent out.                
                os.writeBytes("DATA\r\n");
                os.writeBytes("X-Mailer: " + MAILER + "\r\n");
                os.writeBytes("DATE: " + dFormat.format(new Date()) + "\r\n");
                if (!from.equals("")) os.writeBytes("From: " + displayedFrom + "\r\n");
                if (!to.equals("")) os.writeBytes("To:  " + displayedTo + "\r\n");
                if (!cc.equals("")) os.writeBytes("Cc: " + cc + "\r\n");
                if (!bcc.equals("")) os.writeBytes("RCPT Bcc: " + bcc + "\r\n");

                os.writeBytes("Subject: " + subject + "\r\n\r\n");
                os.writeBytes(message + "\r\n");
                os.writeBytes("\r\n.\r\n");
                os.writeBytes("QUIT\r\n");

                // added by Mahdi to make sure it goes
                os.flush();

                // Now send the email off and check the server reply.  
                // Was an OK is reached you are complete.
                String responseline;
                System.out.println("The response is:");
                while ((responseline = is.readLine()) != null) {  
                    System.out.println(responseline);
                    if (responseline.indexOf("Ok") != -1) {
                        break;
                    }
                }
                os.close();
                is.close();
            } catch (Exception e) {
                System.out.println("Cannot send email as an error occurred.");
                e.printStackTrace();
            }
        }
    }
}