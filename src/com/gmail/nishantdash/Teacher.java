package com.gmail.nishantdash;

public class Teacher {
	private int id, preferredTime;
	private int[] courseIDs, scores;

	public Teacher(int id, int[] courseIDs, int[] scores) {
		this.id = id;
		this.courseIDs = courseIDs;
		this.scores = scores;
	}

	public int[] getScores() {
		return scores;
	}

	public void setScores(int[] scores) {
		this.scores = scores;
	}

	public int[] getCourseIDs() {
		return courseIDs;
	}

	public int getId() {
		return id;
	}

	public int getPreferredTime() {
		return preferredTime;
	}

	public void setCourseIDs(int[] courseIDs) {
		this.courseIDs = courseIDs;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPreferredTime(int preferredTime) {
		this.preferredTime = preferredTime;
	}

}
