package com.voxfinite.logvue.api.utils.deserializers.`object`

import org.junit.jupiter.api.Test

class ObjectDeserializerTest {

    @Test
    fun mapTest() {
        val string = """
            {context={session_visit_number=16, client_timestamp=1643621396141, client_timezone=IST, topic_name=gi_client_common_logging, session_id=ad2F03AA3EF57F097E1E0432D9626B4429115BDD821643619576931, correlationId=, template_id=26355}, event_details={item_selected=lobMatrix, item_position=2, screen_name=home, ab_experiment=Newhomepage2021, action=itemSelected, custom1=1, tag=Flights, event=home, lob=flight}, user=User(is_logged_in=true, uuid=g9ln3qkjpdyn5xw5v, loyalty=Loyalty(tier=1, tribe_coin_balance=66.0, gcp_balance=0.0, tier_start_date=15 Sep'20, tier_end_date=15 Mar'22)), device=Device(app_version=1957, device_id=20bc9bc86c8ea600, googleAdId=4872c655-ff0b-4eed-bfdf-e3c933a3c5df, device_resolution=1080x2250, flavour=android, geo_city=, geo_state=, geo_latitude=-1, geo_longitude=-1, manufacturer=OnePlus, model=LE2111, os_version=12, traffic_country=india, traffic_currency=inr, traffic_language=en, traffic_medium=(none), traffic_source=(direct), user_agent=Goibibo/14.5.0.debug (Android 12; LE2111 Build/SKQ1.210216.001), network_type=WIFI, carrier=airtel, ram=11805249536)}
        """.trimIndent()
        val map = ObjectDeserializer.map(string)
        val events = map["event_details"]
        assert(events is HashMap<*,*>)
        @Suppress("UNCHECKED_CAST")
        assert((events as HashMap<String,*>)["event"] == "home")
    }

    @Test
    fun simpleMapTest() {
        val inputMap = hashMapOf<String,String>()
        inputMap["demo"] = "added"
        inputMap["title"] = "test"
        val string = "$inputMap"
        val map = ObjectDeserializer.map(string)
        assert(map["title"] == "test")
    }

    @Test
    fun wrappedMapTest() {
        val inputMap = hashMapOf<String,String>()
        inputMap["demo"] = "added"
        inputMap["title"] = "test"
        val string = "D[$inputMap]"
        val map = ObjectDeserializer.map(string)
        assert(map["title"] == "test")
    }

}
