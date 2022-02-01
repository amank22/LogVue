package com.voxfinite.logvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.LocalCustomColors
import com.voxfinite.logvue.ui.LocalCustomTypography

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = LocalCustomColors.current.componentBackground,
    textColor: Color = LocalCustomColors.current.highContrast,
    addBorder: Boolean = false,
    textStyle: TextStyle = LocalCustomTypography.current.bodySmall,
    icon: @Composable (() -> Unit)? = null,
) {
    var modifier1 = modifier.background(bgColor, CustomTheme.shapes.small)
    if (addBorder) {
        modifier1 = modifier1
            .border((0.2).dp, textColor, CustomTheme.shapes.small)
    }
    modifier1 = modifier1.padding(horizontal = 8.dp, vertical = 4.dp)
    Row(
        modifier1, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text, color = textColor, style = textStyle, textAlign = TextAlign.Center, maxLines = 1)
        if (icon != null) {
            Box(Modifier.size(18.dp)) {
                icon()
            }
        }
    }
}
