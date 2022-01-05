package utils

import java.util.concurrent.atomic.AtomicBoolean

class HashMapEntity<K, V> : HashMap<K, V>() {

    private val isHashCodeCached = AtomicBoolean(false)
    private var cachedHashCode: Int = 0

    override fun hashCode(): Int {
        synchronized(this) {
            if (isHashCodeCached.get()) {
                return cachedHashCode
            }
            val hashCode = super.hashCode()
            cachedHashCode = hashCode
            isHashCodeCached.set(true)
            return hashCode
        }
    }

}
