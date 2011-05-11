package net.wrap_trap.tomcat.session;

import java.io.Serializable;
import java.util.List;

public class Bar implements Serializable{
	
	private String id;
	
	private List<String> stringList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}
}
