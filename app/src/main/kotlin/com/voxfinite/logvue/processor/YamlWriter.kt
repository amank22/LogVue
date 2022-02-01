package com.voxfinite.logvue.processor

import org.snakeyaml.engine.v2.api.StreamDataWriter
import java.io.PrintWriter

class YamlWriter(private val printWriter: PrintWriter) : StreamDataWriter {
    override fun write(str: String) {
        printWriter.write(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        printWriter.write(str, off, len)
    }

    override fun flush() {
        printWriter.flush()
    }
}
