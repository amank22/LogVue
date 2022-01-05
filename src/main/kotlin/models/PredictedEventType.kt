package models

import java.io.Serializable

sealed class PredictedEventType(val displayMsg: String, val iconResource: String) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

object EventTypeClick : PredictedEventType("Click", "icons/mouse-pointer-click.svg")
object EventTypeView : PredictedEventType("View", "icons/eye.svg")
object EventTypeInteraction : PredictedEventType("Interaction", "icons/activity.svg")
object EventTypeQuery : PredictedEventType("Search", "icons/ico-search.svg")
object EventTypeLoad : PredictedEventType("Load", "icons/loader.svg")
object EventTypeCheckout : PredictedEventType("Checkout", "icons/shopping-cart.svg")
object EventTypePurchase : PredictedEventType("Purchase", "icons/shopping-bag.svg")
object EventTypeReward : PredictedEventType("Reward", "icons/gift.svg")
object EventTypeBug : PredictedEventType("Bug/Exception", "icons/bug.svg")
object EventTypeDelete : PredictedEventType("Delete/Remove", "icons/trash-2.svg")
object EventTypeNotification : PredictedEventType("Notification", "icons/bell.svg")
object EventTypeLogIn : PredictedEventType("LogIn", "icons/log-in.svg")
object EventTypeLogOut : PredictedEventType("LogOut", "icons/log-out.svg")
object EventTypeNotSure : PredictedEventType("Other", "icons/activity.svg")


val predictionPropertiesMap = hashMapOf<String, PredictedEventType>().apply {
    put("clicked", EventTypeClick)
    put(" click", EventTypeClick)
    put("openScreen", EventTypeView)
    put("screenView", EventTypeView)
    put("screenLoad", EventTypeLoad)
    put("viewed", EventTypeView)
    put("cardView", EventTypeView)
    put("itemView", EventTypeView)
    put("action", EventTypeInteraction)
    put("loaded", EventTypeLoad)
    put("loading", EventTypeLoad)
    put("cardLoad", EventTypeLoad)
    put("itemLoad", EventTypeLoad)
    put("rendered", EventTypeView)
    put("queried", EventTypeQuery)
    put("searched", EventTypeQuery)
    put("interaction", EventTypeInteraction)
    put("impression", EventTypeInteraction)
    put("checkout", EventTypeCheckout)
    put("purchase", EventTypePurchase)
    put("rewarded", EventTypeReward)
    put("exception", EventTypeBug)
    put("notification_", EventTypeNotification)
    put("deleted", EventTypeDelete)
    put("removed", EventTypeDelete)
}

val predictionEventNameMap = hashMapOf<String, PredictedEventType>().apply {
    put("clicked", EventTypeClick)
    put("openScreen", EventTypeView)
    put("viewed", EventTypeView)
    put("screenView", EventTypeView)
    put("screenLoad", EventTypeLoad)
    put("itemView", EventTypeView)
    put("cardView", EventTypeView)
    put("loaded", EventTypeLoad)
    put("loading", EventTypeLoad)
    put("queried", EventTypeQuery)
    put("searched", EventTypeQuery)
    put("interaction", EventTypeInteraction)
    put("impression", EventTypeInteraction)
    put("rendered", EventTypeView)
    put("checkout", EventTypeCheckout)
    put("purchase", EventTypePurchase)
    put("rewarded", EventTypeReward)
    put("exception", EventTypeBug)
    put("notification_", EventTypeNotification)
    put("loggedIn", EventTypeLogIn)
    put("loggedOut", EventTypeLogOut)
    put("signIn", EventTypeLogIn)
    put("logIn", EventTypeLogIn)
    put("logOut", EventTypeLogOut)
    put("signOut", EventTypeLogOut)
    put("deleted", EventTypeDelete)
    put("removed", EventTypeDelete)
}
