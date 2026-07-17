package calculator.domain.operations

import calculator.domain.BinaryOperation

object DivideOperation : BinaryOperation {
    override val symbol = "/"

    override fun apply(left: Double, right: Double): Double {
        if (right == 0.0) {
            throw ArithmeticException("Division by zero")
        }
        return left.div(right)
    }
}