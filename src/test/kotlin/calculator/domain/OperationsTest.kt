package calculator.domain

import calculator.domain.operations.AddOperation
import calculator.domain.operations.DivideOperation
import calculator.domain.operations.MultiplyOperation
import calculator.domain.operations.SubtractOperation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OperationsTest {

    @Test
    fun `addition sums positive, negative, and fractional values`() {
        assertEquals(13.0, AddOperation.apply(5.0, 8.0))
        assertEquals(-5.0, AddOperation.apply(-3.0, -2.0))
        assertEquals(3.0, AddOperation.apply(0.5, 2.5))
    }

    @Test
    fun `subtraction is not commutative and honors operand order`() {
        assertEquals(8.0, SubtractOperation.apply(9.0, 1.0))
        assertEquals(-8.0, SubtractOperation.apply(1.0, 9.0))
        assertEquals(-1.0, SubtractOperation.apply(-3.0, -2.0))
    }

    @Test
    fun `multiplication handles negative and fractional values`() {
        assertEquals(6.0, MultiplyOperation.apply(-3.0, -2.0))
        assertEquals(-6.0, MultiplyOperation.apply(3.0, -2.0))
        assertEquals(1.25, MultiplyOperation.apply(0.5, 2.5))
    }

    @Test
    fun `division honors operand order and produces fractional results`() {
        assertEquals(0.625, DivideOperation.apply(5.0, 8.0))
        assertEquals(-2.5, DivideOperation.apply(5.0, -2.0))
    }

    @Test
    fun `division by zero is rejected before calculation`() {
        val error = assertFailsWith<ArithmeticException> { DivideOperation.apply(5.0, 0.0) }
        assertEquals("Division by zero", error.message)
    }

    @Test
    fun `operations expose their symbols`() {
        assertEquals("+", AddOperation.symbol)
        assertEquals("-", SubtractOperation.symbol)
        assertEquals("*", MultiplyOperation.symbol)
        assertEquals("/", DivideOperation.symbol)
    }
}