package com.voxfinite.logvue.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class CustomHeading(
    val fontFamily: FontFamily?,
    val h2: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 39.sp, fontFamily = fontFamily),
    val h3: TextStyle = TextStyle(fontSize = 28.sp, fontFamily = fontFamily),
    val h5: TextStyle = TextStyle(
        fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (1.5).sp, fontFamily = fontFamily
    ),
    val h6: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily),
    val h6Semi: TextStyle = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp, fontFamily = fontFamily
    ),
    val h6Medium: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = fontFamily),
    val caption: TextStyle = TextStyle(fontSize = 12.sp, fontFamily = fontFamily),
    val semiText: TextStyle = TextStyle(fontSize = 11.sp, fontFamily = fontFamily),
)
