package ui.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import models.SocialIcons
import ui.CustomTheme
import ui.components.common.WebLinkButtonFilled
import utils.CustomExceptionHandler

@Composable
fun CrashDialog(onDismissRequest: () -> Unit) {
    CustomExceptionHandler.setLastCrashConsumed()
    SimpleVerticalDialog("Share crash", onDismissRequest) {
        Image(
            painterResource("icons/crash_illustration.xml"), "Crashed",
            Modifier.fillMaxWidth(0.9f), contentScale = ContentScale.FillWidth
        )
        Spacer(Modifier.height(16.dp))
        Text(CustomTheme.strings.appCrashText, textAlign = TextAlign.Center, style = CustomTheme.typography.body)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            WebLinkButtonFilled(SocialIcons.GithubIssues, "Github Issue", Modifier.weight(0.5f))
            WebLinkButtonFilled(SocialIcons.Email, "Mail Us", Modifier.weight(0.5f))
        }

    }
}