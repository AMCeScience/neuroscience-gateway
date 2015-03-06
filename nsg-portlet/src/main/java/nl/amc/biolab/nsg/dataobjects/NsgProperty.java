package nl.amc.biolab.nsg.dataobjects;

public class NsgProperty {
	private String description;
	private String value;
	
	public NsgProperty(String description, String value) {
		this.description = description;
		this.value = value;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getValue() {
		return this.value;
	}
}
