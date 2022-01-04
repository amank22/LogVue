package ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ui.CustomTheme

@Composable
fun ItemHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = CustomTheme.typography.headings.h6Semi)
}