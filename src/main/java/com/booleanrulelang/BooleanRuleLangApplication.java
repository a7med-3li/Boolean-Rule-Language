package com.booleanrulelang;

import com.booleanrulelang.compilerEngine.CompilerEngine;
import com.booleanrulelang.exception.CompilerException;
import com.booleanrulelang.exception.SourceFileException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BooleanRuleLangApplication implements CommandLineRunner {
	
	private final CompilerEngine compilerEngine;
	
	public static void main(String[] args) {
		//System.out.println("Usage: java -jar <app.jar> <source-file-path>");
		SpringApplication.run(BooleanRuleLangApplication.class, args);
	}
	
	@Override
	public void run(String @NonNull ... args) {
		try {
			if (args.length == 0) {
				System.err.println("Usage: java -jar <app.jar> <source-file-path>");
				return;
			}
			
			String sourcePath = args[0];
			compilerEngine.compile(sourcePath);

		} catch(SourceFileException e) {
			throw new SourceFileException(e.getMessage());
		} catch (CompilerException e) {
			System.err.println(e.getFormattedMessage());
			System.exit(e.getExitCode());
		} catch (Exception e) {
			System.err.println("Internal Compiler Error: " + e.getMessage());
			System.exit(1);
		}
	}
}
