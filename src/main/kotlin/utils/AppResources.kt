package utils

//TODO: Move all strings here to support languages in future
interface StringRes {
    val appName: String
    val filterFaqTitle: String
    val appCrashText: String
}

class EnglishStringRes : StringRes {
    override val appName: String = "LogVue"
    override val filterFaqTitle: String = "Filter FAQ’s"
    override val appCrashText: String = "Unfortunately the app was crashed last time. " +
            "While we look into this, you can also report this issue to us on github or through " +
            "mail describing the scenario that caused this crash."

}
