package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.LogItem
import models.SourceInternalContent

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LogList(
    list: List<LogItem>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onClick: (logItem: LogItem) -> Unit
) {
    LazyColumn(
        modifier, reverseLayout = true, state = state, verticalArrangement = Arrangement.spacedBy(16.dp),
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
