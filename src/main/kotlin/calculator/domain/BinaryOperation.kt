package calculator.domain

/**
 * Contract for a binary arithmetic operation applied to two stack operands.
 *
 * Implementations must be stateless. In RPN order, `apply` receives the
 * second-to-top stack value as `left` and the top value as `right`.
 */
interface BinaryOperation {
    val symbol: String

    fun apply(left: Double, right: Double): Double
}