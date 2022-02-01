package com.voxfinite.logvue.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxfinite.logvue.models.MarkupText
import com.voxfinite.logvue.models.SocialIcons
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.components.dialogs.openBrowser
import com.voxfinite.logvue.ui.views.DarkToggleButton

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        painterResource("icons/logo.svg"), CustomTheme.strings.appName,
        modifier,
        colorFilter = ColorFilter.tint(CustomTheme.colors.highContrast),
        contentScale = ContentScale.FillWidth
    )
}

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
                    title.text, style = CustomTheme.typography.headings.h6Medium, lineHeight = 19.sp
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
                    subTitle, style = CustomTheme.typography.headings.semiText, onClick = onClick
                )
            }
        }
    }
}

@Composable
fun MultiLineRadioButton(
    selected: Boolean,
    title: String?,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    spacing: Dp = 8.dp,
    onClick: () -> Unit
) {
    Row(
        modifier.clickable(MutableInteractionSource(), null, onClick = { onClick() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        RadioButton(selected, onClick)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(
                    title, style = CustomTheme.typography.headings.h6Medium, lineHeight = 19.sp
                )
            }
            if (subTitle != null) {
                Text(
                    subTitle,
                    style = CustomTheme.typography.headings.caption,
                    color = CustomTheme.colors.mediumContrast,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun WebLinkButton(
    socialIcons: SocialIcons,
    text: String,
    modifier: Modifier = Modifier
) {
    val buttonColors = ButtonDefaults.textButtonColors(
        contentColor = CustomTheme.colors.mediumContrast
    )
    TextButton({ openBrowser(socialIcons.url) }, modifier, colors = buttonColors) {
        Icon(painterResource(socialIcons.icon), socialIcons.name)
        Spacer(Modifier.width(4.dp))
        Text(text, style = CustomTheme.typography.bodySmall)
    }
}

@Composable
fun WebLinkButtonFilled(
    socialIcons: SocialIcons,
    text: String,
    modifier: Modifier = Modifier
) {
    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = CustomTheme.colors.mediumContrast
    )
    val elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
    Button({ openBrowser(socialIcons.url) }, modifier, colors = buttonColors, elevation = elevation) {
        Icon(painterResource(socialIcons.icon), socialIcons.name)
        Spacer(Modifier.width(4.dp))
        Text(text, style = CustomTheme.typography.bodySmall)
    }
}
