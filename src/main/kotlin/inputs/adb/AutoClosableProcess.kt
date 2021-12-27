package inputs.adb

import org.apache.flink.util.Preconditions
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import kotlin.concurrent.thread

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Utility class to terminate a given [Process] when exiting a try-with-resources statement.
 */
class AutoClosableProcess private constructor(
    private val commands: Array<String>,
    private var stdoutProcessor: Consumer<String?>,
    private val stderrProcessor: Consumer<String?>,
    private val stdInputs: Array<String>?,
) : AutoCloseable, Serializable {

    /**
     * Builder for most sophisticated processes.
     */
    class AutoClosableProcessBuilder internal constructor(commands: Array<String>) : Serializable {

        companion object {
            private const val serialVersionUID = 1L
        }

        private val commands: Array<String>
        private var stdoutProcessor: Consumer<String?> = CommonStdConsumer()
        private var stderrProcessor: Consumer<String?> = CommonStdConsumer()
        private var stdInputs: Array<String>? = null

        init {
            this.commands = arrayOf(*commands)
        }

        fun setStdoutProcessor(stdoutProcessor: Consumer<String?>): AutoClosableProcessBuilder {
            this.stdoutProcessor = stdoutProcessor
            return this
        }

        fun setStderrProcessor(stderrProcessor: Consumer<String?>): AutoClosableProcessBuilder {
            this.stderrProcessor = stderrProcessor
            return this
        }

        fun setStdInputs(vararg inputLines: String): AutoClosableProcessBuilder {
            Preconditions.checkArgument(inputLines.isNotEmpty())
            stdInputs = arrayOf(*inputLines)
            return this
        }

        @Throws(IOException::class)
        fun build(): AutoClosableProcess {
            return AutoClosableProcess(commands, stdoutProcessor, stderrProcessor, stdInputs)
        }
    }

    @Throws(IOException::class, CancelException::class)
    fun runBlocking() {
        val sw = StringWriter()
        PrintWriter(sw).use { printer ->
            process = createProcess(commands, stdoutProcessor, { line: String? ->
                stderrProcessor.accept(line)
                printer.println(line)
            }, stdInputs)
            val pid = process.pid()
            thread {
                aPid.set(pid)
            }
            try {
                val exitValue = process.waitFor()
                if (exitValue == 143) {
                    throw CancelException(IOException("Process execution failed due error. Error output:$sw"))
                }
                if (exitValue != 0) {
                    // use some proper mechanism to pass back errors
                    throw IOException("Process execution failed due exit code $exitValue. Error output:$sw")
                }
            } catch (e: TimeoutException) {
                throw IOException("Process failed due to timeout.")
            } catch (e: InterruptedException) {
                throw IOException("Process failed due to timeout.")
            }
        }
    }

    @Throws(IOException::class)
    fun runNonBlocking() {
        process = createProcess(commands, stdoutProcessor, stderrProcessor, stdInputs)
    }

    @Throws(CancelException::class)
    override fun close() {
//        throw CancelException()
//        createProcess(arrayOf("kill -15 ${aPid.get()}"), null, null, null)
        if (process.isAlive) {
            process.destroy()
            try {
                process.waitFor(10, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    @Throws(IOException::class)
    private fun createProcess(
        commands: Array<String>,
        stdoutProcessor: Consumer<String?>?,
        stderrProcessor: Consumer<String?>?,
        stdInputs: Array<String>?
    ): Process {
        val processBuilder = ProcessBuilder()
        val updatedCommands = concat(OS_LINUX_RUNTIME, commands)
        processBuilder.command(updatedCommands.toList())
        val process = processBuilder.start()
        if (stdoutProcessor != null) {
            consumeOutput(process.inputStream, stdoutProcessor)
        }
        if (stderrProcessor != null) {
            consumeOutput(process.errorStream, stderrProcessor)
        }
        if (stdInputs != null) {
            produceInput(process.outputStream, stdInputs)
        }
        return process
    }

    private fun consumeOutput(stream: InputStream, streamConsumer: Consumer<String?>) {
        Thread {
            try {
                BufferedReader(
                    InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8
                    )
                ).use { bufferedReader ->
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        streamConsumer.accept(line)
                    }
                }
            } catch (e: IOException) {
                LOG.error("Failure while processing process stdout/stderr.", e)
            }
        }.start()
    }

    private fun produceInput(stream: OutputStream, inputLines: Array<String>) {
        Thread {
            // try with resource will close the OutputStream automatically,
            // usually the process terminal will also be finished then.
            try {
                PrintStream(stream, true, StandardCharsets.UTF_8.name()).use { printStream ->
                    for (line in inputLines) {
                        printStream.println(line)
                    }
                }
            } catch (e: IOException) {
                LOG.error("Failure while processing process stdin.", e)
            }
        }.start()
    }

    private fun <T> concat(first: Array<T>, second: Array<T>): Array<T> {
        val result = Arrays.copyOf(first, first.size + second.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

    fun setStdoutProcessor(stdoutProcessor: Consumer<String?>) {
        this.stdoutProcessor = stdoutProcessor
    }

    companion object {
        private const val serialVersionUID = 1L
        private val LOG = LoggerFactory.getLogger(AutoClosableProcess::class.java)
        private val WIN_RUNTIME = arrayOf("cmd.exe", "/C")
        private val OS_LINUX_RUNTIME = arrayOf("/bin/bash", "-l", "-c")

        private val aPid = AtomicLong(0)
        private lateinit var process: Process

        @Throws(IOException::class)
        fun runNonBlocking(vararg commands: String) {
            return create(*commands).build().runNonBlocking()
        }

        @Throws(IOException::class)
        fun runBlocking(vararg commands: String) {
            create(*commands).build().runBlocking()
        }

        fun create(vararg commands: String): AutoClosableProcessBuilder {
            return AutoClosableProcessBuilder(arrayOf(*commands))
        }
    }
}