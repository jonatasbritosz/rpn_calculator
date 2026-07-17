# RPN Calculator

A command-line Reverse Polish Notation (RPN) calculator written in Kotlin, built for people comfortable with UNIX-like CLI utilities.

## High-Level Description

The calculator reads whitespace-delimited tokens from standard input, one or many per line, and writes results to standard output. Numbers are pushed onto a stack; an operator pops the top two values, computes `second-to-top <operator> top`, and pushes the result back. Every error is recoverable: the stack is never mutated by a failed operation, so the user can simply continue.

```
RPN Calculator
Enter numbers and operators in Reverse Polish Notation (e.g. 5 8 +).
Numbers: decimals and negatives are supported (5, -3, 2.5).
Operators: + - * /
Quit: enter 'q' or press Ctrl+D (end of input).

> 5
5.0
> 8
8.0
> +
13.0
> 9 1 - /
1.625
> q
```

### Supported behavior

- The four standard operators: `+`, `-`, `*`, `/`.
- Decimal and negative numbers (`2.5`, `-3`).
- Multiple tokens per line, processed left to right; the line echoes only its final result (`5 5 5 8 + + -` prints just `-13.0`), while errors print as they occur.
- Stack state persists across lines.
- Graceful error recovery: unknown tokens, non-finite numbers, insufficient operands, division by zero, and non-finite results (overflow) print a clear message and leave the stack unchanged.
- `q` exits immediately (later tokens on the same line are ignored); EOF (Ctrl+D) exits without an error.
- Startup help is printed exactly once, before the first prompt, and derives its operator list from the operation registry.

## How to Run

Requires a JDK; the Gradle toolchain resolver downloads JDK 25 automatically if needed.

```bash
./gradlew run -q --console=plain     # interactive session
./gradlew test                       # test suite
```

Piped input works too:

```bash
printf '5 5 5 8 + + -\n13 +\nq\n' | ./gradlew run -q --console=plain
```

## Architecture

```
stdin ──► ConsoleApplication ──► RpnProcessor ──► OperationRegistry ──► BinaryOperation
              │      ▲                │
              ▼      └── results ── CalculatorStack
            stdout
```

- **`domain`** — pure calculator core, no I/O:
  - `BinaryOperation`: stateless contract (`symbol`, `apply(left, right)`), implemented by `Add`, `Subtract`, `Multiply`, and `Divide` operations. Division rejects a zero divisor before calculating.
  - `CalculatorStack`: sole owner of mutable state. Applies operations atomically — validation happens before mutation, so failures preserve the previous stack. Every operation result is re-validated with `isFinite()`, so overflow to `Infinity` or `NaN` is rejected instead of entering the stack.
  - `OperationRegistry`: immutable symbol-to-operation map and the single source of operator symbols (the startup help reads from it; nothing else declares operators).
- **`application`** — `RpnProcessor` turns one token into one explicit `ProcessingResult` (`Output`, `Error`, `ContinueSilently`, `Exit`), a sealed model so adapters never infer business behavior from strings.
- **`cli`** — `ConsoleApplication` (stream-based adapter over `BufferedReader`/`PrintWriter`) and `StartupHelp`.
- **`Main.kt`** — manual composition of the pieces over `System.in`/`System.out`.

## Technical Choices and Reasoning

- **I/O-independent core.** The processor works on tokens and returns sealed results; it never touches `System.in`/`System.out`. A future WebSocket, file, or TCP interface reuses `RpnProcessor` unchanged and only adds a new adapter next to `ConsoleApplication`.
- **Registry-backed operations.** Adding an operator means implementing `BinaryOperation` and registering it — the parser, help text, and error handling need no changes (open/closed principle, DRY for operator symbols).
- **Explicit sealed results.** `when` over `ProcessingResult` is compiler-checked, so a new outcome type cannot be silently ignored by an adapter.
- **Atomic stack mutation.** Operands are read, the operation runs, and only success mutates the stack. This one invariant guarantees graceful recovery everywhere.
- **`Double` values with finite-number validation.** Parsing uses `toDoubleOrNull()` plus `isFinite()`, accepting decimals and negatives while rejecting `NaN`/`Infinity` input.
- **Output formatting.** Values are `Double`s end to end, and results are printed as `Double` values (`13.0`, `-13.0`, `0.625`) using Kotlin's `Double.toString()`. The challenge examples mix `13` and `13.0` styles; the `Double` representation was chosen as the single consistent rule so the printed value is exactly the stack value.
- **Manual composition, no DI framework.** For nine small classes, a framework would be pure overhead (KISS/YAGNI); the composition root in `Main.kt` keeps every extension point visible.
- **Stream-based tests.** `ConsoleApplication` is tested with in-memory reader/writer streams, not hard-coded system streams, so the full console flow is verified without spawning processes.

## Trade-Offs and What I'd Do Differently

- **`Double` precision.** Binary floating point has the usual artifacts (e.g. `0.1 0.2 +` → `0.30000000000000004`). `BigDecimal` would fix this at the cost of division semantics (scale/rounding policy) and noisier code; for a four-operator calculator, `Double` keeps behavior obvious. With more time I would switch to `BigDecimal` with an explicit rounding policy.
- **No expression validation across lines.** The calculator is deliberately a stack machine, not an expression parser; it does not warn about leftover operands at exit.
- **Single-threaded, in-memory state.** Fine for a CLI; a hosted interface would need per-session stacks.
- **Possible next steps:** a `stack`/`clear`/`undo` command set, more operators (`%`, `^`, `sqrt`), and a second adapter (TCP or WebSocket) to prove the port/adapter seam.

## Hosted Application

Not applicable — this is a local CLI tool by design; no hosted instance is provided.
