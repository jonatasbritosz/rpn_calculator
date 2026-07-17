package calculator.cli

import calculator.domain.BinaryOperation
import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry
import calculator.application.RpnProcessor
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartupHelpTest {

    private fun render(registry: OperationRegistry): String {
        val output = StringWriter()
        StartupHelp(registry).render(PrintWriter(output, true))
        return output.toString()
    }

    @Test
    fun `operator list is derived from the registry`() {
        val modulo = object : BinaryOperation {
            override val symbol = "%"
            override fun apply(left: Double, right: Double): Double = left % right
        }

        val help = render(OperationRegistry(listOf(modulo)))

        assertTrue("Operators: %" in help)
    }

    @Test
    fun `help lists the standard operators, quit command, and EOF`() {
        val help = render(OperationRegistry.standard())

        assertTrue("Operators: + - * /" in help)
        assertTrue("'q'" in help)
        assertTrue("end of input" in help)
    }

    @Test
    fun `help appears exactly once, before all results, without touching state`() {
        val stack = CalculatorStack()
        val output = StringWriter()
        ConsoleApplication(
            processor = RpnProcessor(OperationRegistry.standard(), stack),
            startupHelp = StartupHelp(OperationRegistry.standard()),
            reader = StringReader("5\nq\n").buffered(),
            writer = PrintWriter(output, true),
        ).run()

        val text = output.toString()
        assertTrue(text.startsWith("RPN Calculator"))
        assertEquals(1, Regex("RPN Calculator").findAll(text).count())
        assertTrue(text.indexOf("RPN Calculator") < text.indexOf("> 5"))
        assertEquals(listOf(5.0), stack.snapshot())
    }
}