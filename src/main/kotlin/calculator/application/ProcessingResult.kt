package calculator.application

/**
 * Explicit outcome of processing one token, so adapters never infer
 * business behavior from output strings.
 */
sealed interface ProcessingResult {
    data class Output(val text: String) : ProcessingResult
    data class Error(val text: String) : ProcessingResult
    data object ContinueSilently : ProcessingResult
    data object Exit : ProcessingResult
}
