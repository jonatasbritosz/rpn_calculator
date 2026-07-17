package calculator

import calculator.application.RpnProcessor
import calculator.cli.ConsoleApplication
import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry
import java.io.PrintWriter

fun main() {
    val registry = OperationRegistry.standard()
    val processor = RpnProcessor(registry, CalculatorStack())
    val application = ConsoleApplication(
        processor = processor,
        reader = System.`in`.bufferedReader(),
        writer = PrintWriter(System.out, true),
    )
    application.run()
}
