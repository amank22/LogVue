package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import models.Faq
import ui.CustomTheme

@Composable
fun FaqDialog(
    title: String,
    faqs: List<Faq>,
    onDismissRequest: () -> Unit
) {
    SimpleVerticalDialog(
        header = title, onDismissRequest = onDismissRequest,
        paddingValues = PaddingValues()
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(faqs.size, { faqs[it].question }) {
                val faq = faqs[it]
                val bg = if (it % 2 == 0) {
                    CustomTheme.colors.componentOutline.copy(alpha = 0.4f)
                } else {
                    Color.Unspecified
                }
                SimpleListItem(
                    faq.question, Modifier.fillMaxWidth().background(bg).padding(horizontal = 16.dp, vertical = 8.dp),
                    faq.answer,
                    painterResource("icons/ico-help.svg"),
                    12.dp
                )
            }
        }
    }
}

@Composable
fun FilterFaqDialog(onDismissRequest: () -> Unit) {
    FaqDialog(CustomTheme.strings.filterFaqTitle, buildFilterFaqs(), onDismissRequest)
}

private fun buildFilterFaqs(): List<Faq> {
    val list = arrayListOf<Faq>()
    list.add(
        Faq(
            "How to filter event logs?", "You can use SQL query to filter events. " +
                    "The first part is already written and you just have write the condition.\n" +
                    "\nEx: eventName = ‘home’"
        )
    )
    val answer2 =
        "All events have an eventName attribute and other attributes passed in the event.\n\nEx: screenName <> ‘Home’"
    list.add(
        Faq("What attributes I can use?", answer2)
    )
    list.add(
        Faq(
            "What operations are possible?",
            "You can use a subset of operations from SQL like equals, not equals, like, IN etc."
        )
    )
    list.add(
        Faq(
            "Can I use nested objects in query?",
            "Yes, you can use dot operator for nested objects. Just make sure to single quote the nested key." +
                    "\n\nEx: ‘analytics.request_id’ = ‘123456’"
        )
    )
    list.add(
        Faq(
            "I am getting issue with query, what should I do?",
            "Make sure it’s a valid query. Also try wrapping keys & values with single quotes."
        )
    )
    return list
}
