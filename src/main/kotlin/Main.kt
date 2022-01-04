import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import inputs.adb.ddmlib.AdbHelper
import processor.MainProcessor
import storage.Db
import ui.AppTheme
import ui.CustomTheme
import ui.components.BodyPanel
import ui.components.IntroDialog
import ui.components.SideNavigation
import utils.APP_NAME
import utils.Helpers
import utils.Log
import java.awt.Desktop

@Composable
@Preview
fun App() {
    val processor = remember { MainProcessor() }
    val isLightTheme by Helpers.isThemeLightMode.collectAsState()
    AppTheme(isLightTheme) {
        Row(Modifier.fillMaxSize().background(CustomTheme.colors.background)) {
            var sessionId by remember { mutableStateOf(Db.sessionId()) }
            SideNavigation(
                processor, sessionId, Modifier.fillMaxHeight().weight(0.2f)
                    .background(CustomTheme.colors.componentBackground)
            ) {
                sessionId = it.orEmpty()
            }
            Divider(Modifier.fillMaxHeight().width(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            BodyPanel(processor, sessionId, Modifier.fillMaxHeight().weight(0.8f))
        }
        LaunchedEffect(Unit) {
            AdbHelper.init()
        }
        LaunchIntroIfNeeded()
    }
}

@Composable
fun LaunchIntroIfNeeded() {
    var introLaunched by remember { mutableStateOf(Db.configs["isIntroLaunched"].toBoolean()) }
    if (!introLaunched) {
        IntroDialog {
            Db.configs["isIntroLaunched"] = "true"
            introLaunched = true
        }
    }
}

@Composable
private fun RemainingItems(state: LazyListState, lastIndex: Int) {
    val fVIOfState = state.firstVisibleItemIndex
    if (lastIndex - fVIOfState < 3) {
        val firstVisibleItemIndex = fVIOfState - state.layoutInfo.visibleItemsInfo.size
        Log.d("firstVisibleItemIndex", "${lastIndex - firstVisibleItemIndex}")
    }
}

@Composable
private fun ParameterList(list: List<String>, modifier: Modifier) {
    LazyColumn(modifier) {
        items(list, key = { item: String -> item }) {
            Column {
                Text(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application(false) {
    fun onClose(source: String) {
        Log.d("QuitHandler", "Quiting : $source")
        AdbHelper.close()
        Db.close()
    }
    Desktop.getDesktop().setQuitHandler { e, response ->
        onClose(e.source.toString())
        response.performQuit()
    }
    val onCloseRequest = {
        onClose("User Close")
        exitApplication()
    }
    val windowState = rememberWindowState(WindowPlacement.Floating, size = DpSize(1440.dp, 1024.dp))
    Window(onCloseRequest = onCloseRequest, title = APP_NAME, state = windowState) {
//        window.exceptionHandler = WindowExceptionHandler {
//            println(it)
//        }
        App()
    }
}

@Composable
private fun WindowScope.AppWindowTitleBar() = WindowDraggableArea {
    Box(Modifier.fillMaxWidth().height(24.dp).background(Color.White))
}
