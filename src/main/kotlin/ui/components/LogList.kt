package ui.components

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.LogItem
import models.SourceInternalContent

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LogList(
    list: List<LogItem>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onClick: (logItem : LogItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    //    RemainingItems(state, lastIndex)
    LazyColumn(
        modifier.onPreviewKeyEvent {
            handleArrowKeyScroll(it, state, scope)
        }, reverseLayout = true, state = state, verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(24.dp, 0.dp, 24.dp, 24.dp)
    ) {
        items(list, key = { item: LogItem -> (item.key()) }) {
            if (it.source == SourceInternalContent) {
                ListItemInternalContent(it.internalContent, Modifier.fillMaxWidth())
            } else {
                LogCard(it, Modifier.fillMaxWidth(), onClick)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleArrowKeyScroll(
    keyEvent: KeyEvent,
    state: LazyListState,
    scope: CoroutineScope
): Boolean {
    return when (keyEvent.key) {
        // TODO: Doesn't seems to work here
        Key.DirectionUp -> {
            scope.launch {
                state.animateScrollBy(-50f)
            }
            true
        }
        Key.DirectionDown -> {
            scope.launch {
                state.animateScrollBy(50f)
            }
            true
        }
        else -> {
            false
        }
    }
}