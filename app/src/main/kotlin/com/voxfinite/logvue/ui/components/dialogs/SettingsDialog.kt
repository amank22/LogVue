package com.voxfinite.logvue.ui.components.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
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
import com.voxfinite.app.APP_VERSION
import com.voxfinite.logvue.models.SocialIcons
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.components.ItemHeader
import com.voxfinite.logvue.ui.components.common.*
import com.voxfinite.logvue.utils.AppSettings
import com.voxfinite.logvue.utils.Helpers
import com.voxfinite.logvue.utils.SentryHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(onDismissRequest: () -> Unit) {
    SimpleVerticalDialog(header = "Settings", onDismissRequest = onDismissRequest) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            stickyHeader("h1-General") {
                ItemHeader("General", Modifier.fillMaxWidth().background(CustomTheme.colors.componentBackground))
            }
            item("darkMode") {
                DarkModeSwitch()
            }
            item("autoScroll") {
                AutoScrollSwitch()
            }
            item("divider-1") {
                Divider(color = CustomTheme.colors.componentOutline, thickness = (0.5).dp)
            }
            stickyHeader("h1-other") {
                ItemHeader("Other", Modifier.fillMaxWidth().background(CustomTheme.colors.componentBackground))
            }
            item("feedback") {
                Feedback()
            }
            item("aboutUs") {
                AboutUsBlock()
            }
            item("divider-2") {
                Divider(color = CustomTheme.colors.componentOutline, thickness = (0.5).dp)
            }
            item {
                GeneralInfoBlock(Modifier.fillMaxWidth().padding(8.dp))
            }
        }
    }
}

@Composable
private fun DarkModeSwitch() {
    var isDarkMode by remember { mutableStateOf(!Helpers.isThemeLightMode.value) }
    DarkModeSwitchItem(
        isDarkMode, "Dark Mode", Modifier.fillMaxWidth(),
        "Enable dark mode for less strain on eyes"
    ) {
        isDarkMode = it
        Helpers.switchThemes(!it)
    }
}

@Composable
private fun AutoScrollSwitch() {
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

@Composable
private fun Feedback() {
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
}

@Composable
private fun AboutUsBlock() {
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

@Composable
fun GeneralInfoBlock(modifier: Modifier = Modifier) {
    // Replace with a single view with app version, bug reporting enabled etc description
    Column(
        modifier, verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Version : $APP_VERSION", style = CustomTheme.typography.headings.caption)
        if (SentryHelper.isEnabled()) {
            Text("Crash reporting enabled", style = CustomTheme.typography.bodySmall)
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
