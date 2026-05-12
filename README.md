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

By default, **type and parse errors do not call `System.exit`**, so `mvn spring-boot:run` finishes with **BUILD SUCCESS** while still printing the error on stderr. For scripts that need a non-zero exit code, set:

`BOOLEANRULE_STRICT_EXIT=1` (e.g. PowerShell: `$env:BOOLEANRULE_STRICT_EXIT='1'; java -jar ...`).

```powershell
mvn -q spring-boot:run "-Dspring-boot.run.arguments=examples/demo-logic.txt"
```

Or:

```powershell
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\demo-logic.txt
```

On success the pipeline runs **type checking** then **interpretation**:

- **`print`** statements print the evaluated value.
- **`name := expr`** binds the variable and prints `name := value` so you see the stored value (e.g. **`x`** after `x := ...`).
- **`Type error:`** … if operands disagree with the rules (e.g. `true + 5` in [`docs/test.txt`](docs/test.txt)).

## Demo bundle

Prepared examples live under **`examples/`** (`demo-*.txt`). See **`examples/README.md`** for what each script shows.

**Run all demos (PowerShell, from repo root):**

```powershell
.\scripts\run-demo.ps1
```

The script builds the JAR once if missing, then compiles each `examples\demo-*.txt` file.
