package ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Immutable
data class CustomHeading(
    val h3: TextStyle = TextStyle(fontSize = 28.sp),
    val caption: TextStyle = TextStyle(fontSize = 12.sp),
)