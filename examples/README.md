# Demo programs

These `.txt` files are **real source** for the Boolean Rule Language compiler. The language currently has **no comment syntax** (`//` is invalid in source files).

| File | What it illustrates |
|------|---------------------|
| `demo-arithmetic.txt` | Chained `+` / `*` (precedence) and unary `-` with parentheses |
| `demo-logic.txt` | Keyword logic: `not`, `and` chaining, `and` tighter than `or` |
| `demo-comparison.txt` | Comparisons (`>`, `<=`, `==`) combined with `and` |
| `demo-parens.txt` | Using parentheses to change parsing / nesting |

Expected tool behavior today: lex → parse → print **AST pre-order traversal** to stdout (no execution).

## Run manually

From the project root:

```powershell
mvn -q spring-boot:run "-Dspring-boot.run.arguments=examples/demo-logic.txt"
```

Or package once and reuse the JAR:

```powershell
mvn -q package
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\demo-logic.txt
```

## Run all demos (PowerShell)

```powershell
.\scripts\run-demo.ps1
```

Requires JDK 21+ and Maven on `PATH`.
