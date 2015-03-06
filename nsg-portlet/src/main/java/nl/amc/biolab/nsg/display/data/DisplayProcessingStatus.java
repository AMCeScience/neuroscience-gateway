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
package nl.amc.biolab.nsg.display.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.amc.biolab.datamodel.objects.DataElement;
import nl.amc.biolab.datamodel.objects.Processing;
import nl.amc.biolab.datamodel.objects.SubmissionIO;

/**
 * 
 * @author initial architecture and implementation: m.almourabit@amc.uva.nl<br/>
 *
 */
public class DisplayProcessingStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<DisplayProcessingInput> processingInputs = new ArrayList<DisplayProcessingInput>();
//	private List<DisplayProcessingOutput> processingOutputs = new ArrayList<DisplayProcessingOutput>();
	private Processing processing;
	private Collection<DataElement> inputDataList = new ArrayList<DataElement>();
	private String status = "uploading";
	private List<Map<List<SubmissionIO>, List<SubmissionIO>>> submissionIOs;
	
	public DisplayProcessingStatus() {
	} 
	
	public List<DisplayProcessingInput> getProcessingInputs() {
		return processingInputs;
	}

	public void setProcessingInputs(List<DisplayProcessingInput> processingInputs) {
		this.processingInputs = processingInputs;
	}

	public Collection<DataElement> getInputDataList() {
		return inputDataList;
	}

	public Processing getProcessing() {
		return processing;
	}

	public void setProcessing(Processing processing) {
		this.processing = processing;
	}

	public List<DisplayProcessingInput> getProcessinginputs() {
		return processingInputs;
	}

	public void setProcessinginputs(List<DisplayProcessingInput> processinginputs) {
		this.processingInputs = processinginputs;
	}

	public void setInputDataList(Collection<DataElement> inputDataList) {
		this.inputDataList  = inputDataList;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String string) {
		this.status  = string;
	}

	public List<Map<List<SubmissionIO>, List<SubmissionIO>>> getSubmissionIOs() {
		return submissionIOs;
	}

	/**
	 * 
	 * @return list of related input and output submissionIOs maps
	 */
	public void setSubmissionIOs(List<Map<List<SubmissionIO>, List<SubmissionIO>>> submissionIOs) {
		this.submissionIOs = submissionIOs;
	}
}