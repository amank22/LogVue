package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import models.Filter
import models.FilterOperation
import storage.Db
import ui.CustomTheme
import utils.Helpers

private val signList = FilterOperation.getOpList()

@Composable
fun BodyHeader(
    sessionId: String, availableParams: List<String>, modifier: Modifier = Modifier,
    filtersEnabled: Boolean = true,
    onFilterUpdated: () -> Unit
) {
    Column(modifier) {
        var filterItems by remember(sessionId) { mutableStateOf(Db.getSessionFilters().toList()) }
        FilterSearchHeader(Modifier.fillMaxWidth(), sessionId, filtersEnabled, availableParams) {
            onFilterUpdated()
            filterItems = Db.getSessionFilters().toList()
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(8.dp)) {
            items(filterItems, { item -> item.key + item.value }) {
                // TODO: Change this to correct operation type
                val text = "${it.key} ${it.operation.opString} ${it.value}"
                Chip(
                    text, bgColor = CustomTheme.colors.highContrast,
                    textColor = CustomTheme.colors.componentBackground
                ) {
                    IconButton({
                        Db.deleteFilterInCurrentSession(it)
                        onFilterUpdated()
                        filterItems = Db.getSessionFilters().toList()
                    }) {
                        Icon(
                            painterResource("icons/ico_close.xml"), "delete filter",
                            tint = CustomTheme.colors.componentBackground
                        )
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FilterSearchHeader(
    modifier: Modifier,
    sessionId: String,
    filtersEnabled: Boolean,
    availableParams: List<String>,
    onFilterUpdated: () -> Unit
) {
    Box(modifier) {
        var filterText by remember(sessionId) { mutableStateOf(TextFieldValue("")) }
        var menuExpanded by remember(sessionId) { mutableStateOf(false) }
        var showErrorDialog by remember(sessionId) { mutableStateOf(false) }
        FilterSearchBar(
            filterText,
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            filtersEnabled,
            {
                HeaderEndIconsPanel()
            }) {
            filterText = it
            menuExpanded = filterText.text.isNotBlank()
        }
        val spacesSplitted = filterText.text.split(" ")
        val length = spacesSplitted.size
        if (length == 0) return
        DropdownMenu(
            menuExpanded, { menuExpanded = false }, false,
            Modifier.fillMaxWidth(0.3f).heightIn(80.dp, 200.dp)
        ) {
            CreateAutoSearchItems(availableParams, filterText, spacesSplitted, {
                val filterCreated = createFilter(spacesSplitted)
                if (filterCreated) {
                    menuExpanded = false
                    filterText = TextFieldValue()
                    onFilterUpdated()
                } else {
                    showErrorDialog = true
                }
            }) {
                filterText = if (length == 1) {
                    val combinedText = it
                    TextFieldValue(combinedText, TextRange(combinedText.length + 1))
                } else {
                    val combinedText = filterText.text + it
                    TextFieldValue(combinedText, TextRange(combinedText.length + 1))
                }
            }
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

fun createFilter(spacesSplitted: List<String>): Boolean {
    val length = spacesSplitted.size
    if (length < 3) return false
    val key = spacesSplitted[0].trim()
    val sign = spacesSplitted[1].trim()
    if (!signList.contains(sign)) return false
    val value = spacesSplitted.takeLast(length - 2).joinToString(" ").trim()
    val filter = Filter(key, value, FilterOperation.getOp(sign))
    Db.addFilterInCurrentSession(filter)
    return true
}

@Composable
private fun CreateAutoSearchItems(
    availableParams: List<String>,
    filterText: TextFieldValue,
    spacesSplitted: List<String>,
    onAddFilterClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    when (spacesSplitted.size) {
        1 -> {
            availableParams.forEach { param ->
                if (param.contains(filterText.text)) {
                    DropdownMenuItem({ onItemClick("$param ") }) {
                        Text(param)
                    }
                }
            }
        }
        2 -> {
            signList.forEach { param ->
                DropdownMenuItem({ onItemClick("$param ") }) {
                    Text(param)
                }
            }
        }
        else -> {
            DropdownMenuItem(onAddFilterClick) {
                Icon(painterResource("icons/Filter.svg"), "Filter")
                Spacer(Modifier.width(8.dp))
                Text("Create filter")
            }
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
private fun HeaderEndIconsPanel() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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