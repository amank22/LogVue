package com.voxfinite.logvue.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.voxfinite.logvue.ui.CustomTheme

@Composable
fun ItemHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = CustomTheme.typography.headings.h6Semi)
}
