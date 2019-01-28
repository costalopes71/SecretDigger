package com.costalopes.secretdigger.example;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

import com.costalopes.secretdigger.digger.SecretDigger;

public class ExampleOfUsage {
	
	private static String secretToSearch;
	private static Scanner sc = new Scanner(System.in);
	private static SecretDigger digger = new SecretDigger();
	
	public static void main(String[] args) {

		//
		// result list
		//
		List<String> resultList = null;
		
		//
		// getting start time
		//
		Instant start = Instant.now();
		
		try {

			//
			// printing welcome and option menu
			//
			int option = printMenu();
			
			//
			// digging!
			//
			switch (option) {
				case 1:
					
					//
					// asking the user for the directory to search
					//
					resultList = digger.digAndSearchDirectory(secretToSearch, askForDirectory());

					break;
				case 2: 
			
					//
					// starting the search, if no directory option has been made, search everything, getting system partitions
					//
					resultList = digger.digAndSearchEverywhere(secretToSearch);
					
					break;
				default:
					System.out.println("Ilegal option [" + option + "], program will be aborted");
					System.exit(2);
				}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("Press any key to exit...");
			sc.nextLine();
		}
		
		//
		// priting result
		//
		System.out.println("\nRESULT:");
		System.out.println("================================================================================================");
		resultList.forEach(System.out::println);
		System.out.println("================================================================================================");
		
		//
		// END. Printing end and result
		//
		System.out.println("\nFINISHED SEARCHING!");
		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).toSeconds() + " seconds");
	}

	private static File askForDirectory() throws IOException {
		Scanner sc = new Scanner(System.in);
		
		File dir = null;
		do {
			System.out.println("Enter the absolute path of the directory where to start searching");
			System.out.println("->");
			String path = sc.next() + sc.nextLine();
			dir = new File(path);
		} while (!dir.exists());
		
		sc.close();
		
		System.out.println("directory chosen to search [" + dir.getCanonicalPath() + "]");
		
		return dir;
	}

    private static int printMenu() throws IOException {
		System.out.println("================================================================================================");
		System.out.println("=================================== SECRET DIGGER ==============================================");
		System.out.println("================================================================================================");
		
		System.out.print("Type the word or phrase that you want to search ->");
		secretToSearch = sc.next() + sc.nextLine();
		System.out.println("choosen word/phrase [" + secretToSearch + "].");
		
		String option = "";
		do {
			System.out.println("\nNow chose the type of searh: ");
			System.out.println("1 - search into an especific directory");
			System.out.println("2 - search ALL COMPUTER");
			System.out.print("option ->");
			option = sc.next();
			
			if (!option.matches("1|2")) {
				cleanConsoleAndExibErrorMessage();
			}
			
		} while (!option.matches("1|2"));
		
		System.out.println("type of search: [" + Integer.valueOf(option) + "]");
		
		return Integer.valueOf(option);
	}
    
    private static void cleanConsoleAndExibErrorMessage() throws IOException {
    	
    	try {
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			System.out.println("INVALID entry. Try again...");
			System.out.println("");
		} catch (IOException | InterruptedException e) {
			throw new IOException("An error occured while trying to clean the console...");
		}
    	
    }
    
}
