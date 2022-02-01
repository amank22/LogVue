package com.voxfinite.logvue.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.utils.Helpers

@Composable
fun DetailCard(logItem: LogItem, modifier: Modifier = Modifier, onCloseClick: () -> Unit) {
//    val text = Helpers.createJsonString(logItem.properties)
    val yaml = Helpers.convertToYamlString(logItem.properties.wrappedMap) ?: ""
    val text = Helpers.convertYamlToAnnotated(yaml)
    Column(modifier) {
        var copyClicked by remember { mutableStateOf(false) }
        DetailHeader(logItem, Modifier.fillMaxWidth().padding(16.dp), onCloseClick) {
            copyClicked = true
        }
        Divider(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), Color.Gray)
        val scrollState = rememberScrollState()
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        if (copyClicked) {
            val copyText = "eventName: " + logItem.eventName + "\n\n" + text
            LocalClipboardManager.current.setText(AnnotatedString(copyText))
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Text Copied")
            }
        }
        Scaffold(
            modifier, scaffoldState = scaffoldState,
            backgroundColor = CustomTheme.colors.componentBackground
        ) {
            SelectionContainer(
                Modifier.padding(16.dp)
                    .verticalScroll(scrollState)
                    .horizontalScroll(rememberScrollState())
            ) {
                val isLightTheme = Helpers.isThemeLightMode.value
                val color = if (isLightTheme) Color.Black else Color.White
                Text(
                    text,
                    fontFamily = FontFamily.Monospace, softWrap = false,
                    overflow = TextOverflow.Visible,
                    color = color
                )
            }
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
