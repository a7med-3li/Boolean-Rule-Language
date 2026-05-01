# Boolean-Rule-Language
This is a domain-specific language (DSL) designed to explore the nuances of precedence, associativity, and recursive descent parsing outside the typical "calculator" paradigm. While most parsers focus on arithmetic operations, this centers entirely on complex comparison expressions and deeply nested boolean logic.

## Project scaffold

This repository now includes a minimal Java application built with Maven.

### Run it

```bash
mvn test
mvn -q exec:java -Dexec.mainClass=com.booleanrulelanguage.Main
```

If you only want to compile and run manually:

```bash
mvn -q package
java -cp target/boolean-rule-language-1.0.0-SNAPSHOT.jar com.booleanrulelanguage.Main
```

