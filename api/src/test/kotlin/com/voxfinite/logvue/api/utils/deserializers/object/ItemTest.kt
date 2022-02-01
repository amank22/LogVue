package com.voxfinite.logvue.api.utils.deserializers.`object`

import org.junit.jupiter.api.Test

class ItemTest {

    @Test
    fun splitOnFirstLevelCommaRespectEqualSignTest() {
        val string = """
            {context={session_visit_number=16, client_timestamp=1643621396141, client_timezone=IST,
            correlationId=, template_id=26355},
             event_details={item_selected=lobMatrix, action=itemSelected, event=home}, user=User(is_logged_in=true,
              loyalty=Loyalty(tier=1, gcp_balance=0.0, tier_start_date=15 Sep'20, tier_end_date=15 Mar'22))}
        """.trimIndent()
        val items = Item.splitOnFirstLevelCommaRespectEqualSign(string)
        println(items)
    }

    @Test
    fun splitOnFirstLevelCommaRespectEqualSignInMap() {
        val string = """
            {context={session_visit_number=16, client_timestamp=1643621396141, client_timezone=IST,
            correlationId=, template_id=26355},
             event_details={item_selected=lobMatrix, action=itemSelected, event=home}, user=User(is_logged_in=true,
              loyalty=Loyalty(tier=1, gcp_balance=0.0, tier_start_date=15 Sep'20, tier_end_date=15 Mar'22))}
        """.trimIndent()
        val items = Item.splitOnFirstLevelCommaRespectEqualSignInMap(string.removePrefix("{").removeSuffix("}"))
        println(items)
    }

    @Test
    fun splitOnFirstLevelComma() {
        val string = """
            {context={session_visit_number=16, client_timestamp=1643621396141, client_timezone=IST,
            correlationId=, template_id=26355},
             event_details={item_selected=lobMatrix, action=itemSelected, event=home}, user=User(is_logged_in=true,
              loyalty=Loyalty(tier=1, gcp_balance=0.0, tier_start_date=15 Sep'20, tier_end_date=15 Mar'22))}
        """.trimIndent()
        val items = Item.splitOnFirstLevelCommaInMap(string)
        println(items)
    }

}
