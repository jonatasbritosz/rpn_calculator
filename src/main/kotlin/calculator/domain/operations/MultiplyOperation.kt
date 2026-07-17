package calculator.domain.operations

import calculator.domain.BinaryOperation

object MultiplyOperation : BinaryOperation {
    override val symbol = "*"

    override fun apply(left: Double, right: Double): Double = left.times(right)
}