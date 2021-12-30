package ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class CustomHeading(
    val h2: TextStyle = TextStyle(fontSize = 32.sp),
    val h3: TextStyle = TextStyle(fontSize = 28.sp),
    val h5: TextStyle = TextStyle(
        fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (1.5).sp
    ),
    val h6: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    val caption: TextStyle = TextStyle(fontSize = 12.sp),
)