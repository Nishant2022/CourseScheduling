package com.gmail.nishantdash;

import java.util.Arrays;

public class Student {
	private int id, grade;
	private int[] courseIDs;
	
	public Student(int id, int grade, int[] courseIDs) {
		this.id = id;
		this.grade = grade;
		this.setCourses(courseIDs);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public int[] getCourseIDs() {
		return courseIDs;
	}

	public void setCourses(int[] courseIDs) {
		this.courseIDs = courseIDs;
	}

	public String toString() {
		return "Student [id=" + id + ", grade=" + grade + ", courseIDs=" + Arrays.toString(courseIDs) + "]";
	}
}
