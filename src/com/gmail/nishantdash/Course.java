package com.gmail.nishantdash;

public class Course {

	private int id, grade, roomID, mandatory, credits;
	private String name;

	public Course(int id, String name, int grade, int roomID, int mandatory, int credits) {
		this.id = id;
		this.name = name;
		this.grade = grade;
		this.roomID = roomID;
		this.mandatory = mandatory;
		this.credits = credits;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getMandatory() {
		return mandatory;
	}

	public void setMandatory(int mandatory) {
		this.mandatory = mandatory;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

	@Override
	public String toString() {
		return "Course [id=" + id + ", grade=" + grade + ", roomID=" + roomID + ", mandatory=" + mandatory
				+ ", credits=" + credits + ", name=" + name + "]";
	}

}
