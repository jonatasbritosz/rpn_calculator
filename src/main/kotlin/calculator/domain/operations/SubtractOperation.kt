package calculator.domain.operations

import calculator.domain.BinaryOperation

object SubtractOperation : BinaryOperation {
    override val symbol = "-"

    override fun apply(left: Double, right: Double): Double = left.minus(right)
}