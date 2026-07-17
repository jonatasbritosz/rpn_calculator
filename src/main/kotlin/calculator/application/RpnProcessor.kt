package calculator.application

import calculator.domain.CalculatorStack
import calculator.domain.OperationRegistry

/**
 * Token-by-token RPN calculator core, independent of any input/output channel.
 *
 * Every failure is recoverable: an error outcome never mutates the stack,
 * so processing can continue with the previous state.
 */
class RpnProcessor(
    private val registry: OperationRegistry,
    private val stack: CalculatorStack,
) {

    fun process(token: String): ProcessingResult {
        if (token == QUIT_COMMAND) return ProcessingResult.Exit
        if (token.isBlank()) return ProcessingResult.ContinueSilently

        token.toDoubleOrNull()?.let { number ->
            if (!number.isFinite()) {
                return ProcessingResult.Error("Number is not finite: $token")
            }
            stack.push(number)
            return ProcessingResult.Output(format(number))
        }

        registry.resolve(token)?.let { operation ->
            return stack.apply(operation).fold(
                onSuccess = { value -> ProcessingResult.Output(format(value)) },
                onFailure = { error -> ProcessingResult.Error(error.message ?: "Operation failed") },
            )
        }

        return ProcessingResult.Error("Unrecognized token: $token")
    }

    private fun format(value: Double): String = value.toString()

    companion object {
        private const val QUIT_COMMAND = "q"
    }
}