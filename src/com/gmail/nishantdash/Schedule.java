package com.gmail.nishantdash;

import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Schedule {
	private static ArrayList<Integer>[][] coursesPeriods;
	private static ArrayList<ArrayList<Integer>> courseToStudents = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<Student> studentsList = new ArrayList<Student>();
	private static ArrayList<Course> courseList = new ArrayList<Course>();
	private static ArrayList<Room> roomList = new ArrayList<Room>();
	private static ArrayList<Teacher> teacherList = new ArrayList<Teacher>();
	private static int numDays = 5, numPeriods = 7, maxCourses = 7, maxTeacherCourses = 2,
			firstComeFirstServeObjective = 0;
	// private static int[] coursePoints = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
	private static int[] coursePoints = { 4, 3, 2, 1, 1, 1, 1, 1, 1, 1 };
	private static PrintWriter printer;
	private static ArrayList<Integer> fcfsChoiceDist = new ArrayList<Integer>();

	public static void main(String[] args) {

		// Create variables for DataGeneration
		String[] smandatory = { "Eng", "Math", "Hist", "Sci" };
		String[] selective = { "Art", "Music", "Span", "Gym" };
		int minStudents = 100, maxStudents = 150;
		int minCapacity = 5, maxCapacity = 20;
//		String[] smandatory = { "Eng", "Math", "Hist" };
//		String[] selective = { "Art", "Music", "Span", "Gym", "Sci", "Econ"};
//		int minStudents = 400, maxStudents = 600;
//		int minCapacity = 15, maxCapacity = 30;
		int randomSeed = 4;

		ArrayList<String> mandatory = new ArrayList<String>();
		ArrayList<String> elective = new ArrayList<String>();
		
		for (String s : smandatory)
			mandatory.add(s);
		for (String s : selective)
			elective.add(s);

		// Initialize DataGeneration
		DataGeneration.init(mandatory, elective, minStudents, maxStudents, minCapacity, maxCapacity, randomSeed);

		printer = null;
		try {
			// Generate data for Courses, Teachers, Rooms, and Students
			DataGeneration.generateCourses("data\\input\\courseInput2.txt");
			DataGeneration.generateTeachers("data\\input\\teacherInput.txt");
			DataGeneration.generateRooms("data\\input\\roomInput.txt");
			DataGeneration.generateStudents("data\\input\\studentInput.txt");

			// Read generated data
			studentInput("data\\input\\studentInput.txt");
			courseInput("data\\input\\courseInput2.txt");
			roomInput("data\\input\\roomInput.txt");
			teacherInput("data\\input\\teacherInput.txt");

			// Create PrintWriter for first course solve
			printer = new PrintWriter("data\\output\\courseOutput.lp");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Create lp file and solve
		courseSolve();
		printer.close();

		CommandPrompt.execute("cplex < call\\callcplexCourse.txt > output\\courseSolutionInput.txt && exit");

		try {
			// Read data from Course only solve
			courseSolutionInput("data\\output\\courseSolutionInput.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Run first come first serve scenario and print objective function
		firstComeFirstServe();

		// Create PrintWriter for student solve
		try {
			printer = new PrintWriter("data\\output\\studentOutput.lp");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create lp file and solve
		studentSolve();
		printer.close();

		CommandPrompt.execute("cplex < call\\callcplexStudent.txt > output\\studentSolutionOutput.txt && exit");

		// Create PrintWriter for combined solve
		try {
			printer = new PrintWriter("data\\output\\combinedSolveOutput.lp");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create lp file and solve
		combinedSolve(0);
		printer.close();

		CommandPrompt.execute("cplex < call\\callcplexCombined.txt > output\\combinedSolutionOutput.txt && exit");

		// Create PrintWriter for combined solve with teacher
//		try {
//			printer = new PrintWriter("data\\output\\combinedSolveOutputWithTeacher.lp");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		// Create lp file and solve
//		combinedSolve(1);
//		printer.close();
//
//		CommandPrompt.execute("cplex < call\\callcplexCombinedWithTeacher.txt > output\\combinedSolutionOutputWithTeacher.txt && exit");

		// Print choice distributions
		printStudentChoiceDistribution();

		System.out.println("Done");
	}

	private static void firstComeFirstServe() {

		// Initialize courseToStudents list
		for (int i = 0; i < courseList.size(); i++) {
			courseToStudents.add(new ArrayList<Integer>());
		}

		// Initialize firstComeFirstServeStudentDistribution
		for (int i = 0; i < coursePoints.length; i++) {
			fcfsChoiceDist.add(0);
		}

		for (Student s : studentsList) {
			int[] courses = s.getCourseIDs();
			ArrayList<Integer> allocatedCourseIDs = new ArrayList<Integer>();
			int grade = s.getGrade();

			// Assign students to all mandatory courses
			for (Course c : courseList) {
				if (c.getMandatory() == 1 && c.getGrade() == grade) {
					courseToStudents.get(c.getId()).add(s.getId());
					allocatedCourseIDs.add(c.getId());
				}
			}

			// Assign students to elective courses if it does not conflict with any other
			// courses they are assigned to
			for (int i = 0; i < courses.length; i++) {
				if (courseToStudents.get(courses[i]).size() < roomList.get(courseList.get(courses[i]).getRoomID())
						.getCapacity() && allocatedCourseIDs.size() < maxCourses) {
					boolean conflict = false;
					for (int assignedID : allocatedCourseIDs) {
						if (courseConflictChecker(courses[i], assignedID)) {
							conflict = true;
							break;
						}
					}

					if (!conflict) {
						courseToStudents.get(courses[i]).add(s.getId());
						allocatedCourseIDs.add(courses[i]);
						firstComeFirstServeObjective += coursePoints[i];
						fcfsChoiceDist.set(i, fcfsChoiceDist.get(i) + 1);
					}
				}
			}

		}
	}

	private static boolean courseConflictChecker(int id1, int id2) {
		// Returns true if two courses conflict
		for (int i = 0; i < coursesPeriods.length; i++) {
			for (int j = 0; j < coursesPeriods[i].length; j++) {
				if (coursesPeriods[i][j] != null && coursesPeriods[i][j].contains(id1)
						&& coursesPeriods[i][j].contains(id2))
					return true;
			}
		}
		return false;
	}

	private static void studentInput(String fileName) throws FileNotFoundException {
		// Reads student data and creates Student objects
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		for (int studentIndex = 0, numStudents = sc.nextInt(); studentIndex < numStudents; studentIndex++) {
			int id = sc.nextInt();
			int grade = sc.nextInt();
			int numCourses = sc.nextInt();
			int[] courses = new int[numCourses];
			for (int courseIndex = 0; courseIndex < numCourses; courseIndex++) {
				courses[courseIndex] = sc.nextInt();
			}

			studentsList.add(new Student(id, grade, courses));
		}
		sc.close();
	}

	private static void courseInput(String fileName) throws FileNotFoundException {
		// Reads course data and creates Course objects
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		for (int courseIndex = 0, numCourses = sc.nextInt(); courseIndex < numCourses; courseIndex++) {
			int id = sc.nextInt();
			String name = sc.next();
			int grade = sc.nextInt();
			int roomID = sc.nextInt();
			int mandatory = sc.nextInt();
			int credits = sc.nextInt();
			courseList.add(new Course(id, name, grade, roomID, mandatory, credits));
		}
		sc.close();

	}

	private static void roomInput(String fileName) throws FileNotFoundException {
		// Reads room data and creates room objects
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		for (int roomIndex = 0, numRooms = sc.nextInt(); roomIndex < numRooms; roomIndex++) {
			int id = sc.nextInt();
			int capacity = sc.nextInt();
			roomList.add(new Room(id, capacity));
		}
		sc.close();

	}

	private static void teacherInput(String fileName) throws FileNotFoundException {
		// Reads teacher data and creates teacher objects
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine() && sc.hasNextInt()) {
			int id = sc.nextInt();
			int numCourses = sc.nextInt();
			int[] courses = new int[numCourses];
			for (int i = 0; i < courses.length; i++) {
				courses[i] = sc.nextInt();
			}
			int[] scores = new int[numPeriods];
			for (int i = 0; i < scores.length; i++) {
				scores[i] = sc.nextInt();
			}

			teacherList.add(new Teacher(id, courses, scores));
		}

		sc.close();
	}

	@SuppressWarnings("unchecked")
	private static void courseSolutionInput(String fileName) throws FileNotFoundException {
		// Reads course only solution and assigns course IDs to
		// coursesPeriods[day][period]
		File file = new File(fileName);
		Scanner sc = new Scanner(file);

		coursesPeriods = new ArrayList[numDays][numPeriods];

		while (sc.hasNextLine()) {
			String variable = sc.next();

			if (variable.charAt(0) == 'x') {
				int commaLocFirst = variable.indexOf(",");
				int commaLocSec = variable.indexOf(",", commaLocFirst + 1);
				int id = Integer.parseInt(variable.substring(1, commaLocFirst));
				int day = Integer.parseInt(variable.substring(commaLocFirst + 1, commaLocSec));
				int period = Integer.parseInt(variable.substring(commaLocSec + 1, commaLocSec + 2));

				if (coursesPeriods[day][period] == null) {
					coursesPeriods[day][period] = new ArrayList<Integer>();
				}

				coursesPeriods[day][period].add(id);
			}
			sc.nextLine();
		}

		sc.close();

	}

	private static void courseSolve() {
		printer.println("Maximize");

		teacherMaximize(1);

		printer.println("Subject to");

		// A teacher can only teach a max number of courses that they are qualified to
		// teach
		teacherCourseAssignment();

		// Only one teacher can teach a course
		CourseTeacherAssignment();

		// A teacher can teach multiple courses only if they don't conflict
		teacherConflictCourse();

		// Links v variable with u and x
		teacherCourseLink();

		// There can only be one Course I on J day
		oneCoursePerDay();

		// Mandatory classes for the same grade cannot clash
		courseMandatoryConflicts();

		// Number of instances should equal the number of credits
		courseMaxPeriods();

		printer.println("Binary");
		courseBinary();
		teacherBinary();

		printer.println("End");
	}

	private static void studentSolve() {
		printer.println("Maximize");

		studentMaximize();

		printer.println("Subject to");

		// Student can only take max number of courses
		studentMaxCourses();

		// Students cannot be in conflicting classes
		studentCourseConflicts();

		// Students must have all mandatory classes
		studentMandatoryClasses();

		// Room capacity
		studentRoomCapacity();

		printer.println("Binary");
		studentBinary();

		printer.println("End");
	}

	private static void combinedSolve(double teacherWeight) {
		printer.println("Maximize");

		studentMaximize();

		if (teacherWeight > 0)
			teacherMaximize(teacherWeight);

		printer.println("Subject to");

		// A teacher can only teach a max number of courses that they are qualified to
		// teach
		teacherCourseAssignment();

		// Only one teacher can teach a course
		CourseTeacherAssignment();

		// A teacher can teach multiple courses only if they don't conflict
		teacherConflictCourse();

		// Links v variable with u and x
		teacherCourseLink();

		// There can only be one Course I on J day
		oneCoursePerDay();

		// Mandatory classes for the same grade cannot clash
		courseMandatoryConflicts();

		// Number of instances should equal the number of credits
		courseMaxPeriods();

		// Student can only take max number of courses
		studentMaxCourses();

		// Students cannot be in conflicting classes
		combinedStudentCourseConflict();

		// Students must have all mandatory classes
		studentMandatoryClasses();

		// Room capacity
		studentRoomCapacity();

		printer.println("Binary");
		courseBinary();
		studentBinary();
		teacherBinary();

		printer.println("End");
	}

	private static void teacherMaximize(double weight) {
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			for (int j = 0; j < courses.length; j++) {
				for (int k = 0; k < numDays; k++) {
					for (int l = 0; l < numPeriods; l++) {
						printer.printf("+ %.2f v%d,%d,%d,%d ", teacher.getScores()[l] * weight, teacher.getId(),
								courses[j], k, l);
					}
					printer.println();
				}
			}
		}
	}

	private static void teacherCourseAssignment() {
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			printer.printf("tc%d: ", teacher.getId());
			for (int j = 0; j < courses.length; j++) {
				printer.printf("+ u%d,%d ", teacher.getId(), courses[j]);
			}
			printer.printf("<= %d\n", maxTeacherCourses);
		}
	}

	private static void CourseTeacherAssignment() {
		ArrayList<ArrayList<Integer>> ct = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < courseList.size(); i++) {
			ct.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			for (int course : courses) {
				ct.get(course).add(i);
			}
		}

		for (int i = 0; i < ct.size(); i++) {
			printer.printf("Onet%d: ", i);
			for (int j = 0; j < ct.get(i).size(); j++) {
				printer.printf("+ u%d,%d ", ct.get(i).get(j), i);
			}
			printer.printf(" = 1\n");
		}
	}

	private static void teacherCourseLink() {
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			for (int j = 0; j < courses.length; j++) {
				for (int k = 0; k < numDays; k++) {
					for (int l = 0; l < numPeriods; l++) {
						printer.printf("v%d,%d,%d,%d - u%d,%d <= 0\n", teacher.getId(), courses[j], k, l,
								teacher.getId(), courses[j]);
						printer.printf("v%d,%d,%d,%d - x%d,%d,%d <= 0\n", teacher.getId(), courses[j], k, l, courses[j],
								k, l);
//						printer.printf("v%d,%d,%d,%d - x%d,%d,%d - u%d,%d >= -1\n", teacher.getId(), courses[j], k, l, courses[j],
//								k, l, teacher.getId(), courses[j]);
					}
				}
			}
		}
	}

	private static void teacherConflictCourse() {
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			for (int j1 = 0; j1 < courses.length; j1++) {
				for (int j2 = j1 + 1; j2 < courses.length; j2++) {
					for (int k = 0; k < numDays; k++) {
						for (int l = 0; l < numPeriods; l++) {
							printer.printf("u%d,%d + u%d,%d + x%d,%d,%d + x%d,%d,%d <= 3\n", teacher.getId(),
									courses[j1], teacher.getId(), courses[j2], courses[j1], k, l, courses[j2], k, l);
						}
					}
				}
			}
		}
	}

	private static void teacherBinary() {
		for (int i = 0; i < teacherList.size(); i++) {
			Teacher teacher = teacherList.get(i);
			int[] courses = teacher.getCourseIDs();
			for (int j = 0; j < courses.length; j++) {
				printer.printf(" u%d,%d", teacher.getId(), courses[j]);
				for (int k = 0; k < numDays; k++) {
					for (int l = 0; l < numPeriods; l++) {
						printer.printf(" v%d,%d,%d,%d", teacher.getId(), courses[j], k, l);
					}
					printer.println();
				}
			}
		}
	}

	private static void oneCoursePerDay() {
		for (int i = 0; i < courseList.size(); i++) {
			for (int j = 0; j < numDays; j++) {
				printer.printf("%s,Day_%d: ", courseList.get(i).getName(), j);
				for (int k = 0; k < numPeriods; k++) {
					printer.printf("+ x%d,%d,%d ", i, j, k);
				}
				printer.println("<= 1");
			}
		}
	}

	private static void courseMandatoryConflicts() {
		for (int i = 0; i < courseList.size() - 1; i++) {
			for (int iPrime = i + 1; iPrime < courseList.size(); iPrime++) {
				if (courseList.get(i).getGrade() == courseList.get(iPrime).getGrade()) {
					if (courseList.get(i).getMandatory() == 1 || courseList.get(iPrime).getMandatory() == 1) {
						for (int j = 0; j < numDays; j++) {
							for (int k = 0; k < numPeriods; k++) {
								printer.printf("%s,%d,%dvs%s,%d,%d: x%d,%d,%d + x%d,%d,%d <= 1\n",
										courseList.get(i).getName(), j, k, courseList.get(iPrime).getName(), j, k, i, j,
										k, iPrime, j, k);
							}
						}
					}
				}
			}
		}
	}

	private static void courseMaxPeriods() {
		for (int i = 0; i < courseList.size(); i++) {
			printer.printf("%s: ", courseList.get(i).getName());
			for (int j = 0; j < numDays; j++) {
				for (int k = 0; k < numPeriods; k++) {
					printer.printf("+ x%d,%d,%d ", i, j, k);
				}
				printer.println();
			}
			printer.println("= " + courseList.get(i).getCredits());
		}
	}

	private static void courseBinary() {
		for (int i = 0; i < courseList.size(); i++) {
			for (int j = 0; j < numDays; j++) {
				for (int k = 0; k < numPeriods; k++) {
					printer.printf(" x%d,%d,%d ", i, j, k);
				}
				printer.println();
			}
		}
	}

	private static void studentMaximize() {
		for (int i = 0; i < studentsList.size(); i++) {
			int[] electives = studentsList.get(i).getCourseIDs();
			for (int j = 0; j < electives.length; j++) {
				printer.printf("+ %d y%d,%d ", coursePoints[j], i, electives[j]);
			}
			printer.println();
		}
	}

	private static void studentMaxCourses() {
		for (int i = 0; i < studentsList.size(); i++) {
			for (int j = 0; j < courseList.size(); j++) {
				if (studentsList.get(i).getGrade() >= courseList.get(j).getGrade()) {
					printer.printf("+ y%d,%d ", i, j);
				}
			}
			printer.printf("<= %d\n", maxCourses);
		}
	}

	private static void studentCourseConflicts() {
		for (int i = 0; i < studentsList.size(); i++) {
			int[] electiveIDs = studentsList.get(i).getCourseIDs();
			for (int j = 0; j < numDays; j++) {
				for (int k = 0; k < numPeriods; k++) {
					if (coursesPeriods[j][k] != null) {
						int countConflicts = 0;
						for (int l : coursesPeriods[j][k]) {
							boolean mandatory = studentsList.get(i).getGrade() == courseList.get(l).getGrade()
									&& courseList.get(l).getMandatory() == 1;
							boolean electives = arrayContains(electiveIDs, l);
							if (mandatory || electives) {
								countConflicts++;
							}
						}
						if (countConflicts > 1) {
							for (int l : coursesPeriods[j][k]) {
								boolean mandatory = studentsList.get(i).getGrade() == courseList.get(l).getGrade()
										&& courseList.get(l).getMandatory() == 1;
								boolean electives = arrayContains(electiveIDs, l);
								if (mandatory || electives) {
									printer.printf("+ y%d,%d ", i, l);
								}
							}
							printer.printf("<= 1\n");
						}
					}
				}
			}
		}
	}

	private static void studentMandatoryClasses() {
		for (int i = 0; i < studentsList.size(); i++) {
			int numMandatory = 0;
			printer.printf("Std,%d_mand: ", i);
			for (int j = 0; j < courseList.size(); j++) {
				if (courseList.get(j).getGrade() == studentsList.get(i).getGrade()
						&& courseList.get(j).getMandatory() == 1) {
					numMandatory++;
					printer.printf("+ y%d,%d ", i, j);
				}
			}
			printer.printf("= %d\n", numMandatory);
		}
	}

	private static void studentRoomCapacity() {
		for (int j = 0; j < courseList.size(); j++) {
			printer.printf("Cap%d: ", j);
			int capacity = roomList.get(courseList.get(j).getRoomID()).getCapacity();
			for (int i = 0; i < studentsList.size(); i++) {
				if (studentsList.get(i).getGrade() >= courseList.get(j).getGrade()) {
					printer.printf("+ y%d,%d ", i, j);
					if (i > 0 && i % 10 == 0)
						printer.println();
				}
			}
			printer.printf("<= %d\n", capacity);

		}
	}

	private static void studentBinary() {
		for (int i = 0; i < studentsList.size(); i++) {
			for (int j = 0; j < courseList.size(); j++) {
				if (studentsList.get(i).getGrade() >= courseList.get(j).getGrade()) {
					printer.printf("y%d,%d ", i, j);
				}
			}
			printer.println();
		}
	}

	private static void combinedStudentCourseConflict() {
		for (int i = 0; i < studentsList.size(); i++) {
			ArrayList<Integer> allCourseIDs = new ArrayList<Integer>();
			for (int c : studentsList.get(i).getCourseIDs()) {
				allCourseIDs.add(c);
			}
			for (Integer c : getMandatory(studentsList.get(i).getGrade())) {
				allCourseIDs.add(c);
			}
			for (int c1 = 0; c1 < studentsList.get(i).getCourseIDs().length; c1++) {
				for (int c2 = c1 + 1; c2 < allCourseIDs.size(); c2++) {
					int cID1 = studentsList.get(i).getCourseIDs()[c1];
					int cID2 = allCourseIDs.get(c2);
					for (int k = 0; k < numDays; k++) {
						for (int l = 0; l < numPeriods; l++) {
							printer.printf("SC%d,%d,%d: y%d,%d + y%d,%d + x%d,%d,%d + x%d,%d,%d <= 3\n", i, cID1, cID2,
									i, cID1, i, cID2, cID1, k, l, cID2, k, l);
						}
					}
				}
			}
		}
	}

	private static ArrayList<Integer> getMandatory(int grade) {
		// Returns a list of mandatory courses for a particular grade
		ArrayList<Integer> mandatory = new ArrayList<Integer>();
		for (Course course : courseList) {
			if (course.getGrade() == grade && course.getMandatory() == 1) {
				mandatory.add(course.getId());
			}
		}
		return mandatory;
	}

	private static boolean arrayContains(int[] a, int key) {
		// returns true if key is in array a
		for (int num : a) {
			if (key == num)
				return true;
		}
		return false;
	}

	private static int arrayIndexOf(int[] a, int key) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == key)
				return i;
		}
		return -1;
	}

	private static void printStudentChoiceDistribution() {
		System.out.println("\n");

		System.out.println("First Come First Serve Distribution:");

		printChoices(fcfsChoiceDist);

		System.out.println("\nFirst Come First Serve Score: " + firstComeFirstServeObjective);

		System.out.println("\nStudent Solve Distribution: ");
		ArrayList<Integer> ssd = SolveDistribution("data\\output\\studentSolutionOutput.txt");
		printChoices(ssd);

		System.out.println("\nStudent Solve Score: " + getScore(ssd));

		System.out.println("\nCombined Solve Distribution");
		ArrayList<Integer> csd = SolveDistribution("data\\output\\combinedSolutionOutput.txt");
		printChoices(csd);

		System.out.println("\nCombined Score: " + getScore(csd));

//		System.out.println("\nCombined Solve Distribution With Teacher");
//		ArrayList<Integer> csdwt = SolveDistribution("data\\output\\combinedSolutionOutputWithTeacher.txt");
//		printChoices(csdwt);
//
//		System.out.println("\nCombined Score With Teacher: " + getScore(csdwt));

		System.out.println("\n");
	}

	private static void printChoices(ArrayList<Integer> a) {
		for (int i = 0; i < a.size(); i++) {
			System.out.println("Choice " + (i + 1) + ": " + a.get(i));
		}
	}

	private static ArrayList<Integer> SolveDistribution(String filename) {
		ArrayList<Integer> a = new ArrayList<Integer>();

		for (int i = 0; i < coursePoints.length; i++) {
			a.add(0);
		}

		try {
			File file = new File(filename);
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine()) {
				String variable = sc.next();

				if (variable.charAt(0) == 'y') {
					int commaLocFirst = variable.indexOf(",");
					int sid = Integer.parseInt(variable.substring(1, commaLocFirst));
					int cid = Integer.parseInt(variable.substring(commaLocFirst + 1));

					if (arrayContains(studentsList.get(sid).getCourseIDs(), cid)) {
						int index = arrayIndexOf(studentsList.get(sid).getCourseIDs(), cid);
						a.set(index, a.get(index) + 1);
					}
				}
				sc.nextLine();
			}

			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return a;
	}

	private static int getScore(ArrayList<Integer> a) {
		int s = 0;

		for (int i = 0; i < coursePoints.length && i < a.size(); i++) {
			s += coursePoints[i] * a.get(i);
		}

		return s;
	}
}
