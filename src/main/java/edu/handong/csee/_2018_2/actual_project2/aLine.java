package edu.handong.csee._2018_2.actual_project2;

import org.eclipse.jgit.lib.PersonIdent;

public class aLine {
	public int count;
	public PersonIdent person;
	public String content;
	
	aLine(int count, PersonIdent p, String line) {
		this.count = count;
		this.person = p;
		this.content = line;
	}

	@Override
	public String toString() {
		return count + ": " + person + " " + content;
	}
	
}
