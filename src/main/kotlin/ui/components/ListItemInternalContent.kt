package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import models.ErrorContent
import models.InternalContent
import models.NoLogsContent
import org.apache.logging.log4j.core.util.StringBuilderWriter
import storage.Db
import ui.CustomTheme
import java.io.PrintWriter

@Composable
fun ListItemInternalContent(internalContent: InternalContent?, modifier: Modifier = Modifier) {
    if (internalContent == null) return
    when (internalContent) {
        is NoLogsContent -> ListItemEmptyContent(internalContent, modifier)
        is ErrorContent -> ListErrorContent(internalContent, modifier)
    }
}

@Composable
private fun ListItemEmptyContent(noLogsContent: NoLogsContent, modifier: Modifier = Modifier) {
    Card(modifier) {
        val text = if (Db.sessionId() == null) {
            "Create a new session from side panel"
        } else {
            noLogsContent.msg
        }
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource("icons/empty_state.svg"), "Use start to log events",
                Modifier.fillMaxWidth(0.5f).graphicsLayer { rotationY = 180f })
            Spacer(Modifier.height(16.dp))
            Text(
                text, textAlign = TextAlign.Center,
                style = CustomTheme.typography.headings.h5
            )
        }
    }
}

@Composable
private fun ListErrorContent(errorContent: ErrorContent, modifier: Modifier = Modifier) {
    Card(modifier) {
        SelectionContainer {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(errorContent.error, style = CustomTheme.typography.headings.h3)
                Text("Exception:", style = CustomTheme.typography.headings.h6Medium)
                val throwable = errorContent.throwable
                if (throwable != null) {
                    val errorString = StringBuilderWriter()
                    val printWriter = PrintWriter(errorString)
                    throwable.printStackTrace(printWriter)
                    printWriter.flush()
                    Text(
                        errorString.toString(),
                        fontFamily = FontFamily.Monospace,
                        style = CustomTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
