# `ASTTraversalPrinter` — Design, traversal types, and line-by-line guide

This document explains **why** `ASTTraversalPrinter` is structured the way it is, compares **different tree traversals**, and walks through **every line** of `ASTTraversalPrinter.java`.

---

## 1. What this class does at a glance

- It **does not evaluate** expressions and **does not** print stored JSON representing the nested AST object graph.
- It performs a **depth-first pre-order traversal** of the AST: at each subtree it **records (prints) the current node first**, then recursively visits subtrees left-to-right (or statement order).
- Output is plain text lines with **leading spaces** proportional to nesting **depth**.

That matches “show the **traverse** order,” not “dump the AST structure as data.”

---

## 2. Main ideas in the implementation

### 2.1 Visitor pattern (`Node.accept` / `ASTVisitor`)

- **`Node`** is abstract; each subclass (`ProgramNode`, `AssignNode`, …) implements `accept(ASTVisitor<T> visitor)` by calling exactly one method on the visitor (e.g. `visitor.visitAssign(this)`).
- **`ASTVisitor<T>`** declares one `visit…` method per concrete node type. The **compiler** guarantees type-specific dispatch without a big `instanceof` chain in `ASTTraversalPrinter`’s public API.
- Returning **`Void`** means “this visitor is used only for effects (appending lines).” Using `Void` with `return null` is a standard Java workaround when you do not care about the return type.

This design separates **structure** (the AST nodes) from **operations** (print JSON, print traversal, future interpreter, etc.). Adding a post-order printer is another visitor, not edits to ten node classes.

### 2.2 Why two classes (`ASTTraversalPrinter` + `PreOrderWalker`)?

1. **`ASTTraversalPrinter`** is the Spring **`@Component`**: singleton-friendly. It exposes **`print(Node)`** that creates fresh state per invocation.
2. **`PreOrderWalker`** is **`private static final`**, created **inside** `print`. It owns the **`StringBuilder`**, **`depth`**, and the walking logic.

**Why:** A Spring singleton must not stash a shared `StringBuilder` or traversal `depth` in fields across calls (`print` called twice concurrently or in succession would corrupt output). Creating a **`new PreOrderWalker(sb)` per `print`** gives **fresh `depth`** and ties the walker to **one buffer** per run.

### 2.3 Pre-order (“visit parent before children”)

For composite nodes (`program`, `assign`, `binary`, …) the template is always:

```text
appendLine description of THIS node;
depth++;
recurse children;
depth--;
```

So the **parent appears on an earlier line** than its descendants — that defines **pre-order** for both unary/binary n-ary structures.

### 2.4 Indentation and `depth`

- **`indent()`** repeats two spaces **`depth`** times so child lines visually sit under parents.
- **`depth`** is incremented **after** emitting the node line so that **children** get one more indentation level than **this** node.
- **`depth--`** after the loop/recursion restores the level for siblings higher up — classic stack discipline without an explicit stack (the **call stack** holds the rest).

Leaves (`number`, `identifier`, `bool`) have **no** children → no depth change beyond what’s already implicit (they emit at current depth only).

---

## 3. Tree traversals compared

These terms come from classic **trees** with a root and children.

### 3.1 Pre-order (NLR — Node, Left, Right for strict binary formulation)

For a node **N** and subtrees treated in order:

- **Emit / process `N`** first  
- Then visit each subtree in a fixed order (here: left then right for binary; list order for `program`)

**Characteristics:** Parents appear **before** all descendants in the listing. Useful for copying tree shape prefixes, emitting prefix-like structures, or when you need parent context printed first.

**This project:** `ASTTraversalPrinter` uses **depth-first pre-order** extended to arbitrary arity (many statements under `program`, two operands under binary op).

---

### 3.2 Post-order (LRN — Left, Right, Node)

Pattern:

- Visit **all subtrees** first  
- **Then emit / process the parent**

**Characteristics:** Leaves and inner descendants appear **before** their ancestor. Typical for evaluations where you need operands **before** the operator (`3`, `5`, **`+`**), stack machines, destroying trees safely (children first).

**Compared to here:** Same tree, different line order — e.g. `assign` might print **after** its expression subtree finishes.

---

### 3.3 In-order (LNR — only standard for binary trees)

- Left subtree → **emit node** (often for unary “content-like” nodes only) → right subtree  

**Characteristics:** Famous for BST sorted output; awkward for heterogeneous AST kinds—what is “left” vs “right” for `ProgramNode`?

**Compared to here:** Not used in `ASTTraversalPrinter`; ASTs rarely use symmetric in-order for whole programs.

---

### 3.4 Level-order (breadth-first, BFS)

Use a queue: visit roots of distance 1, then 2, … All nodes at depth *d* before depth *d+1*.

**Characteristics:** Mirrors “layer by layer” reading; unrelated to parentheses structure the way DFS is.

**Compared to here:** Output order would reorder nodes relative to DFS; different diagnostic view.

---

### 3.5 Summary table

| Traversal   | Typical order mnemonic | Parent vs children timing | Classic use |
|------------|-------------------------|---------------------------|-------------|
| Pre-order  | Node first              | Parent **before** children | Prefix-ish walks, summaries “from root down” |
| Post-order | Node last               | Parent **after** children | Evaluation, teardown, postfix-like emission |
| In-order   | Middle (binary)         | Depends on BST meaning     | BST sorted traversal |
| Level-order | By layers              | Breadth-wise              | Zig-zag puzzles, widest layer first visually |

Same AST, multiple visitors → multiple traversals — that is the payoff of **`ASTVisitor`** per operation.

---

## 4. Line-by-line explanation: `ASTTraversalPrinter.java`

The line numbers below match the source file shipped in this repository.

---

### Lines 1–12

```java
package com.booleanrulelang.visitor;
```
**L1.** Declares the Java package so the class resolves under `com.booleanrulelang.visitor`.

```java
import com.booleanrulelang.domain.AssignNode;
import com.booleanrulelang.domain.BinaryOpNode;
…
```
**L3–11.** Imports every concrete **`Node`** subclass this visitor mentions by type (`visitAssign`, …), plus the abstract **`Node`** type for **`print`**’s parameter.

```java
import org.springframework.stereotype.Component;
```
**L12.** Import for **`@Component`** registration in Spring’s IoC container.

---

### Lines 14–24

```java
/**
 * Prints a depth-first pre-order traversal of the AST — the visit sequence and node kinds,
 * not a serialized tree dump (no nested JSON AST).
 */
```
**L14–17.** Documentation for maintainers describing behavior (DFS pre-order, text lines, contrast with JSON AST dump).

```java
@Component
```
**L18.** Marks **`ASTTraversalPrinter`** as a Spring bean so **`CompilerEngine`** can inject it with constructor injection alongside **`Lexer`** and **`Parser`**.

```java
public class ASTTraversalPrinter {
```
**L19.** Public class — the façade used by **`CompilerEngine`** to render traversal text.

```java
public String print(Node node) {
```
**L21.** Entry point accepting **any** AST **`Node`** (usually the root **`ProgramNode`** after parsing).

```java
StringBuilder sb = new StringBuilder();
```
**L22.** Efficient mutable **character buffer**; avoids quadratic string concatenation in a loop.

```java
node.accept(new PreOrderWalker(sb));
```
**L23.** **Double dispatch:** `node` selects the concrete `accept` overload, which invokes the matching **`visit…`** method on **`PreOrderWalker`**; the walker appends lines to **`sb`**.

```java
return sb.toString();
```
**L24.** Returns immutable **`String`** for printing or logging.

---

### Lines 26–43

```java
private static final class PreOrderWalker implements ASTVisitor<Void> {
```
**L27.** **Nested helper** class implementing **`ASTVisitor<Void>`**:

- **`static`** — tied to **`ASTTraversalPrinter`**, needs no enclosing instance.
- **`private`** — hide implementation detail inside **`ASTTraversalPrinter`**.
- **`final`** — not subclassed elsewhere.
- **`ASTVisitor<Void>`** — visits return nothing meaningful; **`Void`** implies **`null`**.

```java
private final StringBuilder sb;
```
**L29.** Reference to shared buffer (created in **`print`**) appended by every visit method — **`final`** forbids swapping to another **`StringBuilder`**.

```java
private int depth;
```
**L30.** Current nesting level for indentation; resets implicitly per walker instance.

```java
PreOrderWalker(StringBuilder sb) {
	this.sb = sb;
```
**L32–34.** Constructor assigns buffer; **`depth`** starts at **`0`** (default for **`int`**).

```java
private void indent() {
	sb.append("  ".repeat(depth));
}
```
**L36–37.** Prepends **`depth`** times two spaces (**`repeat`** avoids manual loops).

```java
private void appendLine(String line) {
	indent();
	sb.append(line).append(System.lineSeparator());
}
```
**L40–43.** Applies indentation, appends textual label for node, **`System.lineSeparator()`** inserts OS-correct newline (CRLF vs LF).

---

### Lines 45–53 — `program`

```java
@Override
public Void visitProgram(ProgramNode node) {
```
**L45–46.** Overrides interface contract; **`ProgramNode`** is **list of statements**.

```java
appendLine("program");
```
**L47.** Prints this node (“root program”) **before** any statement — parent-first → **pre-order**.

```java
depth++;
```
**L48.** Children are visually **one tab level deeper**.

```java
for (Node stmt : node.statements) {
	stmt.accept(this);
```
**L49–51.** Sequential statement order preserves **DFS pre-order**: each subtree fully traversed left-to-right (**order of list** equals source order).

```java
depth--;
```
**L52.** Undo depth before returning — siblings at outer level unaffected.

```java
return null;
```
**L53.** Satisfies **`ASTVisitor<Void>`** return requirement.

---

### Lines 56–62 — assignment

```java
@Override
public Void visitAssign(AssignNode node) {
```
**L56–57.** Visiting assignment node (**`identifier := expr`**).

```java
appendLine("assign " + node.name);
```
**L58.** Emits **`assign`** plus **variable name** before walking **value subtree** (**pre-order**).

```java
depth++;
	node.value.accept(this);
depth--;
```
**L59–61.** Indent deeper, traverse expression once, unwind depth.

---

### Lines 65–71 — print

Same pattern:

```java
appendLine("print");
depth++;
node.expression.accept(this);
depth--;
```

**Purpose:** Prints **`print`**, then traversal of expression child.

---

### Lines 73–82 — binary operator

```java
@Override
public Void visitBinaryOp(BinaryOpNode node) {
	appendLine("binary " + node.op);
	depth++;
	node.left.accept(this);
	node.right.accept(this);
	depth--;
	return null;
}
```

**Interpretation:**

- **`node.op`** is lexeme-derived string (**`"+"`**, **`"and"`**, **`"==`**, relational operators — whatever the parser stored in **`BinaryOpNode`**).
- **Left subtree** fully traversed (still pre-order internally) **before** **right subtree** DFS branch — deterministic **consistent pre-order DFS** labeling.

---

### Lines 83–92 — unary operator

Analogous binary pattern but single operand (**unary `-`**, **`not`**, …).

---

### Lines 93–103 — numeric literal

```java
@Override
public Void visitNumber(NumberNode node) {
	double v = node.getValue();
```
**L94–96.** Getter returns **`double`** (parser uses **`Double.parseDouble`** semantics).

```java
if (v == Math.floor(v)) {
	appendLine("number " + (int) v);
} else {
	appendLine("number " + v);
}
```
**L96–101.** Cosmetic: integral doubles print without **`.0`**, fractions print **`double`** string.

```java
return null;
```
**L101.** Leaf node — nothing to recurse → no depth juggling.

---

### Lines 104–114 — identifiers and booleans

```java
@Override
public Void visitIdentifier(IdentifierNode node) {
	appendLine("identifier " + node.getName());
	return null;
}
```
**L104–108.** Leaf: emit variable name; no children or depth change.

```java
@Override
public Void visitBool(BoolNode node) {
	appendLine("bool " + node.isValue());
	return null;
}
```
**L110–114.** Leaf: emit **`true`** / **`false`**; **`isValue()`** comes from **Lombok `@Getter`** on **`boolean value`**.

---

### Lines 115–117

Closing braces **`}`**: end **`PreOrderWalker`**, end **`ASTTraversalPrinter`**.

---

## 5. Putting it together: example mapping

Given source such as **`x := true + 5;`** conceptual AST:

```
Program
└─ Assign(name=x)
   └─ BinaryOp(+)
       ├─ Bool(true)
       └─ Number(5)
```

Likely traversal output (**pre-order**):

```text
program
  assign x
    binary +
      bool true
      number 5
```

A **post-order** walker would postpone **`binary +`** until after **`bool`** and **`number`**, yielding a different textual order reflecting “children before parent.”

---

## 6. Closing note

Implementing traversal as **`ASTVisitor`** keeps **printing policy** orthogonal to **`Parser`** and **`Lexer`**. **Pre-order DFS** suits “describe the spine of the hierarchy first”; **Post-order DFS** suits “ operands before operator.” Choose or add classes per need without touching node definitions.

---

**File reference:** [`src/main/java/com/booleanrulelang/visitor/ASTTraversalPrinter.java`](../src/main/java/com/booleanrulelang/visitor/ASTTraversalPrinter.java)
