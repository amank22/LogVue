package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.LogItem
import models.SourceFA
import ui.CustomTheme
import ui.views.flow.FlowRow
import utils.Helpers
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogCard(logItem: LogItem, modifier: Modifier = Modifier, onClick: (logItem: LogItem) -> Unit) {
    val elevation = if (logItem.isSelected) 3.dp else 0.dp
    Card({
        onClick(logItem)
    }, modifier, indication = null, elevation = elevation) {
        LogCardContent(logItem)
    }
}

@Composable
private fun LogCardContent(logItem: LogItem) {
    Row {
        if (logItem.isSelected) {
            Box(
                Modifier.width(4.dp).height(48.dp)
                    .align(Alignment.CenterVertically)
                    .background(CustomTheme.colors.accent, RoundedCornerShape(0, 50, 50, 0))
            )
        }
        LogIcon(logItem, Modifier.padding(start = 16.dp, top = 20.dp, end = 8.dp))
        Column(Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 2.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LogTitle(logItem)
                val time = logItem.localTime
                val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.S")
                val timeString = formatter.format(time)
                Text(
                    timeString, style = CustomTheme.typography.headings.caption,
                    color = CustomTheme.colors.lowContrast
                )
            }
            Spacer(Modifier.height(16.dp))
            val properties = logItem.properties
            if (properties.isEmpty()) return@Column
            HorizontalFlow(properties)
        }
    }
}

@Composable
fun LogTitle(logItem: LogItem, modifier: Modifier = Modifier) {
    Text(
        logItem.eventName, modifier = modifier, color = CustomTheme.colors.highContrast, fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun LogIcon(logItem: LogItem, modifier: Modifier = Modifier) {
    Box(modifier.size(48.dp)) {
        val sourceIcon = when (logItem.source) {
            is SourceFA -> "icons/firebaseLogo.webp"
            else -> {
                "icons/firebaseLogo.webp"
            }
        }
        val sourceIconPainter = painterResource(sourceIcon)
        Image(sourceIconPainter, logItem.source.type, Modifier.size(36.dp))
        val typePainter = painterResource("icons/eventType_activity.svg")
        Image(
            typePainter, "type",
            Modifier.size(24.dp).align(Alignment.BottomEnd),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun HorizontalFlow(properties: HashMap<String, Any>) {
    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
        properties.keys.take(6).forEach {
            val key = it
            val value = properties[it]
            if (value !is Map<*, *>) {
                val takeValue = Helpers.valueShortText(value)
                if (takeValue.isBlank()) {
                    return@forEach
                }
                val text = "$key : $takeValue"
                Chip(
                    text,
                    bgColor = CustomTheme.colors.componentBackground2,
                    textColor = CustomTheme.colors.highContrast,
                    addBorder = false
                )
            }
        }
    }
}