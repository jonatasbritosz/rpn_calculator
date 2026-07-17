package calculator.cli

import calculator.domain.OperationRegistry
import java.io.PrintWriter

/**
 * Renders the one-time startup help. The operator list is derived from
 * [OperationRegistry], the single source of operator symbols.
 */
class StartupHelp(private val registry: OperationRegistry) {

    fun render(writer: PrintWriter) {
        writer.println("RPN Calculator")
        writer.println("Enter numbers and operators in Reverse Polish Notation (e.g. 5 8 +).")
        writer.println("Numbers: decimals and negatives are supported (5, -3, 2.5).")
        writer.println("Operators: ${registry.symbols.joinToString(" ")}")
        writer.println("Quit: enter 'q' or press Ctrl+D (end of input).")
        writer.println()
    }
}