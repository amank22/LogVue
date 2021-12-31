package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import processor.MainProcessor
import ui.CustomTheme

@Composable
fun CreateSessionButton(onClick: () -> Unit) {
    Button(
        onClick, Modifier.fillMaxWidth(0.8f), elevation = ButtonDefaults.elevation(0.dp),
        shape = RoundedCornerShape(0, 50, 50, 0)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource("icons/ico-plus.svg"), "plus")
            Text("Start New Session", color = contentColorFor(MaterialTheme.colors.primary))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SessionsList(
    sessions: List<String>,
    processor: MainProcessor,
    modifier: Modifier = Modifier,
    onSessionChange: (sessionId: String?) -> Unit,
    onSessionDelete: () -> Unit
) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions, key = { item: String -> item }) {
            val session = processor.getSessionInfo(it) ?: return@items
            val currentSession = processor.getCurrentSessionId()
            val shape = RoundedCornerShape(0, 50, 50, 0)
            val isThisCurrentSession = currentSession == it
            var showDeleteIcon by remember(it) { mutableStateOf(false) }
            var modifier1 = Modifier
                .clip(shape)
                .pointerMoveFilter(onEnter = {
                    showDeleteIcon = true
                    false
                }, onExit = {
                    showDeleteIcon = false
                    false
                })
                .clickable {
                    processor.startOldSession(it)
                    onSessionChange(processor.getCurrentSessionId())
                }.fillMaxWidth(0.95f)
            if (isThisCurrentSession) {
                modifier1 = modifier1.background(
                    CustomTheme.colors.accent.copy(0.4f),
                    shape
                )
            }
            modifier1 = modifier1.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
            Row(
                modifier1, verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.height(36.dp), Arrangement.Center) {
                    Text(session.description, maxLines = 1)
                    Text(session.appPackage, style = CustomTheme.typography.headings.caption, maxLines = 1)
                }
                if (showDeleteIcon) {
                    IconButton({
                        processor.deleteSession(it)
                        onSessionDelete()
                    }, Modifier.size(36.dp).padding(end = 16.dp)) {
                        Icon(painterResource("icons/ico-trashcan.svg"), "delete session")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySession() {
    val painter = painterResource("icons/ic_illustration_new_session.xml")
    Image(
        painter,
        "Start New session",
        Modifier.fillMaxWidth(0.8f).padding(start = 16.dp, end = 16.dp, top = 16.dp),
        contentScale = ContentScale.FillWidth
    )
    Text(
        "Create a new session to get started",
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    )
}