package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ui.CustomTheme
import utils.AppSettings
import utils.Helpers

@Composable
fun SettingsDialog(onDismissRequest: () -> Unit) {

    SimpleVerticalDialog(header = "Settings", onDismissRequest = onDismissRequest) {
        GeneralSettingBlock(Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Divider(color = CustomTheme.colors.componentOutline, thickness = (0.5).dp)
        Spacer(Modifier.height(16.dp))
        OtherSettingBlock(Modifier.fillMaxWidth())
    }
}

@Composable
fun GeneralSettingBlock(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ItemHeader("General")
        var isDarkMode by remember { mutableStateOf(!Helpers.isThemeLightMode.value) }
        DarkModeSwitchItem(
            isDarkMode, "Dark Mode", Modifier.fillMaxWidth(),
            "Enable dark mode for less strain on eyes"
        ) {
            isDarkMode = it
            Helpers.switchThemes(!it)
        }
        var isAutoScroll by remember { mutableStateOf(AppSettings.getFlag(AppSettings.AUTO_SCROLL)) }
        SwitchItem(
            isAutoScroll, "Auto Scroll logs", Modifier.fillMaxWidth(),
            "When recording, auto-scroll to the latest incoming analytics logs",
            painterResource("icons/Tornado.svg")
        ) {
            isAutoScroll = it
            AppSettings.setFlag(AppSettings.AUTO_SCROLL, it)
        }
    }
}

@Composable
fun OtherSettingBlock(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ItemHeader("Other")
        SimpleListItem(
            "Feedback / Issues", Modifier.fillMaxWidth().clickable {
                openBrowser("mailto://kapoor.aman22@gmail.com")
            },
            "If you have any feedback or issues, we would love to hear it from you",
            painterResource("icons/ico-email.svg")
        )
        val aboutUsText = buildAnnotatedString {
            withStyle(SpanStyle(color = CustomTheme.colors.mediumContrast)) {
                append("This ")
                pushStringAnnotation("gitProjectLink", "https://github.com/amank22/LogVue")
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = CustomTheme.colors.highContrast
                    )
                ) {
                    append("open-source project")
                }
                pop()
                append(" is created by Aman Kapoor. Connect with him below.")
            }
        }
        ClickableListItem(
            AnnotatedString("About us"), Modifier.fillMaxWidth(),
            aboutUsText,
            painterResource("icons/ico_info.svg")
        ) { offset ->
            aboutUsText.getStringAnnotations(
                tag = "gitProjectLink", start = offset,
                end = offset
            ).firstOrNull()?.let {
                openBrowser(it.item)
            }
        }
        Row(Modifier.padding(start = 32.dp)) {
            SocialIcons.DefaultIcons.forEach {
                SocialIcon(it)
            }
        }
    }
}

@Composable
private fun SocialIcon(icon: SocialIcons) {
    IconButton({ openBrowser(icon.url) }) {
        Icon(painterResource(icon.icon), "social")
    }
}

sealed class SocialIcons(val icon: String, val url: String) {
    companion object {
        val DefaultIcons = listOf(SocialTwitter, SocialGithub, SocialLinkedin)
    }
}

object SocialTwitter : SocialIcons("icons/social/social_twitter.svg", "https://twitter.com/Aman22Kapoor")
object SocialGithub : SocialIcons("icons/social/social_github.svg", "https://github.com/amank22")
object SocialLinkedin : SocialIcons("icons/social/social_linkedIn.svg", "https://www.linkedin.com/in/amank22/")
object SocialFacebook : SocialIcons("icons/social/social_facebook.svg", "")

fun openBrowser(url: String) = Helpers.openInBrowser(url)
