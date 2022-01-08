package utils

const val APP_NAME = "LogVue"

//TODO: Move all strings here to support languages in future
interface StringRes {
    val appName: String
    val filterFaqTitle: String
}

class EnglishStringRes : StringRes {
    override val appName: String = APP_NAME
    override val filterFaqTitle: String = "Filter FAQ’s"

}
