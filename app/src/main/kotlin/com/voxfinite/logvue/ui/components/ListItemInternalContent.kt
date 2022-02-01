package com.voxfinite.logvue.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.voxfinite.logvue.api.models.ErrorContent
import com.voxfinite.logvue.api.models.InternalContent
import com.voxfinite.logvue.api.models.NoLogsContent
import com.voxfinite.logvue.storage.Db
import com.voxfinite.logvue.storage.SessionConfig
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.utils.ConfigConstants

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
            Image(painterResource("icons/empty_state.svg"),
                "Use start to log events",
                Modifier.fillMaxWidth(0.5f).graphicsLayer { rotationY = 180f })
            Spacer(Modifier.height(16.dp))
            Text(
                text, textAlign = TextAlign.Center, style = CustomTheme.typography.headings.h5
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ListErrorContent(errorContent: ErrorContent, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(errorContent.error, style = CustomTheme.typography.headings.h3)
            Divider()
            Column(
                Modifier.fillMaxWidth().background(CustomTheme.colors.componentOutline, CustomTheme.shapes.large)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Technical Exception:", style = CustomTheme.typography.headings.h6Medium)
                val errorString = createExceptionString(errorContent.throwable)
                SelectionContainer {
                    if (!errorString.isNullOrBlank()) {
                        Text(
                            errorString, fontFamily = FontFamily.Monospace, style = CustomTheme.typography.bodySmall
                        )
                    }
                }
            }
            DisableSelection {
                if (SessionConfig.boolDefaultOn(ConfigConstants.QUERY_INDEX)) {
                    val colors =
                        ButtonDefaults.textButtonColors(contentColor = CustomTheme.colors.alertColors.danger)
                    TextButton({
                        SessionConfig.set(ConfigConstants.QUERY_INDEX, false)
                    }, colors = colors) {
                        Text(CustomTheme.strings.turnOffFilterIndexText, textDecoration = TextDecoration.Underline)
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun ListError() {
    val exception = Exception("Testing error")
    ListErrorContent(ErrorContent("There is some error in this query", exception))
}

private fun createExceptionString(throwable: Throwable?): String? {
    if (throwable == null) return null
    val sb = StringBuilder()
    sb.append(throwable.message)
    val cause = throwable.cause?.message
    if (!cause.isNullOrBlank()) {
        sb.append(System.lineSeparator())
        sb.append(cause)
    }
    return sb.toString()
}
