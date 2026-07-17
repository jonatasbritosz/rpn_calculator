package calculator.cli

import calculator.application.RpnProcessor
import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleApplicationTest {

    private fun runWithInput(input: String): String {
        val output = StringWriter()
        val application = ConsoleApplication(
            processor = RpnProcessor(OperationRegistry.standard(), CalculatorStack()),
            reader = StringReader(input).buffered(),
            writer = PrintWriter(output, true),
        )
        application.run()
        return output.toString()
    }

    private fun resultLines(input: String): List<String> =
        runWithInput(input)
            .split("\n")
            .flatMap { it.split("> ") }
            .filter { it.isNotBlank() }

    @Test
    fun `processes one token per line`() {
        assertEquals(listOf("5.0", "8.0", "13.0"), resultLines("5\n8\n+\nq\n"))
    }

    @Test
    fun `processes multiple tokens per line from left to right`() {
        assertEquals(
            listOf("5.0", "5.0", "5.0", "8.0", "13.0", "18.0", "-13.0"),
            resultLines("5 5 5 8 + + -\nq\n"),
        )
    }

    @Test
    fun `stack state persists across lines`() {
        assertEquals(listOf("5.0", "9.0", "1.0", "8.0", "0.625"), resultLines("5\n9\n1\n-\n/\nq\n"))
    }

    @Test
    fun `errors are reported and processing continues`() {
        assertEquals(
            listOf("5.0", "Unrecognized token: abc", "8.0", "13.0"),
            resultLines("5\nabc\n8 +\nq\n"),
        )
    }

    @Test
    fun `q exits immediately and ignores later tokens on the same line`() {
        assertEquals(listOf("5.0"), resultLines("5\nq 8 +\n"))
    }

    @Test
    fun `end of input terminates without an error`() {
        assertEquals(listOf("5.0", "8.0"), resultLines("5\n8\n"))
    }

    @Test
    fun `blank lines produce no output`() {
        assertEquals(listOf("5.0"), resultLines("\n   \n5\nq\n"))
    }
}
