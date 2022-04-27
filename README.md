# Incorporating Student Preferences into Course Scheduling
#### Video Demo:  <https://youtu.be/vcikm1nZK0U>
#### Description:
In this work, I study course scheduling when student preferences are explicitly taken into account while constructing course schedules. I note that this is the practice in the Croton-Harmon High School in Westchester County, NY.
I will compare course schedules where student preferences for electives are taken into account and those where they are not. I make a number of simplifying assumptions regarding room availabilities and requirements and faculty constraints. I believe our simplified model still captures many constraints used in universities and four-year U.S. high schools. I show, using our simplified model, that if student preferences are taken into account, then a course schedule can be constructed that gives students many more of their preferred courses than if the course schedules are constructed without taking preferences into account.
	
I define three optimization models. The first model assigns classes to periods, and teachers to classes in a way that scheduling conflicts are
taken into account while also maximizing teacher preferences for times at which they teach. The second assigns students to already scheduled classes in such a way
that student preferences for classes are maximized. If one runs the second model using the solution of the first, then one obtains
a complete scheduling solution, while maximizing teacher preferences. However, in many universities it is common for students to come forward with a list of electives
, sorted by preference order, and then classes are given in a first-come first-serve manner. I also test an algorithm to implement such a first-come first-serve assignment.
Finally, I write a combined model which creates a complete schedule, along with teacher-course and student-course assignments. The objective function can be a weighted combination
of student and teacher preferences. 

I compare the student preference scores in the three scenarios:

  1. Teacher preferences are maximized while scheduling classes, and student preferences are completely ignored. After classes have been scheduled, students are assigned to courses on a first-come first-serve basis. (model 1  + algorithm)
  2. Teacher preferences are maximized while scheduling classes, and student preferences are completely ignored. After classes have been scheduled, an optimal allocation of students to courses is computed. (model 1, then model 2)
  3. A combined model is solved to schedule classes, assign teachers and students to courses, while maximizing student preferences and ignoring teacher preferences.

  
  There are four classes: Course, Student, Teacher, and Room, each holding information that will be used to create the integer programming model. 
  
  There is a DataGeneration class to generate and output readable information about courses, students, teachers, and rooms. The `init` function must be called before any information is generated.
  
  The CommandPrompt class is used to execute windows command prompt functions. The `execute` function does not return until the given command is completed.
  
  The Schedule class contains the majority of code. It has methods for reading data, a method for each constraint in the integer programming models, functions to read back and output the data, and some helper functions. All parameters are hard coded in the Schedule class. Changing them will change the generated data. IBM-CPLEX has to be installed and available to solve the integer programming models.
  
  The directory structure is as follows:
  The src and data folders are expected to be in the same directory. The data folder should contain 3 subfolders called input, output, and call.