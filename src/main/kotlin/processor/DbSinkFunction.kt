package processor

import models.LogItem
import models.SourceInternalContent
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import storage.Db


class DbSinkFunction : RichSinkFunction<LogItem>() {

    override fun open(parameters: Configuration?) {
        super.open(parameters)
    }

    override fun close() {
        super.close()
    }

    override fun invoke(value: LogItem?, context: SinkFunction.Context?) {
        super.invoke(value, context)
        if (value == null || value.source is SourceInternalContent) return
        Db.currentSession()[value.key()] = value
    }
}