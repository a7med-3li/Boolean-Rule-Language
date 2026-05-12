# Decision Checklist — Verification

This document walks through every item in [`decision.md`](decision.md) and shows where each item is implemented, what was **already satisfied**, and what was **added** in this pass.

It also includes the **Token vs. Lexeme** explanation requested.

---

## 1. Token vs. Lexeme

A common source of confusion. Both come out of the **scanner / lexer**, but they mean different things.

| Concept | What it is | Example for `:=` | Example for `42` | Example for `flag` |
|---------|------------|--------------------|--------------------|--------------------|
| **Lexeme** | The **raw text** taken from the source file — the actual characters the scanner consumed. | `:=` | `42` | `flag` |
| **Token** | A **classified unit**: a small object with `(type, lexeme, position)`. The `type` is a category from `TokenType` (an enum). | `Token(ASSIGN, ":=", line=…)` | `Token(NUMBER, "42", line=…)` | `Token(ID, "flag", line=…)` |

### How to remember it

- **Lexeme = string** (substring of source).
- **Token = lexeme + category + metadata** (line number here; you can later add column/span).
- Two different lexemes (`<` and `<=`) can be distinct tokens with **different categories** (`LT` vs. `LEQ`).
- Two tokens of the **same category** can carry **different lexemes** (every `Token(NUMBER, "1")`, `Token(NUMBER, "42")`, `Token(NUMBER, "100")` shares the same category `NUMBER`).
- For most **keywords/punctuation**, the lexeme is fixed (`+`, `and`, `print`); the token category still adds the meaning.
- For **identifiers/literals**, the lexeme is the actual variable name or digits, while the category is just `ID` or `NUMBER`.

### Where this lives in the code

```3:7:src/main/java/com/booleanrulelang/domain/Token.java
public record Token(
	TokenType type,
	String lexeme,
	int line
) {}
```

- `type` → **category** (the `TokenType` enum).
- `lexeme` → **the raw text** read from the source.
- `line` → metadata for error messages.

The scanner picks the category by **literal char match** (`+` → `PLUS`), by **lookahead** (`:` + `=` → `ASSIGN`), by **letter run + keyword table** (`and` → `AND`, `flag` → `ID`), or by **digit run** (`42` → `NUMBER`).

```24:31:src/main/java/com/booleanrulelang/scanner/Lexer.java
		keywords = new HashMap<>();
		keywords.put("and",   TokenType.AND);
		keywords.put("or",    TokenType.OR);
		keywords.put("not",   TokenType.NOT);
		keywords.put("true",  TokenType.TRUE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("print", TokenType.PRINT);
```

---

## 2. Top “TODO” block in `decision.md`

### (1) Precedence of `not` / `and` / `or` — ✅ implemented

The parser layers methods from **lowest** to **highest** precedence:

`parseLogicOr` → `parseLogicAnd` → `parseLogicNot` → `parseComparison` → `parseArithmetic` → `parseTerm` → `parseFactor` → `parsePrimary`.

So `or` binds loosest, `and` next, `not` tightest among the boolean operators.

### (2) Comparison separated from arithmetic — ✅ implemented

`parseComparison` consumes **one** comparison operator between two **arithmetic** subexpressions. Arithmetic is its own sub-grammar (`parseArithmetic` / `parseTerm`) and never produces comparison nodes. Conversely, comparison parsing **calls into** arithmetic but does not let arithmetic parsers chew `==` etc.

### (3) Boolean and arithmetic AST nodes are distinguishable — ✅ implemented in this pass

Previously the AST used a single `BinaryOpNode(op, left, right)` and `UnaryOpNode(op, operand)` and distinguished kinds only by the `op` string. The AST has now been **split** into separate classes per family:

| Family | Class | Operators |
|--------|-------|-----------|
| Arithmetic | `ArithmeticOpNode` | `+`, `-`, `*`, `/` |
| Comparison | `ComparisonOpNode` | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| Logical (binary) | `LogicalOpNode` | `and`, `or` |
| Logical (unary) | `NotNode` | `not` |
| Arithmetic (unary) | `NegationNode` | unary `-` |

Now type checking / interpretation use **`instanceof` per family** instead of string switching on `op`, and the visitor interface has dedicated methods (`visitArithmeticOp`, `visitComparisonOp`, `visitLogicalOp`, `visitNot`, `visitNegation`).

Old files `BinaryOpNode.java` and `UnaryOpNode.java` were **removed**.

---

## 3. Common checklist

### Scope is specific and manageable — ✅

Package layout keeps responsibilities focused:

- `scanner/` — `Lexer`, `TokenPrinter`
- `parser/` — `Parser`
- `domain/` — `Token`, `TokenType`, all AST node classes
- `visitor/` — `ASTVisitor`, `ASTJsonPrinter`, `ASTTraversalPrinter`
- `semantics/` — `TypeChecker`, `Interpreter`
- `runtime/` — `RuntimeValue`, `NumberValue`, `BooleanValue`
- `types/` — `Type`
- `exception/` — error hierarchy
- `compilerEngine/` — orchestrates the pipeline

### Token categories → `TokenType` enum — ✅

`com.booleanrulelang.domain.TokenType` enumerates **all categories**: keywords (`AND`, `OR`, `NOT`, `TRUE`, `FALSE`, `PRINT`), `ID`, `NUMBER`, arithmetic operators (`PLUS`, `MINUS`, `STAR`, `SLASH`), comparison operators (`GT`, `LT`, `EQ`, `NEQ`, `GEQ`, `LEQ`), `ASSIGN`, delimiters (`LPAREN`, `RPAREN`, `SEMICOLON`), and `EOF`.

### Token vs. lexeme — ✅

See section 1 above. The `Token` record carries both.

### Keywords, identifiers, literals, operators, delimiters handled correctly — ✅

- **Keywords**: identifier text is looked up in `keywords` map (case-insensitive thanks to `.toLowerCase()` in `readIdentifier`).
- **Identifiers**: letters only, mapped to `TokenType.ID` if not a keyword.
- **Literals**: digit sequences → `NUMBER`; `true` / `false` → `TRUE` / `FALSE`.
- **Operators**: single-char (`+ - * /`) and lookahead multi-char (`:= == != >= <=`).
- **Delimiters**: `(`, `)`, `;`.

### Scanner output is visible independently — ✅ added in this pass

`com.booleanrulelang.scanner.TokenPrinter` formats the lexer output as a numbered, aligned table. `CompilerEngine` now emits it before parsing:

```30:33:src/main/java/com/booleanrulelang/compilerEngine/CompilerEngine.java
		List<Token> tokens = lexer.scan(filePath);
		System.out.println("== Tokens (scanner output) ==");
		System.out.print(tokenPrinter.print(tokens));
		System.out.println();
```

Sample output for `print 2 + 3;`:

```text
== Tokens (scanner output) ==
 1  PRINT      'print'         line 1
 2  NUMBER     '2'             line 1
 3  PLUS       '+'             line 1
 4  NUMBER     '3'             line 1
 5  SEMICOLON  ';'             line 1
 6  EOF        <eof>           line 1
```

### Lexical errors are reported clearly — ✅

`UnrecognizedText` (extends `CompilerException`) reports the bad character and line:

```text
Error at [0:0]: Unrecognized character: '@' at line 3
```

Unreadable source files surface as `SourceFileException` (`Could not read file: …`).

### Grammar is suitable for recursive descent — ✅

The grammar is **LL(1)** with one-token lookahead (`peek()`). Each non-terminal becomes a method; the only special handling is `parseLogicNot` (right-recursive for `not not …`) which is the natural shape for a unary prefix operator.

### Parser logic reflects grammar non-terminals — ✅

Method names mirror the grammar:

```text
program        -> parseProgram
statement      -> parseStatement
print          -> parsePrint
assignment     -> parseAssignment
expression     -> parseExpression / parseLogicOr
logicAnd       -> parseLogicAnd
logicNot       -> parseLogicNot
comparison     -> parseComparison
arithmetic     -> parseArithmetic
term           -> parseTerm
factor         -> parseFactor
primary        -> parsePrimary
```

### Lookahead handling is clear — ✅

- **Scanner**: uses a `PushbackReader`. For each ambiguous prefix (`:` `!` `>` `<` `=`) it reads one more char and either commits to a two-char token or **unreads** to fall back. See lines 55–92 in `Lexer.java`.
- **Parser**: `peek()` returns the current token without consuming; `check(type)` and `expect(type, msg)` build on it. No backtracking is needed.

### Valid input parses, invalid is rejected — ✅

- Valid: every example under `examples/` lexes, parses, type-checks, and runs.
- Invalid: missing `;`, unknown char, undefined variable, mixed types → exception with a clear message.

### AST is clearly different from token output — ✅ both are now printed

After tokens, the engine emits the **AST traversal** (indented pre-order) in a clearly labelled section:

```35:37:src/main/java/com/booleanrulelang/compilerEngine/CompilerEngine.java
		System.out.println("== AST (pre-order traversal) ==");
		System.out.print(astTraversalPrinter.print(ast));
		System.out.println();
```

So a single run produces, in order: **Tokens**, **AST**, **Type check status**, **Execution**.

---

## 4. End-to-end demo

Source: `examples/demo-arithmetic.txt` →

```text
print 2 + 3 * 4;
y := -(1 + 2);
```

Resulting output:

```text
== Tokens (scanner output) ==
 1  PRINT      'print'         line 1
 2  NUMBER     '2'             line 1
 3  PLUS       '+'             line 1
 4  NUMBER     '3'             line 1
 5  STAR       '*'             line 1
 6  NUMBER     '4'             line 1
 7  SEMICOLON  ';'             line 1
 8  ID         'y'             line 2
 9  ASSIGN     ':='            line 2
10  MINUS      '-'             line 2
11  LPAREN     '('             line 2
12  NUMBER     '1'             line 2
13  PLUS       '+'             line 2
14  NUMBER     '2'             line 2
15  RPAREN     ')'             line 2
16  SEMICOLON  ';'             line 2
17  EOF        <eof>           line 2

== AST (pre-order traversal) ==
program
  print
    arithmetic +
      number 2
      arithmetic *
        number 3
        number 4
  assign y
    negate
      arithmetic +
        number 1
        number 2

== Type check: OK ==

== Execution ==
14
y := -3
```

For a type error (`docs/test.txt`, `x := true + 5;`):

```text
== Tokens (scanner output) ==
 1  ID         'x'             line 1
 2  ASSIGN     ':='            line 1
 3  TRUE       'true'          line 1
 4  PLUS       '+'             line 1
 5  NUMBER     '5'             line 1
 6  SEMICOLON  ';'             line 1
 7  EOF        <eof>           line 1

== AST (pre-order traversal) ==
program
  assign x
    arithmetic +
      bool true
      number 5

Type error: left operand of '+' must be NUMBER, was BOOLEAN
```

So the four artefacts (**tokens**, **AST**, **type-check result**, **execution**) are visible and clearly distinct.

---

## 5. What changed in this pass

| Area | Change |
|------|--------|
| `domain/` | Added `ArithmeticOpNode`, `ComparisonOpNode`, `LogicalOpNode`, `NotNode`, `NegationNode`. **Removed** `BinaryOpNode`, `UnaryOpNode`. |
| `visitor/` | `ASTVisitor` got `visitArithmeticOp`, `visitComparisonOp`, `visitLogicalOp`, `visitNot`, `visitNegation`. `ASTJsonPrinter` and `ASTTraversalPrinter` updated accordingly. |
| `parser/` | Builds the new node classes. Also resets `currentPosition` on each `parseProgram` call so the bean is safely reusable. |
| `semantics/` | `TypeChecker` and `Interpreter` use per-class `instanceof` instead of string-matching `op` on a generic binary/unary node. |
| `scanner/` | New `TokenPrinter` formats lexer output as an aligned table. |
| `compilerEngine/` | Pipeline now prints **Tokens**, **AST**, **Type check status**, **Execution** as separately labelled sections. |
| `docs/` | This file. |

Every checklist item in `decision.md` now has an implementation path.
