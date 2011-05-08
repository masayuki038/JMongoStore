package net.wrap_trap.tomcat.session;

import java.io.Serializable;
import java.util.List;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class Foo implements Serializable{

	private String id;
	private int count;
	private Bar bar;
	private List<Bar> barList;
	
	public Foo(){}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Bar getBar() {
		return bar;
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public List<Bar> getBarList() {
		return barList;
	}

	public void setBarList(List<Bar> barList) {
		this.barList = barList;
	}
}
