package calculator.application

import calculator.cli.ConsoleApplication
import calculator.cli.StartupHelp
import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Demonstration of edge-case behavior at the boundaries of Double arithmetic
 * and token parsing. These tests make the current behavior explicit and
 * protect it against accidental regression; some cases document known
 * limitations of binary floating point rather than desired ideals.
 */
class EdgeCasesDemonstrationTest {

    private val stack = CalculatorStack()
    private val processor = RpnProcessor(OperationRegistry.standard(), stack)

    private fun lastResult(vararg tokens: String): ProcessingResult =
        tokens.map(processor::process).last()

    private fun lastOutput(vararg tokens: String): String {
        val result = lastResult(*tokens)
        assertIs<ProcessingResult.Output>(result)
        return result.text
    }

    private fun lastError(vararg tokens: String): String {
        val result = lastResult(*tokens)
        assertIs<ProcessingResult.Error>(result)
        return result.text
    }

    // --- Binary floating-point precision ---

    @Test
    fun `classic Double artifact - 0,1 plus 0,2 is not exactly 0,3`() {
        assertEquals("0.30000000000000004", lastOutput("0.1", "0.2", "+"))
    }

    @Test
    fun `repeating decimals are truncated to Double precision`() {
        assertEquals("0.3333333333333333", lastOutput("1", "3", "/"))
    }

    @Test
    fun `integers above 2 pow 53 lose precision - adding 1 changes nothing`() {
        val bigInteger = "9007199254740992" // 2^53
        assertEquals(lastOutput(bigInteger), lastOutput("0", "+", "1", "+"))
    }

    // --- Overflow and underflow ---

    @Test
    fun `overflow to Infinity is rejected and both operands stay on the stack`() {
        val maxValue = Double.MAX_VALUE.toString()
        assertEquals("Result of '+' is not finite", lastError(maxValue, maxValue, "+"))
        assertEquals(listOf(Double.MAX_VALUE, Double.MAX_VALUE), stack.snapshot())
    }

    @Test
    fun `underflow silently collapses to zero`() {
        assertEquals("0.0", lastOutput("1e-300", "1e-300", "*"))
    }

    // --- Negative zero ---

    @Test
    fun `negative zero is a valid Double input and prints as negative zero`() {
        assertEquals("-0.0", lastOutput("-0.0"))
    }

    @Test
    fun `negative zero divisor is still division by zero`() {
        assertEquals("Division by zero", lastError("1", "-0.0", "/"))
        assertEquals(listOf(1.0, -0.0), stack.snapshot())
    }

    // --- Token parsing quirks ---

    @Test
    fun `scientific notation is accepted as input`() {
        assertEquals("1000.0", lastOutput("1e3"))
        assertEquals("-0.0025", lastOutput("-2.5e-3"))
    }

    @Test
    fun `leading plus sign makes a number, bare plus is the operator`() {
        assertEquals("5.0", lastOutput("+5"))
        assertEquals("10.0", lastOutput("+5", "+"))
    }

    @Test
    fun `bare or trailing decimal point still parses`() {
        assertEquals("0.5", lastOutput(".5"))
        assertEquals("5.0", lastOutput("5."))
    }

    @Test
    fun `Java-style type suffixes are accepted by Double parsing`() {
        // Double.parseDouble tolerates trailing 'd' and 'f'.
        assertEquals("5.0", lastOutput("5d"))
        assertEquals("2.5", lastOutput("2.5f"))
    }

    @Test
    fun `hexadecimal floating point literals are accepted by Double parsing`() {
        // 0x1.8p1 = 1.5 * 2^1
        assertEquals("3.0", lastOutput("0x1.8p1"))
    }

    @Test
    fun `comma is not a decimal separator - parsing is locale-independent`() {
        assertEquals("Unrecognized token: 3,14", lastError("3,14"))
    }

    @Test
    fun `quit command is case-sensitive`() {
        assertEquals("Unrecognized token: Q", lastError("Q"))
    }

    @Test
    fun `named non-finite tokens are rejected in both signs`() {
        assertEquals("Number is not finite: Infinity", lastError("Infinity"))
        assertEquals("Number is not finite: -Infinity", lastError("-Infinity"))
        assertEquals("Number is not finite: NaN", lastError("NaN"))
        assertEquals(emptyList(), stack.snapshot())
    }

    @Test
    fun `lone minus is the operator, not a negative number`() {
        assertEquals("Operator '-' requires 2 operands, but the stack has 0", lastError("-"))
    }

    // --- Console line behavior ---

    @Test
    fun `tabs and repeated spaces are valid separators and only the line result prints`() {
        val output = StringWriter()
        ConsoleApplication(
            processor = RpnProcessor(OperationRegistry.standard(), CalculatorStack()),
            startupHelp = StartupHelp(OperationRegistry.standard()),
            reader = StringReader("5\t\t8   +\nq\n").buffered(),
            writer = PrintWriter(output, true),
        ).run()

        val results = output.toString()
            .substringAfter("\n\n")
            .split("\n")
            .flatMap { it.split("> ") }
            .filter { it.isNotBlank() }
        assertEquals(listOf("13.0"), results)
    }
}
