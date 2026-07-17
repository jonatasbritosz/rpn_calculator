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
            startupHelp = StartupHelp(OperationRegistry.standard())
        )
        application.run()
        return output.toString()
    }

    private fun resultLines(input: String): List<String> =
        runWithInput(input)
            .substringAfter("\n\n") // skip the startup help block
            .split("\n")
            .flatMap { it.split("> ") }
            .filter { it.isNotBlank() }

    @Test
    fun `processes one token per line`() {
        assertEquals(listOf("5.0", "8.0", "13.0"), resultLines("5\n8\n+\nq\n"))
    }

    @Test
    fun `multi-token line prints only its final result`() {
        assertEquals(listOf("-13.0"), resultLines("5 5 5 8 + + -\nq\n"))
        assertEquals(
            listOf("-13.0", "0.0"),
            resultLines("5 5 5 8 + + -\n13 +\nq\n"),
        )
    }

    @Test
    fun `stack state persists across lines`() {
        assertEquals(listOf("5.0", "9.0", "1.0", "8.0", "0.625"), resultLines("5\n9\n1\n-\n/\nq\n"))
    }

    @Test
    fun `errors are reported and processing continues`() {
        assertEquals(
            listOf("5.0", "Unrecognized token: abc", "13.0"),
            resultLines("5\nabc\n8 +\nq\n"),
        )
    }

    @Test
    fun `mid-line error prints the error and still echoes the line result`() {
        assertEquals(
            listOf("Unrecognized token: bogus", "13.0"),
            resultLines("5 8 + bogus\nq\n"),
        )
    }

    @Test
    fun `line with only errors prints no result`() {
        assertEquals(
            listOf("Unrecognized token: abc", "Operator '+' requires 2 operands, but the stack has 0"),
            resultLines("abc +\nq\n"),
        )
    }

    @Test
    fun `q after tokens on the same line still prints the pending result`() {
        assertEquals(listOf("13.0"), resultLines("5 8 + q\n"))
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
