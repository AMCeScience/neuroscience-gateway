package nl.amc.biolab.nsg.dataobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nl.amc.biolab.datamodel.objects.Project;

@Entity
@Table(name = "NsgDataElement")
public class NsgProject {
	private Project project;
	
	private Long dbId;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "resource")
	private String resource;
	
	public NsgProject(Project project, String resource_name) {
		this.dbId = project.getDbId();
		this.name = project.getName();
		this.description = project.getDescription();
		this.resource = resource_name;
		this.project = project;
	}
	
	public Long getDbId() {
		return dbId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getResource() {
		return resource;
	}
	
	public Project getProject() {
		return project;
	}
}
