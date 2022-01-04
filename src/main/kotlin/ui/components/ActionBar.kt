package ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ui.views.flow.FlowRow

@Composable
fun ActionBar(
    menus: List<ActionMenu> = ActionMenu.DefaultList,
    modifier: Modifier = Modifier,
    onMenuClick: (action: ActionMenu) -> Unit
) {
    FlowRow(modifier, mainAxisSpacing = 16.dp, crossAxisSpacing = 16.dp) {
        menus.forEach {
            val painter = painterResource(it.icon)
            val onClick = {
                onMenuClick(it)
            }
            if (it.isPrimary) {
                Button(onClick, content = {
                    ButtonContent(painter, it, ButtonDefaults.buttonColors())
                }, shape = RoundedCornerShape(4.dp), elevation = ButtonDefaults.elevation(0.dp))
            } else {
                TextButton(onClick, content = {
                    ButtonContent(painter, it, ButtonDefaults.textButtonColors())
                })
            }
        }
    }
}

@Composable
private fun ButtonContent(
    painter: Painter,
    it: ActionMenu,
    bgColors: ButtonColors
) {
    Icon(
        painter, it.text, Modifier.size(24.dp),
        tint = bgColors.contentColor(true).value
    )
    Text(it.text, Modifier.padding(start = 8.dp))
}

sealed class ActionMenu(val text: String, val isPrimary: Boolean, val icon: String = "") {
    companion object {
        val DefaultList = arrayListOf(ActionStart, ActionExport, ActionFeedback)
        val PauseList = arrayListOf(ActionPause, ActionExport, ActionFeedback)
    }
}

// TODO: Add enable flag to disable button when maybe device is not connected or there is no data to export
object ActionStart : ActionMenu("Start", isPrimary = true, icon = "icons/ico_play.svg")
object ActionPause : ActionMenu("Pause", isPrimary = true, icon = "icons/ico_pause.svg")
object ActionExport : ActionMenu("Export Session Data", isPrimary = false, icon = "icons/ico-share.svg")
object ActionFeedback : ActionMenu("Feedback", isPrimary = false, icon = "icons/ico-email.svg")
