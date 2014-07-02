/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
Created on : 26 November 2012, 16:59
Copyright (C) 2013  Academic Medical Center of the University of Amsterdam
Author: a.benabdelkader@amc.uva.nl

*/
package nl.amc.biolab.Tools;

import java.util.Collection;

import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.SubmissionIO;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.UserAuthentication;

abstract class AppTest {
    /**
     * Create the test case
     *
     * @param AppRetrieveData
     */
	static User user = new User();
    static PersistenceManager pm = PersistenceManager.instance();
    // TODO main
 public static void main(String[] args) throws Exception {
	 user = pm.init("Ammar");
	 /*
	 System.out.println("Processes in Progress: " + pm.getProcessingsInProgress().size() );
	Collection<Processing> procs = pm.executeQuery("from Processing");
	for (Processing proc:procs) {
		 System.out.println(proc.getDbId() + ": " + proc.getSubmissions().size());		
	}
	*/
	 //System.out.println("Data History: " + pm.getDataHistory(480L));
	 System.out.println("Processing Report: \n" + pm.getProcessingReport2(18L));
	 //pm.setUserPassword(99L, "Ammar99", "Ammar99", 99L);
	 //pm.setUserPassword(1L, "Ammar99", "Ammar99", 99L);
	 //pm.setUserPassword(99L, "Ammar99", "Ammar99", 1L);
	 //pm.setUserPassword(99L, "Ammar99", "Ammar99", 1L);
	 /*
	 UserAuthentication userAuth = pm.setUserPassword(1L, "Ammar99", "Ammar99", 1L);
	 if (userAuth!=null)
		 System.out.println("User pass: " + userAuth.getUserLogin() + "/" + pm.decryptString(userAuth.getAuthentication()));
 	 */
	 pm.shutdown();
 }
}
