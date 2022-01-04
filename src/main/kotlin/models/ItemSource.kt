package models

import java.io.Serializable

sealed class ItemSource(val type: String) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

object SourceFA : ItemSource("Firebase")
object SourceInternalContent : ItemSource("Content")
