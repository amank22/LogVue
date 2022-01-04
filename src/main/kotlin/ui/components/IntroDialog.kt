package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.CustomTheme

@Composable
fun IntroDialog(onDismissRequest: () -> Unit) {
    CustomDialog(
        dialogWidthRatio = 0.28f,
        dialogHeightRatio = 0.56f, onDismissRequest = {}
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo(Modifier.fillMaxWidth(0.4f).padding(16.dp))
            Divider(color = CustomTheme.colors.componentOutline, thickness = (0.5).dp)
            Spacer(Modifier.height(16.dp))
            val text = "A Utility to record, analyze local analytical logs"
            Text(
                text, Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, style = CustomTheme.typography.headings.h5
            )
            Spacer(Modifier.height(16.dp))
            val introPoints = """
                Create multiple sessions
                Record logs using adb
                Filter logs using sql query on the event names or analytics parameters
                Export and share session data in multiple formats
                Extensible. Create plugins for your own tools
                Dark mode for night
            """.trimIndent()
            introPoints.lines().forEach {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Box(
                        Modifier.padding(top = 6.dp).size(8.dp).background(CustomTheme.colors.highContrast, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(it)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onDismissRequest, elevation = ButtonDefaults.elevation(0.dp)) {
                Text("Let's Start")
            }

        }
    }
}
