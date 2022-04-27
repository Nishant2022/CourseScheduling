package com.gmail.nishantdash;

import java.util.ArrayList;
import java.util.Random;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

// TODO Generate Multiple student orders for same teacher data
// TODO Generate multiple data with different random seeds
// TODO Create larger data size
// TODO Create a document explaining everything
// TODO Submit to CS50
// TODO Have course capacity linked to student preferences

public class DataGeneration {

	private static ArrayList<String> mandatoryClasses, electiveClasses;
	private static int minStudents, maxStudents, minCapacity, maxCapacity;
	private static ArrayList<Integer> studentDistribution;
	private static Random rand;

	public static void init(ArrayList<String> mandatoryClasses, ArrayList<String> electiveClasses, int minStudents,
			int maxStudents, int minCapacity, int maxCapacity, int randomSeed) {

		DataGeneration.mandatoryClasses = mandatoryClasses;
		DataGeneration.electiveClasses = electiveClasses;
		DataGeneration.minStudents = minStudents;
		DataGeneration.maxStudents = maxStudents;
		DataGeneration.minCapacity = minCapacity;
		DataGeneration.maxCapacity = maxCapacity;
		DataGeneration.rand = new Random(randomSeed);

		studentDistribution = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++)
			studentDistribution.add(0);

	}

	public static void generateCourses(String filename) {
		try {
			PrintWriter printer = new PrintWriter(filename);

			int numberOfClasses = 4 * (mandatoryClasses.size() + electiveClasses.size());
			printer.println(numberOfClasses);

			for (int i = 0; i < numberOfClasses;) {

				// Changes Grade after all classes for previous grade have been printed
				int grade = i / (mandatoryClasses.size() + electiveClasses.size()) + 9;

				// Loop through all Mandatory classes and print id, name + grade, grade,
				// placeholder room value, and random credits
				for (String className : mandatoryClasses) {
					printer.printf("%d %s%d %d %d 1 %d\n", i, className, grade, grade, i, rng(3, 4));
					i++;
				}

				// Loop through all Mandatory classes and print id, name + grade, grade,
				// placeholder room value, and random credits
				for (String className : electiveClasses) {
					printer.printf("%d %s%d %d %d 0 %d\n", i, className, grade, grade, i, rng(2, 3));
					i++;
				}

			}

			printer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	public static void generateRooms(String filename) {
		try {
			PrintWriter printer = new PrintWriter(filename);

			int numberOfClasses = 4 * (mandatoryClasses.size() + electiveClasses.size());
			printer.println(numberOfClasses);

			// Create a room for each class
			for (int i = 0; i < numberOfClasses; i++) {

				// If it is mandatory, capacity is 1000
				for (String className : mandatoryClasses) {
					printer.printf("%d %d\n", i, 1000);
					i++;
				}

				// If it is for elective, capacity is between 5 and 20 inclusive
				for (String className : electiveClasses) {
					printer.printf("%d %d\n", i, rng(minCapacity, maxCapacity));
					i++;
				}

			}

			printer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void generateStudents(String filename) {
		try {
			PrintWriter printer = new PrintWriter(filename);

			// totalStudents is a random number between pre-defined min and max
			int totalStudents = rng(minStudents, maxStudents);
			printer.println(totalStudents);

			for (int i = 0; i < totalStudents; i++) {
				int grade = rng(9, 12);
				int numClasses = rng(1, 4);

				printer.printf("%d %d %d ", i, grade, numClasses);

				// Update number of students per grade
				studentDistribution.set(grade - 9, studentDistribution.get(grade - 9) + 1);

				ArrayList<Integer> classChoices = new ArrayList<Integer>();

				// startIndex is the first position of the given grade's electives
				int startIndex = (grade - 9) * (mandatoryClasses.size() + electiveClasses.size())
						+ mandatoryClasses.size();

				// Insert all choices into classChoices list
				for (int j = startIndex; j < startIndex + electiveClasses.size(); j++) {
					classChoices.add(j);
				}

				// Print a random elective
				for (int j = 0; j < numClasses; j++) {
					int randomChoice = choice(classChoices);
					if (randomChoice == -1)
						break;

					printer.printf("%d ", randomChoice);
				}
				printer.println();

			}

			printer.close();

			for (int i = 0; i < studentDistribution.size(); i++) {
				System.out.printf("Grade: %d, Number: %d\n", i + 9, studentDistribution.get(i));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void generateTeachers(String filename) {
		try {
			PrintWriter printer = new PrintWriter(filename);

			int id = 0;
			int numCourses = mandatoryClasses.size() + electiveClasses.size();
			for (int i = 0; i < numCourses; i++) {
				for (int j = rng(2, 3); j > 0; j--) {
					if (electiveClasses.size() <= 4) {
						printer.printf("%d 4 %d %d %d %d", id, i, i + numCourses, i + numCourses * 2,
								i + numCourses * 3);
					}
					else {
						printer.printf("%d 12", id);
						for (int k = 0; k < 4; k++) {
							printer.printf(" %d %d %d", i % mandatoryClasses.size() + numCourses * k, i % mandatoryClasses.size() + 3 + numCourses * k, i % mandatoryClasses.size() + 6 + numCourses * k);
						}
					}
					preference(rng(0, 2), printer);
					id++;
				}
			}

			printer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static int rng(int min, int max) {
		// returns random int between min and max inclusive
		return rand.nextInt(max + 1 - min) + min;
	}

	private static int choice(ArrayList<Integer> list) {
		if (list.isEmpty()) {
			return -1;
		}

		// Get random index and return removed element from list
		int index = rng(0, list.size() - 1);
		return list.remove(index);
	}

	private static void preference(int n, PrintWriter printer) {
		switch (n) {
		case 0:
			//printer.printf(" 10 9 8 7 6 5 4\n");
			printer.printf(" 4 3 2 2 1 1 1\n");
			break;
		case 1:
//			printer.printf(" 4 5 6 7 8 9 10\n");
			printer.printf(" 1 1 1 2 2 3 4\n");
			break;
		default:
//			printer.printf(" 4 6 8 10 9 7 5\n");
			printer.printf(" 1 1 2 4 3 2 1\n");
			
		}

	}

}
