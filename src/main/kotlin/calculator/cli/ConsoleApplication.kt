package calculator.cli

import calculator.application.ProcessingResult
import calculator.application.RpnProcessor
import java.io.BufferedReader
import java.io.PrintWriter

/**
 * Console adapter: reads whitespace-delimited tokens line by line, prints
 * errors as they occur, and echoes only the final result of each line (the
 * top of the stack after the last successful token). Stops immediately on an
 * [ProcessingResult.Exit] outcome or on end of input (EOF).
 */
class ConsoleApplication(
    private val processor: RpnProcessor,
    private val reader: BufferedReader,
    private val writer: PrintWriter,
    private val startupHelp: StartupHelp
) {

    fun run() {
        startupHelp.render(writer)
        while (true) {
            writer.print(PROMPT)
            writer.flush()
            val line = reader.readLine() ?: break
            if (!processLine(line)) break
        }
    }

    private fun processLine(line: String): Boolean {
        var lastResult: String? = null
        for (token in line.split(WHITESPACE).filter(String::isNotEmpty)) {
            when (val result = processor.process(token)) {
                is ProcessingResult.Output -> lastResult = result.text
                is ProcessingResult.Error -> writer.println(result.text)
                ProcessingResult.ContinueSilently -> Unit
                ProcessingResult.Exit -> {
                    lastResult?.let(writer::println)
                    return false
                }
            }
        }
        lastResult?.let(writer::println)
        return true
    }

    companion object {
        private const val PROMPT = "> "
        private val WHITESPACE = Regex("\\s+")
    }
}
