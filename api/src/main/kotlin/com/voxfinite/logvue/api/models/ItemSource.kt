package com.voxfinite.logvue.api.models

import java.io.Serializable

abstract class ItemSource(val type: String, val icon: String) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

object SourceFA : ItemSource("Firebase", "icons/firebaseLogo.webp") // TODO: Move to url
object SourceInternalContent : ItemSource("Content", "")
