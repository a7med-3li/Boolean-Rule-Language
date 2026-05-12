# Testing & Discussion Guide

This guide tells you exactly how to **run** the project, **what each section of the output means** during the discussion, and gives you a **curated test suite** under `examples/tests/` that exercises every language feature and every type/parse/lex error.

---

## 1. Prerequisites

- **JDK 21** (matches `pom.xml`'s `java.version`)
- **Maven** on the `PATH`
- Windows PowerShell (the scripts are `.ps1`; the commands themselves are cross-platform too)

Quick check:

```powershell
java -version
mvn -version
```

---

## 2. Build & sanity-check

From the repository root:

```powershell
cd "d:\Faculty\level four\compiler\Boolean-Rule-Language"
mvn test
```

`mvn test` runs the Spring Boot context test. **Expected:** BUILD SUCCESS.

Build a runnable JAR (used by the test scripts):

```powershell
mvn -q package -DskipTests
```

This produces `target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar`.

---

## 3. Running a single source file

The compiler takes **one** argument — the **path to a source file**.

```powershell
# Option A — Maven (no JAR required)
mvn -q spring-boot:run "-Dspring-boot.run.arguments=examples/tests/pass/01-arithmetic-basics.txt"

# Option B — JAR (after mvn package)
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\tests\pass\01-arithmetic-basics.txt
```

---

## 4. What the output looks like (and how to discuss it)

Every successful run prints **four clearly labelled sections** in this order. Use them as discussion talking points.

### 4.1 `== Tokens (scanner output) ==`

A numbered table of `(category, lexeme, line)`. Use this to explain:

- The difference between **`TokenType`** (the category enum) and **`lexeme`** (raw source text).
- How **lookahead** in the scanner picks `:=` over `:`, `<=` over `<`, etc.
- How **keywords** are recognized via the `keywords` table after reading a letter-run.

Example for `print 2 + 3 * 4;`:

```text
== Tokens (scanner output) ==
 1  PRINT      'print'         line 1
 2  NUMBER     '2'             line 1
 3  PLUS       '+'             line 1
 4  NUMBER     '3'             line 1
 5  STAR       '*'             line 1
 6  NUMBER     '4'             line 1
 7  SEMICOLON  ';'             line 1
 8  EOF        <eof>           line 1
```

### 4.2 `== AST (pre-order traversal) ==`

A depth-first **pre-order** print of the AST built by the recursive-descent parser. Use this to explain:

- The **grammar layering** (precedence): `logical or → and → not → comparison → +/- → */ → unary - → primary`.
- That **arithmetic, comparison, logical** ops are **separate classes** (`ArithmeticOpNode`, `ComparisonOpNode`, `LogicalOpNode`), and so are unary `-` (`NegationNode`) vs `not` (`NotNode`).
- That parentheses **reshape** the tree without producing their own node.

### 4.3 `== Type check: OK ==`

Confirmation that the static type checker accepted the program. The type system is:

| Operator(s) | Operand types | Result |
|-------------|---------------|--------|
| `+ - * /` (binary) | `NUMBER`, `NUMBER` | `NUMBER` |
| unary `-` | `NUMBER` | `NUMBER` |
| `< > <= >=` | `NUMBER`, `NUMBER` | `BOOLEAN` |
| `== !=` | same type on both sides | `BOOLEAN` |
| `and or` | `BOOLEAN`, `BOOLEAN` | `BOOLEAN` |
| `not` | `BOOLEAN` | `BOOLEAN` |
| identifier | must have been **assigned** earlier | its inferred type |

Failure prints `Type error: …` and skips execution.

### 4.4 `== Execution ==`

The interpreter runs the AST top-to-bottom:

- **`print expr;`** → prints the evaluated value (`true` / `false` / number).
- **`name := expr;`** → stores the value and prints **`name := value`** so you see what was bound. This is what the user means by "the value that will be in `x` after the traverse".

---

## 5. Running the curated test suite

The suite lives under `examples/tests/`:

- **`examples/tests/pass/`** — programs that should succeed end-to-end.
- **`examples/tests/fail/`** — programs that should be rejected with a clear error.

Run everything in one shot:

```powershell
.\scripts\run-tests.ps1
```

The script:

1. Builds the JAR if it isn't there yet.
2. Iterates each `.txt` file in `pass/` then `fail/`.
3. Prints a banner with the expected outcome and pipes the compiler's full output.

You can also run any single case:

```powershell
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\tests\fail\04-type-eq-mismatch.txt
```

---

## 6. Pass cases (`examples/tests/pass/`)

> Every program below is **valid**. The discussion focus is *what the output proves*.

### 6.1 `01-arithmetic-basics.txt` — operator precedence & parens

```text
print 2 + 3 * 4;
print (2 + 3) * 4;
print 10 - 6 / 2;
```

**Talking points:**

- `*` binds tighter than `+` → first line is **14**, not 20.
- Parentheses **override** precedence → second line is **20**.
- `/` binds tighter than `-` → third line is **7**.

**Expected execution:**

```text
14
20
7
```

### 6.2 `02-unary-minus.txt` — unary minus and assignment

```text
print -5;
print -(1 + 2);
y := -10;
print y;
```

**Talking points:** unary `-` is its own AST node (`NegationNode`). Reading `y` after assignment shows the environment.

**Expected execution:**

```text
-5
-3
y := -10
-10
```

### 6.3 `03-boolean-and-not.txt` — boolean literals & `not`

```text
print true;
print false;
print not true;
print not not true;
```

**Talking points:** `not` is right-recursive (`parseLogicNot` calls itself), so `not not true` parses as `not (not true)`.

**Expected execution:**

```text
true
false
false
true
```

### 6.4 `04-logical-binary.txt` — `and` / `or` precedence

```text
print true or false and false;
print (true or false) and false;
print true and false or true;
```

**Talking points:** `and` binds tighter than `or`; parens change the shape.

**Expected execution:**

```text
true
false
true
```

(First: `false and false = false`, then `true or false = true`. Second: `(true or false) and false = false`. Third: `(true and false) or true = true`.)

### 6.5 `05-comparison.txt` — relational & equality

```text
print 1 < 2;
print 2 == 2;
print 5 != 5;
print 3 + 4 == 7;
print true == true;
```

**Talking points:** `< > <= >=` need numbers; `== !=` accept any same-type pair.

**Expected execution:**

```text
true
true
false
true
true
```

### 6.6 `06-assignment-and-use.txt` — variables across statements

```text
x := 10;
y := x * 2 + 1;
print y;
flag := y > 15;
print flag;
```

**Talking points:** the interpreter keeps a `Map<String, RuntimeValue>` environment; the type checker keeps a parallel `Map<String, Type>` to validate `+`, `>`, etc.

**Expected execution:**

```text
x := 10
y := 21
21
flag := true
true
```

### 6.7 `07-scanner-coverage.txt` — wide token coverage in one line

```text
result := (1 + 2) * 3 >= 9 and not false;
print result;
```

**Talking points:** in the **Tokens** section you can see `LPAREN`, `RPAREN`, `PLUS`, `STAR`, `GEQ`, `AND`, `NOT`, `FALSE`, `ASSIGN`, `SEMICOLON`, `ID`, `NUMBER`, `PRINT`, `EOF` — almost every category.

**Expected execution:**

```text
result := true
true
```

---

## 7. Fail cases (`examples/tests/fail/`)

> Every program below should be **rejected**. The discussion focus is *which phase reports it and how clearly*.

| File | Phase that rejects | Expected message (substring) |
|------|---------------------|------------------------------|
| `01-type-bool-plus-number.txt` | type check | `Type error: left operand of '+' must be NUMBER, was BOOLEAN` |
| `02-type-and-on-numbers.txt` | type check | `Type error: left operand of 'and' must be BOOLEAN, was NUMBER` |
| `03-type-not-on-number.txt` | type check | `Type error: operand of 'not' must be BOOLEAN, was NUMBER` |
| `04-type-eq-mismatch.txt` | type check | `Type error: Operands of '==' must have the same type (got BOOLEAN and NUMBER)` |
| `05-type-undefined-variable.txt` | type check | `Type error: Undefined variable 'unknownVar'` |
| `06-syntax-missing-semicolon.txt` | parser | `Parse error at line 1: expected ; but got ''` |
| `07-lex-bad-character.txt` | scanner | `Unrecognized character: '@' at line 1` |

### 7.1 What each one demonstrates

**`01-type-bool-plus-number.txt`** — `x := true + 5;`  
*The scanner and parser both succeed: tokens and AST are printed first. The type checker rejects the program; execution is skipped.*

**`02-type-and-on-numbers.txt`** — `print 1 and 2;`  
*Numbers cannot be combined with `and` (a logical operator), even though the syntax is fine.*

**`03-type-not-on-number.txt`** — `print not 7;`  
*Unary `not` requires a boolean operand.*

**`04-type-eq-mismatch.txt`** — `print true == 1;`  
*Equality is allowed but the **types must match** on both sides.*

**`05-type-undefined-variable.txt`** — `print unknownVar;`  
*Variables must be assigned before they are referenced. Caught **before** execution.*

**`06-syntax-missing-semicolon.txt`** — `print 1 + 2`  
*Parser-level rejection: no `;` to close the statement. Comes **before** the type-check section is reached.*

**`07-lex-bad-character.txt`** — `x := 1 @ 2;`  
*Scanner-level rejection on `@`. Comes **before** the AST is built.*

### 7.2 Note on the process exit code

By default the application **does not call `System.exit` on a `CompilerException`** (so `mvn spring-boot:run` does not show `BUILD FAILURE`). The error still appears on **stderr**.  
If you need a non-zero exit code (e.g. for CI), set:

```powershell
$env:BOOLEANRULE_STRICT_EXIT='1'
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar examples\tests\fail\01-type-bool-plus-number.txt
```

---

## 8. Suggested discussion flow

1. **Compile & test:** run `mvn test` once.
2. **Walk through a pass case:** run `pass/06-assignment-and-use.txt` and explain each of the four output sections.
3. **Show precedence:** compare `pass/01-arithmetic-basics.txt` and `pass/04-logical-binary.txt`.
4. **Show token vs lexeme:** zoom into the **Tokens** section of `pass/07-scanner-coverage.txt`.
5. **Show AST distinction:** point at `arithmetic +` vs `comparison >=` vs `logical and` in the AST output of `pass/07-scanner-coverage.txt` (these are different classes — `ArithmeticOpNode`, `ComparisonOpNode`, `LogicalOpNode`).
6. **Show diagnostics:** run a few `fail/*.txt` and call out which phase rejects each (lexer / parser / type checker).
7. **Replay everything in one shot:** `.\scripts\run-tests.ps1`.

---

## 9. Cheat sheet

```powershell
# build & run JUnit
mvn test

# single program
java -jar target\Boolean-rule-lang-0.0.1-SNAPSHOT.jar <path-to-source>

# every demo program (examples\demo-*.txt)
.\scripts\run-demo.ps1

# every test case (examples\tests\pass + examples\tests\fail)
.\scripts\run-tests.ps1
```
