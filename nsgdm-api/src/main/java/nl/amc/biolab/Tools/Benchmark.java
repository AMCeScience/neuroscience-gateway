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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.rowset.serial.SerialBlob;
//import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import nl.amc.biolab.Tools.XNATRestClient;
import nl.amc.biolab.nsgdm.Application;
import nl.amc.biolab.nsgdm.DataElement;
import nl.amc.biolab.nsgdm.Resource;
import nl.amc.biolab.nsgdm.Preference;
import nl.amc.biolab.nsgdm.Processing;
import nl.amc.biolab.nsgdm.Project;
import nl.amc.biolab.nsgdm.Property;
import nl.amc.biolab.nsgdm.Submission;
import nl.amc.biolab.nsgdm.SubmissionIO;
import nl.amc.biolab.nsgdm.User;
import nl.amc.biolab.nsgdm.UserActivity;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
/** * * * * *  Unit test for data retrieval  * * * * * * 
 * 
 * This package contains three (03) examples for querying the data based on:
 * 1- Project ID
 * 2- Experiment ID
 * 3- User ID
 */
abstract class Benchmark {
    /**
     * Create the test case
     *
     * @param AppRetrieveData
     */
    static PersistenceManager pm = PersistenceManager.instance();


       
 public static void main(String[] args) throws Exception {

		Date d1 = new Date();
 		Date d2 = new Date();
 		System.out.println("1- init:\t\t\t" + pm.init("Matthan").getFirstName() + ": "  + ((new Date()).getTime() - d1.getTime()) + " ms");


 		d1 = new Date();
 		System.out.println("2- getAllProcessings:\t" + pm.getAllProcessings().size() + " objects: "  + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("3- getApplication:\t\t" + pm.getApplication(1L).getInternalName() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("4- getApplicationByName:\t" + pm.getApplicationByName("Test_Application").getInternalName() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("5- getApplicationPorts:\t" + pm.getApplicationPorts(1L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("6- getApplications:\t" + pm.getApplications().size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("7- getApplications:\t" + pm.getApplications(357L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("8- getDataElement:\t\t" + pm.getDataElement(357L).getName() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("9- getDownloadURI:\t\t" + pm.getDownloadURI(357L) + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("10- getInputURI:\t\t" + pm.getInputURI(357L) + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("11- getProcessing:\t\t" + pm.getProcessing(15L).getName() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("12- getProcessingInputs:\t" + pm.getProcessingInputs(21L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("13- getProcessingIOs:\t" + pm.getProcessingIOs(21L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("14- getProcessingOutputs:\t" + pm.getProcessingOutputs(21L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("15- getProcessings:\t\t" + pm.getProcessings().size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("16- getProcessingSubmissions:\t" + pm.getProcessingSubmissions(21L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("17- getProject:\t\t" + pm.getProject(45L).getName() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("18- getProjectData:\t\t" + pm.getProjectData(60L).size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("20- getSubmissionError:\t" + pm.getSubmissionError(15L).getCode() + ": " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		System.out.println("21- getProcessingsInProgress:\t" + pm.getProcessingsInProgress().size() + " objects: " + ((new Date()).getTime() - d1.getTime()) + " ms");

 		d1 = new Date();
 		pm.shutdown();
 		System.out.println("22- shutdown:\t\t" + ((new Date()).getTime() - d1.getTime()) + " ms");

 		
 		System.out.println("\n\nTotal time:\t\t" + ((new Date()).getTime() - d2.getTime()) + " ms");
	
	 
			 
/*	 if (args.length!=2) {
		 System.out.println("wrong argument!...\nUsage: SynchXNAT UserID Action\n\tUserID: UserID as in the neuroscience catalogue\n\tAction: UpdateCatalogue | UpdateScanDate | ComputeMatchingApplications | All");
		 return;
*/	 }
    	




 }
