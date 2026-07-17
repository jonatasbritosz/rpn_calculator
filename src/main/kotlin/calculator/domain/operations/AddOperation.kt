package calculator.domain.operations

import calculator.domain.BinaryOperation

object AddOperation : BinaryOperation {
    override val symbol = "+"

    override fun apply(left: Double, right: Double): Double = left + right
}