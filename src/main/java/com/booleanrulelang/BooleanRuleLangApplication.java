package com.booleanrulelang;

import com.booleanrulelang.compilerEngine.CompilerEngine;
import com.booleanrulelang.exception.CompilerException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BooleanRuleLangApplication implements CommandLineRunner {

	private static final String STRICT_EXIT_ENV = "BOOLEANRULE_STRICT_EXIT";

	private final CompilerEngine compilerEngine;

	public static void main(String[] args) {
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

		} catch (CompilerException e) {
			System.err.println(e.getFormattedMessage());
			if (strictExit()) {
				System.exit(e.getExitCode());
			}
		} catch (Exception e) {
			System.err.println("Internal Compiler Error: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * When set to "1", type/parse/source errors call {@link System#exit(int)} so shells
	 * see a non-zero status. Default is off so {@code mvn spring-boot:run} does not report
	 * a Maven failure for expected diagnostics.
	 */
	private static boolean strictExit() {
		return "1".equals(System.getenv(STRICT_EXIT_ENV));
	}
}
