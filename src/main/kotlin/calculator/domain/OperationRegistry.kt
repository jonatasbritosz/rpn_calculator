package calculator.domain

import calculator.domain.operations.AddOperation
import calculator.domain.operations.DivideOperation
import calculator.domain.operations.MultiplyOperation
import calculator.domain.operations.SubtractOperation

/**
 * Single source of truth for the operator symbols the calculator supports.
 */
class OperationRegistry(operations: List<BinaryOperation>) {

    private val operationsBySymbol: Map<String, BinaryOperation> =
        operations.associateBy(BinaryOperation::symbol)

    val symbols: List<String> get() = operationsBySymbol.keys.toList()

    fun resolve(symbol: String): BinaryOperation? = operationsBySymbol[symbol]

    companion object {
        fun standard(): OperationRegistry = OperationRegistry(
            listOf(AddOperation, SubtractOperation, MultiplyOperation, DivideOperation),
        )
    }
}