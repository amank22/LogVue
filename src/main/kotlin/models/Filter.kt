package models

import java.io.Serializable

data class Filter(val key : String, val value : Any, val operation : FilterOperation = FilterOperation.OpEqual) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
