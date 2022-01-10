package ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import models.SocialIcons
import ui.CustomTheme
import ui.components.ItemHeader
import ui.components.common.*
import utils.AppSettings
import utils.Helpers
import utils.SentryHelper

@Composable
fun SettingsDialog(onDismissRequest: () -> Unit) {
    SimpleVerticalDialog(header = "Settings", onDismissRequest = onDismissRequest) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GeneralSettingBlock(Modifier.fillMaxWidth())
            }
            item {
                Divider(color = CustomTheme.colors.componentOutline, thickness = (0.5).dp)
            }
            item {
                OtherSettingBlock(Modifier.fillMaxWidth())
            }
        }
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
        if (SentryHelper.isEnabled()) {
            var reportCrash by remember { mutableStateOf(AppSettings.getFlag("reportCrash")) }
            SwitchItem(
                reportCrash, "Report Crashes", Modifier.fillMaxWidth(),
                "Should we report crashes? We use it to make sure our app stays healthy. " +
                        "No private information is shared with us.",
                painterResource("icons/bug.svg")
            ) {
                reportCrash = it
                AppSettings.setFlag("reportCrash", it)
            }
        }
    }
}

@Composable
fun OtherSettingBlock(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ItemHeader("Other")
        Column {
            SimpleListItem(
                "Feedback / Issues", Modifier.fillMaxWidth(),
                "If you have any feedback or issues, we would love to hear it from you",
                painterResource("icons/ico-email.svg")
            )
            Row(
                Modifier.padding(start = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WebLinkButton(SocialIcons.GithubIssues, "Create new issue")
                WebLinkButton(SocialIcons.Email, "Mail Us")
            }
        }
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
        Column {
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
}

@Composable
private fun SocialIcon(icon: SocialIcons) {
    IconButton({ openBrowser(icon.url) }) {
        Icon(painterResource(icon.icon), "social")
    }
}

fun openBrowser(url: String) = Helpers.openInBrowser(url)
