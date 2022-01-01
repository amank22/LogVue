package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ui.CustomTheme
import utils.Helpers

@Composable
fun BodyHeader(
    sessionId: String?, modifier: Modifier = Modifier,
    filtersEnabled: Boolean = true,
    onFilterUpdated: (filterText: String) -> Unit
) {
    Column(modifier) {
        FilterSearchHeader(Modifier.fillMaxWidth(), sessionId, filtersEnabled, onFilterUpdated)
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun FilterSearchHeader(
    modifier: Modifier,
    sessionId: String?,
    filtersEnabled: Boolean,
    onFilterUpdated: (filterText: String) -> Unit
) {
    Box(modifier) {
        var filterText by remember(sessionId) { mutableStateOf(TextFieldValue("")) }
        var showErrorDialog by remember(sessionId) { mutableStateOf(false) }
        FilterSearchBar(
            filterText,
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).onPreviewKeyEvent {
                if (it.key == Key.Enter) {
                    onFilterUpdated(filterText.text)
                    true
                } else
                    false
            },
            filtersEnabled,
            {
                HeaderEndIconsPanel {
                    onFilterUpdated(filterText.text)
                }
            }) {
            filterText = it
        }

        if (showErrorDialog) {
            AlertDialog({
                showErrorDialog = false
            }, {
                TextButton({ showErrorDialog = false }, Modifier.padding(16.dp)) {
                    Text("Ok")
                }
            }, Modifier.fillMaxSize(0.25f), title = { Text("Error in creating filter") },
                text = { Text("Please select supported operations") })
        }
    }
}

@Composable
private fun FilterSearchBar(
    filterText: TextFieldValue,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    endIcons: @Composable (() -> Unit)? = null,
    onValueChange: (TextFieldValue) -> Unit
) {
    val colors = TextFieldDefaults.textFieldColors(
        backgroundColor = CustomTheme.colors.componentBackground,
        focusedIndicatorColor = Color.Unspecified,
        unfocusedIndicatorColor = Color.Unspecified,
    )
    val placeholderText = if (enabled) {
        "Filter logs..."
    } else {
        "Pause stream to filter logs..."
    }
    TextField(filterText, onValueChange, modifier, enabled = enabled, placeholder = {
        Text(placeholderText, color = CustomTheme.colors.lowContrast)
    }, leadingIcon = {
        val painter = painterResource("icons/ico-search.svg")
        Icon(painter, "Search filters", tint = CustomTheme.colors.highContrast)
    }, trailingIcon = endIcons, shape = RectangleShape, colors = colors, singleLine = true
    )
}

@Composable
private fun HeaderEndIconsPanel(onSearchClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onSearchClick) {
            Text("Search")
        }
        IconButton({}) { // TODO: Settings
            val painter = painterResource("icons/ico-settings.svg")
            Icon(painter, "Settings", tint = CustomTheme.colors.highContrast)
        }
        DarkThemeSwitch()
    }
}

@Composable
private fun DarkThemeSwitch() {
    var switched by remember { mutableStateOf(!Helpers.isThemeLightMode.value) }
    Switch(switched, {
        switched = it
        Helpers.switchThemes(!it)
    })
}