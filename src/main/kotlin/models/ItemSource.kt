package models

import java.io.Serializable

sealed class ItemSource(val type: String, val icon: String) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

object SourceFA : ItemSource("Firebase", "icons/firebaseLogo.webp")
object SourceInternalContent : ItemSource("Content", "")
