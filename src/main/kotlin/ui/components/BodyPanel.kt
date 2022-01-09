package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inputs.adb.LogCatErrors
import inputs.adb.ddmlib.Devices
import inputs.adb.logcatErrorString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.LogItem
import models.SourceInternalContent
import processor.MainProcessor
import ui.CustomTheme
import utils.AppSettings

@Composable
fun BodyPanel(
    processor: MainProcessor,
    sessionId: String?,
    modifier: Modifier = Modifier
) {
    val logItems = remember(sessionId) { mutableStateListOf<LogItem>() }
    var streamRunning by remember(sessionId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberSaveable(saver = LazyListState.Saver, key = sessionId) {
        LazyListState()
    }

    fun scrollToTop() {
        if (logItems.isNotEmpty()) {
            scope.launch {
                state.scrollToItem((logItems.size - 1).coerceAtLeast(0))
            }
        }
    }

    val onNewMessage: (msg: List<LogItem>) -> Unit = { msg ->
        logItems.addAll(msg)
        if (AppSettings.getFlag(AppSettings.AUTO_SCROLL)) {
            scrollToTop()
        }
    }

    fun oldStreamFun(filterQuery: String? = null) {
        fetchOldData(processor, scope, filterQuery) {
            onNewMessage(it)
            scrollToTop()
        }
    }

    Column(modifier) {
        var actionMenuItems by remember(sessionId) { mutableStateOf(ActionMenu.DefaultList) }
        val currentDevice by Devices.currentDeviceFlow.collectAsState()
        var errorString by remember(currentDevice) {
            mutableStateOf(if (currentDevice == null) "No device is connected" else "")
        }

        val onError: (logError: LogCatErrors) -> Unit = {
            actionMenuItems = ActionMenu.DefaultList
            streamRunning = false
            errorString = logcatErrorString(it)
        }

        BodyHeader(
            sessionId,
            Modifier.fillMaxWidth().background(CustomTheme.colors.componentBackground),
            !streamRunning
        ) {
            logItems.clear()
            oldStreamFun(it)
        }
        if (errorString.isNotBlank()) {
            ErrorBar(errorString)
        }
        var isOpen by remember { mutableStateOf(false) }
        if (isOpen) {
            val sessionInfo = processor.getSessionInfo(sessionId.orEmpty())
            if (sessionInfo != null) {
                ui.components.dialogs.ExportDialog(sessionInfo, logItems) {
                    isOpen = false
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Analytics Logs", Modifier.padding(24.dp),
                style = CustomTheme.typography.headings.h3
            )
            if (!sessionId.isNullOrBlank()) {
                ActionBar(
                    actionMenuItems, Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    when (it) {
                        ActionExport -> {
                            isOpen = true
                        }
                        ActionPause -> {
                            pauseProcessor(processor)
                            actionMenuItems = ActionMenu.DefaultList
                            streamRunning = false
                        }
                        ActionStart -> {
                            streamData(processor, scope, onError, onNewMessage)
                            actionMenuItems = ActionMenu.PauseList
                            streamRunning = true
                            errorString = ""
                        }
                    }
                }
            }
        }
        MainBodyContent(logItems, Modifier.fillMaxSize(), sessionId, state)
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
    logItems: SnapshotStateList<LogItem>,
    modifier: Modifier = Modifier,
    sessionId: String?,
    state: LazyListState
) {
    val lastIndex = (logItems.size - 1).coerceAtLeast(0)
    Row(modifier) {
        var selectedItem by remember(sessionId) { mutableStateOf<LogItem?>(null) }
        LogListView(
            logItems, state, lastIndex, Modifier.fillMaxHeight().weight(0.6f)
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
                selectedItem?.isSelected = false
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
                PortalToTopButton(state, lastIndex, Modifier.align(Alignment.TopCenter).padding(8.dp))
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
private fun PortalToTopButton(state: LazyListState, lastIndex: Int, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val firstVisibleItemIndex = state.firstVisibleItemIndex
    val itemsAtTop = lastIndex - firstVisibleItemIndex

    val m1 = modifier
        .clip(CustomTheme.shapes.small)
        .clickable {
            scope.launch {
                state.animateScrollToItem(lastIndex)
            }
        }
        .graphicsLayer(shadowElevation = 8.dp.value, clip = true)
        .background(CustomTheme.colors.highContrast, CustomTheme.shapes.small)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    AnimatedVisibility(
        itemsAtTop > 6, m1,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$itemsAtTop", style = CustomTheme.typography.headings.h6,
                color = CustomTheme.colors.componentBackground2
            )
            Icon(
                painterResource("icons/ico-carrot-right.svg"), "scroll up",
                Modifier.size(16.dp).rotate(-90f),
                tint = CustomTheme.colors.componentBackground2
            )
        }
    }
}

private fun streamData(
    processor: MainProcessor,
    scope: CoroutineScope,
    onError: (logError: LogCatErrors) -> Unit,
    onMessage: (msg: List<LogItem>) -> Unit
) {
    scope.launch {
        processor.observeNewStream(onError) { msg ->
            onMessage(msg)
        }
    }
}

private fun fetchOldData(
    processor: MainProcessor,
    scope: CoroutineScope,
    filterQuery: String? = null,
    onMessage: (msg: List<LogItem>) -> Unit
) {
    scope.launch {
        processor.fetchOldStream(filterQuery, onMessage)
    }
}

private fun pauseProcessor(processor: MainProcessor) {
    processor.pause()
}
