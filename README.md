# Boolean Rule Language

A small domain-specific language aimed at **comparisons, boolean logic, and arithmetic**, implemented in Java as **lexer → recursive-descent parser → AST traversal output** (no interpreter yet).

## Requirements

- **JDK 21** (see `java.version` in `pom.xml`)
- **Maven**

## Build and tests

```bash
mvn test
mvn -q package
```

## Run the compiler

Pass **one argument**: path to a **source file**.

```powershell
mvn -q spring-boot:run "-Dspring-boot.run.arguments=examples/demo-logic.txt"
```

Or:

```powershell
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\demo-logic.txt
```

On success you get an **AST pre-order traversal** printed to stdout.

## Demo bundle

Prepared examples live under **`examples/`** (`demo-*.txt`). See **`examples/README.md`** for what each script shows.

**Run all demos (PowerShell, from repo root):**

```powershell
.\scripts\run-demo.ps1
```

The script builds the JAR once if missing, then compiles each `examples\demo-*.txt` file.
