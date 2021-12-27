package inputs.adb

import java.io.Serializable
import java.util.function.Consumer

class CommonStdConsumer : Consumer<String?>, Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    override fun accept(t: String?) {
        println(t)
    }

}