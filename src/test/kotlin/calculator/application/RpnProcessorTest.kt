package calculator.application

import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class RpnProcessorTest {

    private val stack = CalculatorStack()
    private val processor = RpnProcessor(OperationRegistry.standard(), stack)

    private fun outputs(vararg tokens: String): List<String> =
        tokens.map { token ->
            when (val result = processor.process(token)) {
                is ProcessingResult.Output -> result.text
                is ProcessingResult.Error -> "error: ${result.text}"
                ProcessingResult.ContinueSilently -> "<silent>"
                ProcessingResult.Exit -> "<exit>"
            }
        }

    @Test
    fun `numbers are pushed and echoed as Double values`() {
        assertEquals(listOf("5.0", "8.0", "13.0"), outputs("5", "8", "+"))
    }

    @Test
    fun `challenge sequence with chained operators`() {
        assertEquals(
            listOf("5.0", "5.0", "5.0", "8.0", "13.0", "18.0", "-13.0"),
            outputs("5", "5", "5", "8", "+", "+", "-"),
        )
        assertEquals(listOf("13.0", "0.0"), outputs("13", "+"))
    }

    @Test
    fun `negative numbers are parsed as values, not operators`() {
        assertEquals(listOf("-3.0", "-2.0", "6.0", "5.0", "11.0"), outputs("-3", "-2", "*", "5", "+"))
    }

    @Test
    fun `division produces fractional results`() {
        assertEquals(listOf("5.0", "9.0", "1.0", "8.0", "0.625"), outputs("5", "9", "1", "-", "/"))
    }

    @Test
    fun `decimal input keeps its fractional formatting`() {
        assertEquals(listOf("2.5", "0.5", "3.0"), outputs("2.5", "0.5", "+"))
    }

    @Test
    fun `q exits immediately`() {
        assertEquals(ProcessingResult.Exit, processor.process("q"))
    }

    @Test
    fun `blank token continues silently`() {
        assertEquals(ProcessingResult.ContinueSilently, processor.process(" "))
    }

    @Test
    fun `unrecognized token reports an error and preserves the stack`() {
        processor.process("5")

        val result = processor.process("abc")

        assertEquals(ProcessingResult.Error("Unrecognized token: abc"), result)
        assertEquals(listOf(5.0), stack.snapshot())
    }

    @Test
    fun `non-finite number reports an error and preserves the stack`() {
        val result = processor.process("Infinity")

        assertEquals(ProcessingResult.Error("Number is not finite: Infinity"), result)
        assertEquals(emptyList(), stack.snapshot())
    }

    @Test
    fun `insufficient operands report an error and preserve the stack`() {
        processor.process("5")

        val result = processor.process("+")

        assertEquals(
            ProcessingResult.Error("Operator '+' requires 2 operands, but the stack has 1"),
            result,
        )
        assertEquals(listOf(5.0), stack.snapshot())
    }

    @Test
    fun `division by zero reports an error and preserves the stack`() {
        processor.process("5")
        processor.process("0")

        val result = processor.process("/")

        assertEquals(ProcessingResult.Error("Division by zero"), result)
        assertEquals(listOf(5.0, 0.0), stack.snapshot())
    }

    @Test
    fun `processing continues normally after an error`() {
        processor.process("5")
        processor.process("+")

        assertEquals(listOf("8.0", "13.0"), outputs("8", "+"))
    }
}