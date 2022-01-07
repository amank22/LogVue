package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.MarkupText
import ui.CustomTheme
import ui.views.DarkToggleButton

@Composable
fun DarkModeSwitchItem(
    isDarkMode: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DarkToggleButton(isDarkMode, Modifier.padding(top = 4.dp).size(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = CustomTheme.typography.headings.h6Medium)
            if (subTitle != null) {
                Text(
                    subTitle,
                    style = CustomTheme.typography.headings.semiText,
                    color = CustomTheme.colors.mediumContrast
                )
            }
        }
        Switch(isDarkMode, onCheckedChange, Modifier.height(20.dp))
    }
}

@Composable
fun SwitchItem(
    checked: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    icon: Painter? = null,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (icon != null) {
            Icon(icon, title, Modifier.padding(top = 4.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = CustomTheme.typography.headings.h6Medium)
            if (subTitle != null) {
                Text(
                    subTitle,
                    style = CustomTheme.typography.headings.semiText,
                    color = CustomTheme.colors.mediumContrast
                )
            }
        }
        Switch(checked, onCheckedChange, Modifier.height(20.dp))
    }
}

@Composable
fun SimpleListItem(
    title: String?,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    icon: Painter? = null
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (icon != null) {
            Icon(icon, title, Modifier.padding(top = 4.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(title, style = CustomTheme.typography.headings.h6Medium)
            }
            if (subTitle != null) {
                Text(
                    subTitle,
                    style = CustomTheme.typography.headings.semiText,
                    color = CustomTheme.colors.mediumContrast
                )
            }
        }
    }
}

@Composable
fun SimpleListItem(
    title: MarkupText?,
    modifier: Modifier = Modifier,
    subTitle: MarkupText? = null,
    icon: Painter? = null,
    spacing: Dp = 16.dp
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        if (icon != null) {
            Icon(icon, title?.text ?: "Icon", Modifier.padding(top = 4.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(
                    title.text, style = CustomTheme.typography.headings.h6Medium,
                    lineHeight = 19.sp
                )
            }
            if (subTitle != null) {
                Text(
                    subTitle.text,
                    style = CustomTheme.typography.headings.caption,
                    color = CustomTheme.colors.mediumContrast,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun ClickableListItem(
    title: AnnotatedString?,
    modifier: Modifier = Modifier,
    subTitle: AnnotatedString? = null,
    icon: Painter? = null,
    onClick: (Int) -> Unit
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (icon != null) {
            Icon(icon, title?.text ?: "Icon", Modifier.padding(top = 4.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(title, style = CustomTheme.typography.headings.h6Medium)
            }
            if (subTitle != null) {
                ClickableText(
                    subTitle,
                    style = CustomTheme.typography.headings.semiText, onClick = onClick
                )
            }
        }
    }
}
