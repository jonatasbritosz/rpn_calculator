package calculator.domain

/**
 * Owner of the calculator's mutable stack state.
 *
 * Operations are applied atomically: the stack is only mutated after the
 * operation succeeds, so any failure leaves the previous state intact.
 */
class CalculatorStack {

    private val values = ArrayDeque<Double>()

    val size: Int get() = values.size

    fun snapshot(): List<Double> = values.toList()

    fun push(value: Double) {
        values.addLast(value)
    }

    fun apply(operation: BinaryOperation): Result<Double> {
        if (values.size < 2) {
            return Result.failure(
                IllegalStateException(
                    "Operator '${operation.symbol}' requires 2 operands, but the stack has ${values.size}",
                ),
            )
        }
        val right = values[values.size - 1]
        val left = values[values.size - 2]
        return runCatching {
            val result = operation.apply(left, right)
            if (!result.isFinite()) {
                throw ArithmeticException("Result of '${operation.symbol}' is not finite")
            }
            result
        }.onSuccess { result ->
            values.removeLast()
            values.removeLast()
            values.addLast(result)
        }
    }
}