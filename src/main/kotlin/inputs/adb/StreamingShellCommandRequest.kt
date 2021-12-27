package inputs.adb

import com.malinskiy.adam.Const
import com.malinskiy.adam.request.AsyncChannelRequest
import com.malinskiy.adam.request.NonSpecifiedTarget
import com.malinskiy.adam.request.Target
import com.malinskiy.adam.transport.ByteBufferPool
import com.malinskiy.adam.transport.Socket
import kotlinx.coroutines.channels.SendChannel
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer


open class StreamingShellCommandRequest(
    private val cmd: String,
    target: Target = NonSpecifiedTarget,
    socketIdleTimeout: Long? = null
) : AsyncChannelRequest<String, Unit>(target = target, socketIdleTimeout = socketIdleTimeout) {

    companion object {
        val MaxFilePacketPool: ByteBufferPool =
            ByteBufferPool(poolSize = Const.DEFAULT_BUFFER_SIZE, bufferSize = Const.MAX_FILE_PACKET_LENGTH * 100)
    }

    override suspend fun readElement(socket: Socket, sendChannel: SendChannel<String>): Boolean {
        withMaxFilePacketBuffer {
            val data = array()
            val count = socket.readAvailable(data, 0, data.size)
            when {
                count > 0 -> {
                    val stream = ByteArrayInputStream(data)
                    val streamReader = InputStreamReader(stream, Const.DEFAULT_TRANSPORT_ENCODING)
                    val bufferedReader = BufferedReader(streamReader)
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        sendChannel.send(line?:"")
                    }
                }
                count == -1 -> return true
                else -> Unit
            }
            return false
        }
    }

    private inline fun <R> withMaxFilePacketBuffer(block: ByteBuffer.() -> R): R {
        val instance = MaxFilePacketPool.borrow()
        return try {
            block(instance)
        } finally {
            println("Buffer clear")
            MaxFilePacketPool.recycle(instance)
        }
    }

    override fun serialize() = createBaseRequest("shell:$cmd")
    override suspend fun writeElement(element: Unit, socket: Socket) = Unit
}