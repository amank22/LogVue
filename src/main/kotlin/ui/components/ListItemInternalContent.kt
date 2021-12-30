package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import models.InternalContent
import models.NoLogsContent
import storage.Db
import ui.CustomTheme

@Composable
fun ListItemInternalContent(internalContent: InternalContent?, modifier: Modifier = Modifier) {
    if (internalContent == null) return
    when (internalContent) {
        NoLogsContent -> ListItemEmptyContent(modifier)
    }
}

@Composable
fun ListItemEmptyContent(modifier: Modifier = Modifier) {
    Card(modifier) {
        val text = if (Db.sessionId() == null) {
            "Create a new session from side panel"
        } else {
            "Record logs using the Start button above"
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
