package ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inputs.adb.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.LogItem
import models.SourceInternalContent
import processor.FlinkProcessor
import storage.Db
import ui.CustomTheme

@Composable
fun BodyPanel(
    processor: FlinkProcessor,
    sessionId: String,
    modifier: Modifier = Modifier
) {
    val logItems = remember(sessionId) { mutableStateListOf<LogItem>() }
    var streamRunning by remember(sessionId) { mutableStateOf(false) }
    var paramItems by remember(sessionId) { mutableStateOf(Db.parameterSet.toList()) }
    Column(modifier) {
        val scope = rememberCoroutineScope()
        val state = rememberSaveable(saver = LazyListState.Saver, key = sessionId) {
            LazyListState()
        }
        var actionMenuItems by remember(sessionId) { mutableStateOf(ActionMenu.DefaultList) }
        val currentDevice by AdbUtils.currentDeviceFlow.collectAsState()
        var errorString by remember(currentDevice) {
            mutableStateOf(if (currentDevice.isNullOrBlank()) "No device is connected" else "")
        }
        val onNewMessage: (msg: LogItem) -> Unit = { msg ->
            paramItems = Db.parameterSet.toList().sorted()
            logItems.add(msg)
        }
        val onError: (logError: LogCatErrors) -> Unit = {
            actionMenuItems = ActionMenu.DefaultList
            streamRunning = false
            errorString = when (it) {
                is LogErrorADBIssue -> {
                    "There is some issue with device. Check if your device is connected and your app is running"
                }
                is LogErrorDeviceNotConnected -> {
                    "Please connect your device or start an emulator"
                }
                is LogErrorNotEnabledForFA -> {
                    "Unable to enable logs for firebase"
                }
                is LogErrorPackageIssue -> {
                    "The app might not be installed or app processed is not running on the device. Please check."
                }
                is LogErrorUnknown -> {
                    "This is some unknown error in collecting logs. \n ${it.exception.localizedMessage}"
                }
            }
        }

        fun oldStreamFun() {
            fetchOldData(processor, scope) {
                onNewMessage(it)
                if (logItems.isNotEmpty()) {
                    scope.launch {
                        state.scrollToItem((logItems.size - 1).coerceAtLeast(0))
                    }
                }
            }
        }
        BodyHeader(
            sessionId, paramItems,
            Modifier.fillMaxWidth().background(CustomTheme.colors.componentBackground),
            !streamRunning
        ) {
            logItems.clear()
            oldStreamFun()
        }
        if (errorString.isNotBlank()) {
            ErrorBar(errorString)
        }
        ActionBarMenu(actionMenuItems, {
            streamData(processor, scope, onError, onNewMessage)
            actionMenuItems = ActionMenu.PauseList
            streamRunning = true
            errorString = ""
        }, {
            pauseProcessor(processor)
            actionMenuItems = ActionMenu.DefaultList
            streamRunning = false
        })
        Text(
            "Analytics Logs", Modifier.padding(24.dp),
            style = CustomTheme.typography.headings.h3
        )
        MainBodyContent(logItems, Modifier.fillMaxSize(), streamRunning, sessionId, state)
        LaunchedEffect(sessionId) {
            oldStreamFun()
        }
    }
}

@Composable
private fun ErrorBar(errorString: String) {
    Row(
        Modifier.fillMaxWidth().background(CustomTheme.colors.alertColors.danger)
            .padding(24.dp, 8.dp), horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource("icons/ico-alert.svg"), "Alert",
            tint = Color.White
        )
        Spacer(Modifier.width(8.dp))
        Text(errorString, color = Color.White)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainBodyContent(
    logItems: SnapshotStateList<LogItem>, modifier: Modifier = Modifier,
    streamRunning: Boolean = false,
    sessionId: String,
    state: LazyListState
) {
    val lastIndex = (logItems.size - 1).coerceAtLeast(0)
    Row(modifier) {
        var selectedItem by remember(sessionId) { mutableStateOf<LogItem?>(null) }
        LogListView(
            logItems, state, lastIndex, streamRunning,
            Modifier.fillMaxHeight().weight(0.6f)
        ) {
            selectedItem?.isSelected = false
            selectedItem = it
            selectedItem?.isSelected = true
        }
        if (isHaveLogItems(logItems)) {
            DetailedCard(
                selectedItem,
                Modifier.fillMaxHeight().weight(0.4f)
            ) {
                selectedItem = null
            }
        }
    }
}

private fun isHaveLogItems(logItems: SnapshotStateList<LogItem>) =
    logItems.isNotEmpty() && !(logItems.size == 1 && logItems.first().source == SourceInternalContent)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DetailedCard(selectedItem: LogItem?, modifier: Modifier, onCloseClick: () -> Unit) {
    Card(
        modifier,
        shape = RoundedCornerShape(topStart = 16.dp),
        elevation = 8.dp
    ) {
        AnimatedContent(selectedItem) {
            if (it != null) {
                DetailCard(it, Modifier.fillMaxSize(), onCloseClick)
            } else {
                DetailCardEmpty()
            }
        }
    }
}

@Composable
private fun LogListView(
    logItems: SnapshotStateList<LogItem>,
    state: LazyListState,
    lastIndex: Int,
    streamRunning: Boolean,
    modifier: Modifier,
    onClick: (logItem: LogItem) -> Unit
) {
    Box(modifier) {
        if (logItems.isNotEmpty()) {
            LogList(logItems, state = state, onClick = onClick)
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 4.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                ),
                reverseLayout = true,
                style = LocalScrollbarStyle.current.copy(minimalHeight = 24.dp)
            )
            if (isHaveLogItems(logItems)) {
                PortalToTopButton(state, lastIndex, Modifier.align(Alignment.BottomEnd).padding(24.dp))
            }
        } else {
            LoadingAnimation(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}

@Composable
private fun ActionBarMenu(actionMenuItems: List<ActionMenu>, onStreamStart: () -> Unit, onStreamPause: () -> Unit) {
    ActionBar(
        actionMenuItems, Modifier.fillMaxWidth()
            .background(CustomTheme.colors.componentBackground)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        when (it) {
            ActionExport -> {
                // TODO: Export action
            }
            ActionPause -> {
                onStreamPause()
            }
            ActionStart -> {
                onStreamStart()
            }
        }
    }
}

@Composable
private fun PortalToTopButton(state: LazyListState, lastIndex: Int, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    ExtendedFloatingActionButton({
        Text("Scroll to Top")
    }, {
        scope.launch {
            state.animateScrollToItem(lastIndex)
        }
    }, modifier, {
        Icon(
            painterResource("icons/ico-carrot-right.svg"), "scroll up",
            Modifier.rotate(-90f)
        )
    })
}

private fun streamData(
    processor: FlinkProcessor, scope: CoroutineScope,
    onError: (logError: LogCatErrors) -> Unit, onMessage: (msg: LogItem) -> Unit
) {
    scope.launch {
        processor.observeNewStream(onError) { msg ->
//                    Log.d("Got Message" , msg)
            onMessage(msg)
        }
    }
}

private fun fetchOldData(processor: FlinkProcessor, scope: CoroutineScope, onMessage: (msg: LogItem) -> Unit) {
    scope.launch {
        processor.fetchOldStream { msg ->
//                    Log.d("Got Message" , msg)
            onMessage(msg)
        }
    }
}

private fun pauseProcessor(processor: FlinkProcessor) {
    try {
        processor.pause()
    } catch (ex: CancelException) {
        println(ex.message)
    }
}