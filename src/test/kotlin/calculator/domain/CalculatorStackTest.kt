package calculator.domain

import calculator.domain.operations.AddOperation
import calculator.domain.operations.DivideOperation
import calculator.domain.operations.SubtractOperation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CalculatorStackTest {

    private val stack = CalculatorStack()

    @Test
    fun `push grows the stack in order`() {
        stack.push(5.0)
        stack.push(8.0)

        assertEquals(listOf(5.0, 8.0), stack.snapshot())
    }

    @Test
    fun `apply consumes second-to-top as left and top as right`() {
        stack.push(9.0)
        stack.push(1.0)

        val result = stack.apply(SubtractOperation)

        assertEquals(8.0, result.getOrThrow())
    }

    @Test
    fun `successful operation replaces both operands with the result`() {
        stack.push(2.0)
        stack.push(9.0)
        stack.push(1.0)

        stack.apply(SubtractOperation)

        assertEquals(listOf(2.0, 8.0), stack.snapshot())
    }

    @Test
    fun `apply with fewer than two operands fails and keeps the stack unchanged`() {
        stack.push(5.0)

        val result = stack.apply(SubtractOperation)

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(listOf(5.0), stack.snapshot())
    }

    @Test
    fun `failed operation keeps both operands on the stack`() {
        stack.push(5.0)
        stack.push(0.0)

        val result = stack.apply(DivideOperation)

        assertTrue(result.isFailure)
        assertIs<ArithmeticException>(result.exceptionOrNull())
        assertEquals(listOf(5.0, 0.0), stack.snapshot())
    }

    @Test
    fun `non-finite result is rejected and keeps both operands on the stack`() {
        stack.push(Double.MAX_VALUE)
        stack.push(Double.MAX_VALUE)

        val result = stack.apply(AddOperation)

        assertTrue(result.isFailure)
        assertEquals("Result of '+' is not finite", result.exceptionOrNull()?.message)
        assertEquals(listOf(Double.MAX_VALUE, Double.MAX_VALUE), stack.snapshot())
    }
}