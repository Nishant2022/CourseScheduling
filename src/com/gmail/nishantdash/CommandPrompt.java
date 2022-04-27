package com.gmail.nishantdash;

public class CommandPrompt {

	public static boolean execute(String command) {
		try {
			Process p = Runtime.getRuntime().exec("cmd /c start /wait cmd.exe /K \"cd data && " + command + "\"");
			
			System.out.println("Running command: " + command);
			
			p.waitFor();
			
			System.out.println("Process complete");
			
			return true;
		} catch (Exception e) {
			System.out.println("Something went wrong");
			e.printStackTrace();
			return false;
		}

	}

}
