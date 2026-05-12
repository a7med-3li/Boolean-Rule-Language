# Boolean Rule Language — Team Plan & Full Source Tour

> Audience: a 5-person team building a compiler for a small DSL focused on **boolean logic, comparisons, and arithmetic**.
>
> This document is split into:
>
> 1. **Project framing** — what we are building in compiler-theory terms.
> 2. **5-member work plan** — who owns what, what is already done, what they should pick up next.
> 3. **Package purposes** — what each top-level folder in `src/main/java/com/booleanrulelang/` is responsible for.
> 4. **Class-by-class tour** — every single `.java` file under `src/`, with the compiler concept it implements.
> 5. **Pipeline trace** — how one source statement flows through the system.
> 6. **Test class** — what the JUnit code does and how to extend it.

---

## 1. Project framing for the team

### 1.1 What "Boolean Rule Language" is

A **domain-specific language (DSL)** with two statements (`assignment` and `print`) and expressions over:

- **Numbers** (integer-looking, stored as `double`)
- **Booleans** (`true`, `false`)
- Arithmetic operators `+ - * /` and unary `-`
- Comparison operators `< > <= >= == !=`
- Logical operators `and or` and unary `not`
- Identifiers (variables) and parentheses for grouping

### 1.2 Compiler pipeline (and how the folders map onto it)

```
source file ──▶ Scanner ──▶ Tokens ──▶ Parser ──▶ AST ──▶ TypeChecker ──▶ Interpreter ──▶ stdout
                 (lex)                  (syntax)         (semantics)        (runtime)
```

| Phase | Compiler theory | Implementing folder |
|-------|-----------------|---------------------|
| Lexical analysis | Group characters into tokens | `scanner/` |
| Syntax analysis | Recognize phrases via grammar | `parser/` |
| Intermediate representation | Build & operate on AST | `domain/` + `visitor/` |
| Semantic analysis | Static type checking | `semantics/TypeChecker` + `types/` |
| Execution | Tree-walking interpreter | `semantics/Interpreter` + `runtime/` |
| Diagnostics | Errors with phase + position | `exception/` |
| Orchestration | Drive the pipeline | `compilerEngine/` |
| Entry point | CLI / Spring Boot | `BooleanRuleLangApplication` (root) |

### 1.3 Output the project produces today

Each successful run prints four labeled sections:

1. `== Tokens (scanner output) ==`
2. `== AST (pre-order traversal) ==`
3. `== Type check: OK ==` (or `Type error: …`)
4. `== Execution ==`

This separation is **deliberate** — we use it to **visualize each compiler phase** during the discussion.

---

## 2. Team work plan (5 members)

Each member is the **primary owner** of an area, but reviews/coordinates with the others on shared interfaces (e.g. anyone touching `ASTVisitor` coordinates with C, A, D).

### Member A — Scanner & Tokens (lexical front-end)

**Owns:** `scanner/Lexer.java`, `scanner/TokenPrinter.java`, `domain/Token.java`, `domain/TokenType.java`, `exception/UnrecognizedText.java`, `exception/SourceFileException.java`.

**Already done:**

- Character stream reading with `PushbackReader` (lookahead).
- `TokenType` enum (categories).
- `Token` record carrying `(type, lexeme, line)`.
- Lookahead handling for `:=`, `==`, `!=`, `<=`, `>=`.
- Keyword vs identifier classification (case-insensitive on keywords).
- Independent token listing via `TokenPrinter`.

**Suggested next features:**

- **Column tracking** in `Token` for better error messages.
- **Line comments** (`// …` to end of line) and/or **block comments** (`/* … */`).
- **Float literals** (e.g. `3.14`).
- **Identifiers with underscores/digits** after the first letter (currently letters only).
- **Better lexical errors**: show the surrounding line snippet with a caret.

### Member B — Parser & Grammar (syntactic front-end)

**Owns:** `parser/Parser.java`, `domain/ProgramNode.java`, `domain/AssignNode.java`, `domain/PrintNode.java`, `exception/SyntaxException.java`, `exception/UnexpectedTokenException.java`, `exception/StatementException.java`.

**Already done:**

- Recursive-descent grammar with the precedence layering required by the project decisions.
- One-token lookahead via `peek()` / `check()` / `expect()`.
- Statements: `assignment` and `print`.
- Comparison and arithmetic kept as separate grammar tiers.

**Suggested next features:**

- **Control-flow statements**: `if (cond) { … } else { … }`, `while (cond) { … }` (would introduce a `BlockNode`).
- **Comparison chains rejection** with a friendly message (`a < b < c`).
- **Error recovery** (synchronize on `;` after a parse error to keep reporting).
- **Source locations on AST nodes** (line/column on every node, not just inside error messages).
- **Boolean equality with `!=` between booleans** is allowed; document and test it.

### Member C — AST & Visitors (intermediate representation)

**Owns:** `domain/Node.java`, all expression node classes (`ArithmeticOpNode`, `ComparisonOpNode`, `LogicalOpNode`, `NotNode`, `NegationNode`, `NumberNode`, `IdentifierNode`, `BoolNode`), `visitor/ASTVisitor.java`, `visitor/ASTJsonPrinter.java`, `visitor/ASTTraversalPrinter.java`.

**Already done:**

- `Node` abstract base + `accept(visitor)` for **double dispatch**.
- Operator-family split so arithmetic vs comparison vs logical are **distinct classes** (matches `decision.md` requirement).
- JSON dump visitor (still available as a debug tool) and pre-order text visitor (used in `CompilerEngine`).

**Suggested next features:**

- **Pretty-printer visitor** that re-emits valid source from the AST.
- **Constant folding** visitor (`2 + 3` collapses to `5`).
- **DOT/Graphviz output** for diagrams.
- **Source locations** propagated from tokens into nodes (collaborate with Member B).
- **Post-order / level-order** traversal variants for teaching slides.

### Member D — Semantics & Runtime (type system + interpreter)

**Owns:** `semantics/TypeChecker.java`, `semantics/Interpreter.java`, `types/Type.java`, `runtime/RuntimeValue.java`, `runtime/NumberValue.java`, `runtime/BooleanValue.java`, `exception/TypeCheckException.java`, `exception/EvaluationException.java`.

**Already done:**

- Type rules for every operator family (arithmetic, comparison with same-type `==`/`!=`, logical, unary).
- Symbol table during type checking (`Map<String, Type>`).
- Tree-walking interpreter with environment (`Map<String, RuntimeValue>`).
- Sealed `RuntimeValue` hierarchy for `Number` and `Boolean`.
- Division-by-zero check.

**Suggested next features:**

- **Block scoping** when control flow lands.
- **String type** (`StringValue` + `String` type), with `+` overloaded for concat.
- **REPL** mode (read line, type check, evaluate).
- **Evaluation traces** for the discussion: print the value at every intermediate sub-expression.
- **Short-circuit semantics** for `and`/`or` (the interpreter already does this naturally; document it).

### Member E — Compiler Engine, Tooling & Testing

**Owns:** `BooleanRuleLangApplication.java`, `compilerEngine/CompilerEngine.java`, `exception/CompilerException.java`, Maven config (`pom.xml`), Spring config (`application.properties`), `scripts/run-demo.ps1`, `scripts/run-tests.ps1`, `examples/`, all `docs/*.md`, the JUnit harness.

**Already done:**

- CLI wiring through Spring Boot (`CommandLineRunner`).
- Pipeline orchestration in `CompilerEngine`.
- Diagnostic exit-code policy (`BOOLEANRULE_STRICT_EXIT`).
- Test corpus (`examples/tests/pass`, `examples/tests/fail`) and runner script.
- Docs: project guide, traversal explanation, decision checklist, testing guide.

**Suggested next features:**

- **Golden-file JUnit tests** that read each `examples/tests/**/*.txt`, run the pipeline, and compare against a `.expected` file.
- **GitHub Actions** workflow to run `mvn test` on push.
- **`--phase` flag** to stop after lex / parse / typecheck for teaching mode.
- **`--ast-json`** to re-enable the existing `ASTJsonPrinter`.
- **REPL launcher** when D is ready.

### Joint responsibilities

- **`ASTVisitor` interface changes** require A (if a new token category), C (new node), and D (new typing/eval rule).
- **Diagnostics format** — when E adds golden-file tests, A/B/D coordinate to keep messages stable.
- **Documentation** is everyone's job; each PR updates the matching `.md`.
- **Code review** rotates so every member reads code from every area at least once a sprint.

---

## 3. Package purposes

### 3.1 Root package `com.booleanrulelang`

The CLI entry point lives here. Spring Boot uses it to scan the rest of the packages and wire beans.

### 3.2 `scanner/` — Lexical analysis

**Compiler concept:** *Lexical analysis* turns a flat **character stream** from the source file into a **token stream** that the parser can consume. Tokens are atomic units (categories) with attached lexeme text and line info.

This package owns reading the file, classifying characters, handling **lookahead** for multi-character operators, and the keyword table.

### 3.3 `parser/` — Syntax analysis

**Compiler concept:** *Syntax analysis* (a.k.a. parsing) checks that the token sequence respects the **grammar** of the language and builds the **Abstract Syntax Tree (AST)**. We use **recursive descent** because the grammar is LL(1) and the operator precedence is encoded by the order of method calls — `parseLogicOr → parseLogicAnd → … → parsePrimary`.

### 3.4 `domain/` — Tokens & AST (the IR)

**Compiler concept:** The **intermediate representation (IR)** the rest of the pipeline operates on. Two families live here:

- **`Token`** + **`TokenType`** — output of the scanner.
- **`Node`** + all its subclasses — output of the parser.

We deliberately keep **separate AST classes** for arithmetic / comparison / logical / `not` / unary `-`, so the type checker, interpreter, and printers can distinguish them by Java type rather than string matching.

### 3.5 `visitor/` — AST operations (visitor pattern)

**Compiler concept:** The **visitor pattern** decouples *what we want to do with the AST* from *the AST classes themselves*. `Node.accept(visitor)` uses **double dispatch**: the concrete node class chooses the right `visitor.visitXxx(...)` method.

We have two concrete visitors today: `ASTJsonPrinter` (debug) and `ASTTraversalPrinter` (used in the engine's output).

### 3.6 `semantics/` — Semantic analysis & execution

**Compiler concept:**

- **Semantic analysis** = static checks beyond syntax (here: type rules per operator and scope of variables).
- **Execution** = either codegen + runtime, or, in our case, a **tree-walking interpreter** that evaluates the AST directly.

We do both in this package: `TypeChecker` reports type errors *before* execution, then `Interpreter` walks the same AST to produce values.

### 3.7 `types/` — Static type domain

**Compiler concept:** The lattice of types the type checker reasons about. For us this is small: `NUMBER` and `BOOLEAN`. Keeping it in its own package makes future extensions (e.g. `STRING`, `UNIT`) obvious.

### 3.8 `runtime/` — Runtime value domain

**Compiler concept:** The values that exist **at run time**. Distinct from `types/` because static types are erased after type checking; what we manipulate during execution is a tagged value (`NumberValue` / `BooleanValue`).

### 3.9 `exception/` — Diagnostics

**Compiler concept:** A uniform error hierarchy that lets every phase throw a typed exception with a **phase-specific message** and an **exit code** policy. The CLI catches `CompilerException` and decides whether to call `System.exit`.

### 3.10 `compilerEngine/` — Orchestrator

**Compiler concept:** The **driver** that runs the phases in order, prints intermediate artifacts, and connects the parts. Sometimes called the "compilation manager" or "front-end driver."

---

## 4. Class-by-class tour

For every class, this section gives:

- **What it is** (one line).
- **Compiler concept** it implements.
- **Role / what it does**.
- **Pointers** to notable code patterns.

### 4.1 Root

#### `BooleanRuleLangApplication.java`

- **What:** Spring Boot application class implementing `CommandLineRunner`.
- **Compiler concept:** Command-line entry point / driver.
- **Role:** Bootstraps Spring (so `CompilerEngine` and friends get auto-wired), reads the first argument as the source file path, calls `compilerEngine.compile(path)`, and translates `CompilerException` into a stderr message (with optional `System.exit` controlled by `BOOLEANRULE_STRICT_EXIT=1`).
- **Pointers:** `strictExit()` reads the env var; the catch chain ensures internal errors are clearly distinguished from compiler errors.

### 4.2 `compilerEngine/`

#### `CompilerEngine.java`

- **What:** Spring `@Component` that runs the whole pipeline for one file.
- **Compiler concept:** Pipeline orchestrator (driver).
- **Role:** `compile(filePath)` calls `Lexer → TokenPrinter → Parser → ASTTraversalPrinter → TypeChecker → Interpreter`, printing labelled sections in between so the user sees every phase's product.
- **Pointers:** Constructor injection via Lombok `@RequiredArgsConstructor` makes the dependency list explicit (this is where you would add a new phase, e.g. an optimizer visitor).

### 4.3 `scanner/`

#### `Lexer.java`

- **What:** Hand-written scanner.
- **Compiler concept:** Lexical analyzer (regular-language recognizer expressed as code).
- **Role:** Reads the file character by character with a `PushbackReader`, classifies characters, builds a `List<Token>`, terminates with `Token(EOF)`. Handles multi-character operators by reading one extra char and **unreading** it if the second char does not match. Whitespace is skipped; `\n` advances the line counter. Keywords are recognized via a `Map<String, TokenType>` (case-insensitive on the key).
- **Pointers:** `readNumber` and `readIdentifier` are small helper sub-machines; both demonstrate the standard "accumulate while predicate holds, then unread the breaking char" pattern.

#### `TokenPrinter.java`

- **What:** Small formatter for the lexer output.
- **Compiler concept:** Inspect/debug the **token stream** independently — supports the project decision item *"scanner output is visible independently"*.
- **Role:** Aligned, numbered table of `(TokenType, lexeme, line)`. Empty lexeme for `EOF` is shown as `<eof>`.
- **Pointers:** Width computation makes the table readable for any input size.

### 4.4 `parser/`

#### `Parser.java`

- **What:** Recursive-descent parser.
- **Compiler concept:** *Syntax analysis*. Each grammar non-terminal is a method; precedence is encoded by which method calls which.
- **Role / grammar mapping:**

  ```
  program     -> parseProgram      (statement* EOF)
  statement   -> parseStatement    -> parsePrint | parseAssignment
  assignment  -> parseAssignment   (ID := expression ;)
  print       -> parsePrint        (print expression ;)
  expression  -> parseExpression   -> parseLogicOr
  logicOr     -> parseLogicOr      (logicAnd (or logicAnd)*)
  logicAnd    -> parseLogicAnd     (logicNot (and logicNot)*)
  logicNot    -> parseLogicNot     (not logicNot | comparison)
  comparison  -> parseComparison   (arith [op arith])
  arith       -> parseArithmetic   (term (+|- term)*)
  term        -> parseTerm         (factor (*|/ factor)*)
  factor      -> parseFactor       (- primary | primary)
  primary     -> parsePrimary      (NUMBER | ID | TRUE | FALSE | ( expression ))
  ```

- **Pointers:** `peek` / `advance` / `check` / `expect` form the standard recursive-descent toolkit. `parseLogicNot` is **right-recursive** (so `not not x` parses as `not (not x)`). `parseProgram` resets `currentPosition` so the same parser bean can be reused on multiple files.

### 4.5 `domain/`

#### `Token.java`

- **What:** Immutable Java record `(TokenType type, String lexeme, int line)`.
- **Compiler concept:** The **token** — pairing the **lexical category** with the **raw lexeme** and a position.
- **Role:** Carries everything the parser needs to make a decision.

#### `TokenType.java`

- **What:** `enum` of token categories.
- **Compiler concept:** The finite set of **terminal symbols** of the grammar (plus `EOF`).
- **Role:** Grouped by intent (keywords, identifiers, literals, arithmetic ops, comparison ops, assignment, delimiters, EOF). Used everywhere `check(TokenType.XYZ)` appears in the parser.

#### `Node.java`

- **What:** Abstract base of all AST nodes; declares `<T> T accept(ASTVisitor<T> visitor)`.
- **Compiler concept:** Root of the **AST** + **visitor double dispatch** mechanism.
- **Role:** Every concrete subclass implements `accept` by calling the appropriate `visitor.visitXxx(this)` — this is what allows multiple visitor algorithms over the same tree without modifying the tree classes.

#### `ProgramNode.java`

- **What:** Root AST node holding `List<Node> statements`.
- **Compiler concept:** Top-level non-terminal in the grammar.
- **Role:** The handle that the type checker, interpreter, and printers receive.

#### `AssignNode.java`

- **What:** Statement node for `name := value`.
- **Compiler concept:** Mutating statement; introduces or rebinds a variable in the symbol/runtime environment.
- **Role:** Holds the target variable name and the value expression node.

#### `PrintNode.java`

- **What:** Statement node for `print expression`.
- **Compiler concept:** Side-effecting statement.
- **Role:** The interpreter evaluates `expression` and writes it to stdout.

#### `ArithmeticOpNode.java`

- **What:** Binary node carrying `op` ∈ `{+, -, *, /}` and `left`, `right` children.
- **Compiler concept:** Arithmetic expression node — distinct **AST class** from comparison/logical (encodes the decision-doc requirement "boolean and arithmetic AST nodes are distinguishable").
- **Role:** Type checker enforces NUMBER × NUMBER → NUMBER; interpreter does the actual arithmetic, including division-by-zero detection.

#### `ComparisonOpNode.java`

- **What:** Binary node carrying `op` ∈ `{<, >, <=, >=, ==, !=}` and two children.
- **Compiler concept:** Relational/equality expression node, kept **separate** from arithmetic so comparisons never appear inside arithmetic operands at the AST level.
- **Role:** For `< > <= >=` both sides must be NUMBER; for `== !=` both sides must share a type. Result is BOOLEAN.

#### `LogicalOpNode.java`

- **What:** Binary node carrying `op` ∈ `{and, or}`.
- **Compiler concept:** Boolean connective node.
- **Role:** Both operands must be BOOLEAN; result is BOOLEAN. Interpreter naturally short-circuits because Java's `&&`/`||` do.

#### `NotNode.java`

- **What:** Unary node for `not operand`.
- **Compiler concept:** Logical negation — separate Java class so it never gets confused with arithmetic negation.
- **Role:** Operand must be BOOLEAN; result BOOLEAN.

#### `NegationNode.java`

- **What:** Unary node for `- operand` (arithmetic).
- **Compiler concept:** Arithmetic negation.
- **Role:** Operand must be NUMBER; result NUMBER.

#### `NumberNode.java`

- **What:** Leaf carrying a `double value`.
- **Compiler concept:** Numeric literal.
- **Role:** Parser builds it via `Double.parseDouble(lexeme)`; printers display integer-valued doubles without `.0` for readability.

#### `BoolNode.java`

- **What:** Leaf carrying a `boolean value`.
- **Compiler concept:** Boolean literal.
- **Role:** Always typed BOOLEAN.

#### `IdentifierNode.java`

- **What:** Leaf carrying a `String name`.
- **Compiler concept:** Variable reference — the lookup happens later (in the type checker and interpreter, not in the parser).
- **Role:** Type checker rejects unbound names; interpreter reads the value from its environment.

### 4.6 `visitor/`

#### `ASTVisitor.java`

- **What:** Generic interface `ASTVisitor<T>` with one `visit…` method per concrete node.
- **Compiler concept:** Visitor contract; the **operation surface** for the AST.
- **Role:** Anyone wanting to run an algorithm over the AST (typecheck, evaluate, print, optimize, codegen) implements this interface.

#### `ASTJsonPrinter.java`

- **What:** `ASTVisitor<JSONObject>` that builds a tagged JSON tree.
- **Compiler concept:** Serialization of the IR for inspection.
- **Role:** Not used in the default pipeline now, but kept as a debugging tool. Useful when developing new passes.

#### `ASTTraversalPrinter.java`

- **What:** Public wrapper that delegates to a private inner `PreOrderWalker implements ASTVisitor<Void>`.
- **Compiler concept:** Depth-first **pre-order** traversal — the indentation visualizes nesting depth and the order shows the recursion.
- **Role:** Produces the `== AST (pre-order traversal) ==` section so audiences can see the parser's product without reading JSON.

### 4.7 `semantics/`

#### `TypeChecker.java`

- **What:** Spring `@Component` that walks the AST with a `Map<String, Type>` "type environment."
- **Compiler concept:** Static **semantic analysis** — every operator has typing rules; identifier use must follow a definition.
- **Role:** Returns the type of any expression via `inferExpr`. For statements, threads the environment so `x` is known on later statements after `x := …`. Throws `TypeCheckException` with a precise message on the first violation.

#### `Interpreter.java`

- **What:** Spring `@Component` that walks the AST with a `Map<String, RuntimeValue>` runtime environment.
- **Compiler concept:** **Tree-walking interpreter** — directly evaluates each node instead of generating code.
- **Role:** For `print` writes the value; for `:=` stores the value and prints `name := value` so the user sees what landed in the variable. Java pattern-matching `switch` selects per-operator behavior. Division-by-zero turns into `EvaluationException`.

### 4.8 `types/`

#### `Type.java`

- **What:** Enum `{NUMBER, BOOLEAN}`.
- **Compiler concept:** Static type domain used by the type checker.
- **Role:** Kept minimal so adding `STRING` later is one enum entry plus rule updates.

### 4.9 `runtime/`

#### `RuntimeValue.java`

- **What:** Sealed interface permitting `NumberValue` and `BooleanValue`.
- **Compiler concept:** Tagged value at run-time; the **dynamic** counterpart of `types/Type`.
- **Role:** Exhaustive `switch` over `RuntimeValue` works because the interface is sealed.

#### `NumberValue.java`

- **What:** `record NumberValue(double value)`.
- **Compiler concept:** Boxed numeric runtime value.

#### `BooleanValue.java`

- **What:** `record BooleanValue(boolean value)`.
- **Compiler concept:** Boxed boolean runtime value.

### 4.10 `exception/`

#### `CompilerException.java`

- **What:** Abstract base extending `RuntimeException` with `exitCode`, `line`, `column`.
- **Compiler concept:** Common shape for all phase-level diagnostics; lets the driver treat all of them uniformly.

#### `SourceFileException.java`

- **What:** Thrown when the source file cannot be opened/read.
- **Role:** Reported before any phase runs.

#### `UnrecognizedText.java`

- **What:** Thrown by the **scanner** on an illegal character.
- **Compiler concept:** Lexical error.

#### `SyntaxException.java`

- **What:** Thrown by `expect(...)` in the parser when the current token does not match.
- **Compiler concept:** Syntax error.

#### `UnexpectedTokenException.java`

- **What:** Thrown by `parsePrimary` when no valid primary expression starts here.
- **Compiler concept:** Syntax error (a primary expression was required).

#### `StatementException.java`

- **What:** Thrown by `parseStatement` when neither `print` nor an identifier begins the statement.
- **Compiler concept:** Syntax error at statement level.

#### `TypeCheckException.java`

- **What:** Thrown by the type checker.
- **Compiler concept:** Semantic error. Overrides `getFormattedMessage()` to drop the `[line:column]` prefix because we don't yet track per-node positions.

#### `EvaluationException.java`

- **What:** Thrown by the interpreter when a runtime issue happens (e.g. division by zero).
- **Compiler concept:** Runtime error (rare — most things are caught earlier by the type checker).

### 4.11 Test sources

#### `src/test/java/com/booleanrulelang/BooleanRuleLangApplicationTests.java`

- **What:** JUnit 5 + Spring Boot test class with one `contextLoads` test.
- **Compiler concept:** Smoke test — makes sure the Spring wiring (all `@Component`s) initializes without exceptions.
- **Role:** Today it only proves the context loads. Member E should grow this into golden-file tests over `examples/tests/`.

---

## 5. Pipeline trace — what happens for `x := 1 + 2;`

1. **CLI:** `BooleanRuleLangApplication.run` receives `args[0]` and calls `CompilerEngine.compile`.
2. **Lexer:** opens the file via `PushbackReader`, emits the token stream:

   ```text
   Token(ID, "x", 1)
   Token(ASSIGN, ":=", 1)
   Token(NUMBER, "1", 1)
   Token(PLUS, "+", 1)
   Token(NUMBER, "2", 1)
   Token(SEMICOLON, ";", 1)
   Token(EOF, "", 1)
   ```

3. **TokenPrinter:** writes the `== Tokens ==` section.
4. **Parser:** consumes the tokens by recursive descent:

   ```
   parseProgram → parseStatement (sees ID, dispatches to parseAssignment)
     parseAssignment: expect ID, expect ":=", parseExpression, expect ";"
       parseExpression → parseLogicOr → parseLogicAnd → parseLogicNot → parseComparison → parseArithmetic
         parseArithmetic: parseTerm (parseFactor → parsePrimary → NumberNode(1))
                          then PLUS → parseTerm → NumberNode(2)
                          builds ArithmeticOpNode("+", 1, 2)
   ```

   AST:

   ```
   ProgramNode
   └─ AssignNode(name="x")
       └─ ArithmeticOpNode(op="+")
           ├─ NumberNode(1)
           └─ NumberNode(2)
   ```

5. **ASTTraversalPrinter:** prints `program / assign x / arithmetic + / number 1 / number 2`.
6. **TypeChecker:**
   - Enters `AssignNode` → `inferExpr(value)` on `ArithmeticOpNode("+", …)` → both operands `NUMBER` → result `NUMBER`.
   - Records `env["x"] = NUMBER`.
   - Prints `== Type check: OK ==`.
7. **Interpreter:**
   - Evaluates `1 + 2` to `NumberValue(3.0)`.
   - Stores `env["x"] = NumberValue(3.0)`.
   - Prints `x := 3`.
8. **Done.**

If any phase fails, the relevant `CompilerException` is thrown, the `BooleanRuleLangApplication` catches it, prints the formatted message on stderr, and (depending on `BOOLEANRULE_STRICT_EXIT`) sets a non-zero exit code.

---

## 6. Test class & how to extend it

Currently `BooleanRuleLangApplicationTests` does only `@SpringBootTest` smoke testing — it confirms the bean graph wires correctly with all `@Component`s present.

### Recommended grow path for Member E

Add a golden-file test:

```java
@SpringBootTest
class CompilerEngineGoldenTests {

    @Autowired CompilerEngine engine;

    @ParameterizedTest
    @MethodSource("cases")
    void program_runs_and_matches_expected(Path source, Path expected) throws Exception {
        var sysOut = redirectStdout();
        engine.compile(source.toString());
        var got = sysOut.captured();
        var want = Files.readString(expected);
        assertEquals(want.strip(), got.strip());
    }

    static Stream<Arguments> cases() throws Exception {
        return Files.walk(Path.of("examples/tests/pass"))
            .filter(p -> p.toString().endsWith(".txt"))
            .map(p -> Arguments.of(p, Path.of(p + ".expected")));
    }
}
```

Where each `examples/tests/pass/*.txt` gets a sibling `*.txt.expected` file. Same idea for `fail/*.txt` but checking captured stderr.

This converts the **manual demo** into an **automated regression test** without changing any production code — exactly the kind of work Member E owns.

---

## 7. Quick reference — class to compiler concept

| Class | Folder | Compiler concept |
|------|--------|------------------|
| `BooleanRuleLangApplication` | root | Driver / CLI |
| `CompilerEngine` | compilerEngine | Pipeline orchestrator |
| `Lexer` | scanner | Lexical analyzer |
| `TokenPrinter` | scanner | Phase-inspection tool |
| `Parser` | parser | Recursive-descent syntax analyzer |
| `Token` | domain | Token (cat + lexeme + line) |
| `TokenType` | domain | Terminal symbols (enum) |
| `Node` | domain | AST root + visitor accept |
| `ProgramNode` | domain | Top-level non-terminal |
| `AssignNode` | domain | Assignment statement |
| `PrintNode` | domain | Output statement |
| `ArithmeticOpNode` | domain | Arithmetic expression |
| `ComparisonOpNode` | domain | Relational/equality expression |
| `LogicalOpNode` | domain | Boolean connective |
| `NotNode` | domain | Logical unary |
| `NegationNode` | domain | Arithmetic unary |
| `NumberNode` | domain | Numeric literal |
| `BoolNode` | domain | Boolean literal |
| `IdentifierNode` | domain | Variable reference |
| `ASTVisitor` | visitor | Visitor contract (double dispatch) |
| `ASTJsonPrinter` | visitor | AST serialization visitor |
| `ASTTraversalPrinter` | visitor | Pre-order traversal visitor |
| `TypeChecker` | semantics | Static semantic analysis (typing + scoping) |
| `Interpreter` | semantics | Tree-walking interpreter (execution) |
| `Type` | types | Static type domain |
| `RuntimeValue` (sealed) | runtime | Runtime value tag |
| `NumberValue` | runtime | Numeric runtime value |
| `BooleanValue` | runtime | Boolean runtime value |
| `CompilerException` | exception | Phase-error base type |
| `SourceFileException` | exception | I/O error |
| `UnrecognizedText` | exception | Lexical error |
| `SyntaxException` | exception | Syntax error (expectation failed) |
| `UnexpectedTokenException` | exception | Syntax error (bad primary) |
| `StatementException` | exception | Syntax error (bad statement start) |
| `TypeCheckException` | exception | Semantic error |
| `EvaluationException` | exception | Runtime error |
| `BooleanRuleLangApplicationTests` | test | Spring context smoke test |

Every class under `src/` is now mapped to a member, a folder purpose, and a compiler concept.
