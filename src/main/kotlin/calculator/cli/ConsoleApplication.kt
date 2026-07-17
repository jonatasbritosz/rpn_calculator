package calculator.cli

import calculator.application.ProcessingResult
import calculator.application.RpnProcessor
import java.io.BufferedReader
import java.io.PrintWriter

/**
 * Console adapter: reads whitespace-delimited tokens line by line and writes
 * processor outcomes. Stops immediately on an [ProcessingResult.Exit] outcome
 * or on end of input (EOF).
 */
class ConsoleApplication(
    private val processor: RpnProcessor,
    private val reader: BufferedReader,
    private val writer: PrintWriter,
) {

    fun run() {
        while (true) {
            writer.print(PROMPT)
            writer.flush()
            val line = reader.readLine() ?: break
            if (!processLine(line)) break
        }
    }

    private fun processLine(line: String): Boolean {
        for (token in line.split(WHITESPACE).filter(String::isNotEmpty)) {
            when (val result = processor.process(token)) {
                is ProcessingResult.Output -> writer.println(result.text)
                is ProcessingResult.Error -> writer.println(result.text)
                ProcessingResult.ContinueSilently -> Unit
                ProcessingResult.Exit -> return false
            }
        }
        return true
    }

    companion object {
        private const val PROMPT = "> "
        private val WHITESPACE = Regex("\\s+")
    }
}
