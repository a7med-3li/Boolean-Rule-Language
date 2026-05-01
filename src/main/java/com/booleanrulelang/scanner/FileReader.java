package com.booleanrulelang.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class FileReader {
	
	public String readFile(String fileName) {
		File file = new File(fileName);
		
		String data = "";
		try (Scanner reader = new Scanner(file)) {
			while (reader.hasNextLine()) {
				data = reader.nextLine();
				System.out.println(data);
			}
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		return data;
	}
}
