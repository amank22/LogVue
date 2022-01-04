package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import models.LogItem
import ui.CustomTheme

@Composable
fun DetailCard(logItem: LogItem, modifier: Modifier = Modifier, onCloseClick: () -> Unit) {
    val text = logItem.propertiesAString ?: AnnotatedString("")
    Column(modifier) {
        var copyClicked by remember { mutableStateOf(false) }
        DetailHeader(logItem, Modifier.fillMaxWidth().padding(16.dp), onCloseClick) {
            copyClicked = true
        }
        if (copyClicked) {
            val copyText = AnnotatedString(logItem.eventName + "\n\n") + text
            LocalClipboardManager.current.setText(copyText)
            Popup { Text("Text Copied") } // TODO: Change this to inline or something else
        }
        Divider(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), Color.Gray)
        val scrollState = rememberScrollState()
        SelectionContainer {
            Text(text, Modifier.padding(16.dp).fillMaxHeight().verticalScroll(scrollState), lineHeight = 8.sp)
        }
    }
}

@Composable
fun DetailCardEmpty() {
    Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // show empty state
        Image(
            painterResource("icons/waiting.svg"),
            "Select log",
            Modifier.fillMaxWidth(0.6f).graphicsLayer { rotationY = 180f },
            contentScale = ContentScale.FillWidth
        )
        Spacer(Modifier.height(24.dp))
        Text("Select log to see full details", textAlign = TextAlign.Center)
    }
}

@Composable
fun DetailHeader(
    logItem: LogItem,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    // TODO: Move to something like in ActionBar
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onCloseClick) {
                Icon(
                    painterResource("icons/ico_close.xml"), "Close",
                    tint = CustomTheme.colors.highContrast
                )
            }
            LogIcon(logItem)
            LogTitle(logItem, Modifier.padding(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton({
                onCopyClick()
            }) {
                Icon(
                    painterResource("icons/ico_copy.svg"), "Copy",
                    tint = CustomTheme.colors.highContrast
                )
            }
        }
    }
}
