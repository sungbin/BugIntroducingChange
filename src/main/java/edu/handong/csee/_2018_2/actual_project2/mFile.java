package edu.handong.csee._2018_2.actual_project2;

public class mFile {
	public String name;
	public String[] lines;
	public String rev;
	
	mFile(String name, String lines, String id) {
		this.name = name;
		this.lines = lines.split("\n");
		rev = id;
	}
	mFile(String name, String[] lines, String id) {
		this.name = name;
		this.lines = lines;
		rev = id;
	}
}
