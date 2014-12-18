package nl.amc.biolab.nsg.dataobjects;

import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nl.amc.biolab.datamodel.objects.DataElement;

@Entity
@Table(name = "NsgDataElement")
public class NsgDataElement {
	private DataElement de;
	
	private Long dbId;
	
    @Column(name = "date")
	private String date;
    
    @Column(name = "type")
	private String type;
    
    @Column(name = "format")
	private String format;
    
    @Column(name = "subject")
	private String subject;
    
    @Column(name = "resource")
	private String resource_name;
	
	public NsgDataElement(DataElement de) {
		this.de = de;
		this.dbId = de.getDbId();
		this.date = new SimpleDateFormat("dd/MM/yyyy").format(de.getDate());
		this.type = de.getType();
		this.format = de.getFormat();
		this.subject = de.getValueByName("xnat_subject_label");
		this.resource_name = de.getResource().getName();
	}
	
	public DataElement getDataElement() {
		return de;
	}
	
	public Long getDbId() {
		return dbId;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getType() {
		return type;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getResource() {
		return resource_name;
	}
}
