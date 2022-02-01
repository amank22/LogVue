package com.voxfinite.logvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.voxfinite.logvue.processor.QUERY_PREFIX
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.components.dialogs.FilterFaqDialog
import com.voxfinite.logvue.ui.components.dialogs.SettingsDialog

@Composable
fun BodyHeader(
    sessionId: String?,
    modifier: Modifier = Modifier,
    filtersEnabled: Boolean = true,
    onFilterUpdated: (filterText: String) -> Unit
) = FilterSearchHeader(modifier, sessionId, filtersEnabled, onFilterUpdated)

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun FilterSearchHeader(
    modifier: Modifier,
    sessionId: String?,
    filtersEnabled: Boolean,
    onFilterUpdated: (filterText: String) -> Unit
) {
    var filterText by remember(sessionId) { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    fun sendFilterBack() {
        val text = QUERY_PREFIX + " " + filterText.text.trim()
        onFilterUpdated(text)
    }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val m1 = Modifier.weight(1f).padding(horizontal = 20.dp, vertical = 8.dp).onPreviewKeyEvent {
            if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                sendFilterBack()
                true
            } else {
                false
            }
        }.onFocusChanged {
            isFocused = it.isFocused
        }
        FilterSearchBar(filterText, m1, filtersEnabled, isFocused) {
            filterText = it
        }
        HeaderEndIconsPanel(filterText.text, isFocused, Modifier.height(IntrinsicSize.Max).padding(end = 8.dp), {
            sendFilterBack()
        }, {
            filterText = TextFieldValue()
            focusManager.clearFocus()
            onFilterUpdated("")
        })
    }
}

@Composable
private fun FilterSearchBar(
    filterText: TextFieldValue,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isFocused: Boolean = false,
    endIcons: @Composable (() -> Unit)? = null,
    onValueChange: (TextFieldValue) -> Unit
) {
    val colors = TextFieldDefaults.textFieldColors(
        backgroundColor = CustomTheme.colors.componentBackground,
        focusedIndicatorColor = Color.Unspecified,
        unfocusedIndicatorColor = Color.Unspecified,
    )
    val placeholderText = if (isFocused) {
        ""
    } else if (enabled) {
        "Filter logs..."
    } else {
        "Pause stream to filter logs..."
    }
    TextField(filterText, onValueChange, modifier, enabled = enabled, placeholder = {
        Text(placeholderText, color = CustomTheme.colors.lowContrast)
    }, leadingIcon = {
        val painter = painterResource("icons/ico-search.svg")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter, "Search filters", tint = CustomTheme.colors.highContrast)
            Spacer(Modifier.width(8.dp))
            if (isFocused || filterText.text.isNotBlank()) {
                Text(QUERY_PREFIX, color = CustomTheme.colors.lowContrast)
            }
        }
    }, trailingIcon = endIcons, shape = RectangleShape, colors = colors, singleLine = true
    )
}

@Composable
private fun HeaderEndIconsPanel(
    text: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (text.isNotBlank()) {
            IconButton(onSearchClick) {
                val painter = painterResource("icons/ico_filter.svg")
                Icon(painter, "Close", tint = CustomTheme.colors.highContrast)
            }
        }
        if (isFocused) {
            IconButton(onCloseClick) {
                val painter = painterResource("icons/ico_close.xml")
                Icon(painter, "Close", tint = CustomTheme.colors.highContrast)
            }
        }
        var filterFaqOpen by remember { mutableStateOf(false) }
        if (filterFaqOpen) {
            FilterFaqDialog { filterFaqOpen = false }
        }
        IconButton({ filterFaqOpen = true }) {
            val painter = painterResource("icons/ico-help.svg")
            Icon(painter, "Close", tint = CustomTheme.colors.highContrast)
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(CustomTheme.colors.lowContrast))
        var showSettingDialog by remember { mutableStateOf(false) }
        IconButton({
            showSettingDialog = true
        }) {
            val painter = painterResource("icons/ico-settings.svg")
            Icon(painter, "Settings", tint = CustomTheme.colors.highContrast)
        }
        if (showSettingDialog) {
            SettingsDialog {
                showSettingDialog = false
            }
        }
    }
}
