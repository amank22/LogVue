package utils

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class HashMapEntity<K, V> : HashMap<K, V>() {

    private val isHashCodeCached = AtomicBoolean(false)
    private var cachedHashCode by Delegates.notNull<Int>()

    override fun hashCode(): Int {
        if (isHashCodeCached.get()) {
            return cachedHashCode
        }
        val hashCode = super.hashCode()
        cachedHashCode = hashCode
        isHashCodeCached.set(true)
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

}