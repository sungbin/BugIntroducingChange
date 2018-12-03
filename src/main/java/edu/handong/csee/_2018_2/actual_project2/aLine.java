package edu.handong.csee._2018_2.actual_project2;

import org.eclipse.jgit.lib.PersonIdent;

public class aLine {
	public int count;
	public PersonIdent person;
	public String content;
	public String rev;
	public boolean bug;

	aLine(int count, PersonIdent p, String line, String rev) {
		this.count = count;
		this.person = p;
		this.content = line;
		this.rev = rev;
		bug = false;
	}

	@Override
	public String toString() {
		return count +" "+rev+ ":"+bug+" " + person + " " + content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		result = prime * result + ((rev == null) ? 0 : rev.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		aLine other = (aLine) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		if (rev == null) {
			if (other.rev != null)
				return false;
		} else if (!rev.equals(other.rev))
			return false;
		return true;
	}
	
	public void buggy() {
		bug = true;
	}

}
