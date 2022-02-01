package com.voxfinite.logvue.api.utils

import java.io.Serializable

/**
 * Copyright 2012-2015 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Wrapper for Map to allow efficient use in an IndexCollection.
 * MapEntities can be created via [QueryFactory.mapEntity]. Attributes can be created to read the entries
 * in these maps, using [QueryFactory.mapAttribute].
 *
 *
 * This works by optimizing the performance of the [.hashCode] and [.equals] methods - which
 * may be called frequently during query processing. The hashCode of the wrapped Map will be cached to improve the
 * performance of repeated invocations of [.hashCode]. The cached hashCode will be used in the
 * implementation of the [.equals] method too, to avoid computing equality entirely when the cached
 * hashCodes are different.
 *
 *
 * Note it is not safe to modify entries in maps which are indexed, although non-indexed entries may be modified
 * safely. Alternatively, remove and re-add the Map to the collection.
 */
class HashMapEntity<K, V> private constructor(val wrappedMap: MutableMap<K, V>, val cachedHashCode: Int) :
    MutableMap<K, V>, Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    constructor(mapToWrap: MutableMap<K, V>) : this(mapToWrap, mapToWrap.hashCode())

    /**
     * Returns the hashcode of the wrapped map which was cached when this MapEntity was created.
     * @return the hashcode of the wrapped map which was cached when this MapEntity was created.
     */
    override fun hashCode(): Int {
        return cachedHashCode
    }

    /**
     * Returns true if the cached hashcodes of both objects are equal and the wrapped maps are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is HashMapEntity<*, *>) {
            return false
        }
        return if (cachedHashCode != other.cachedHashCode) {
            false
        } else wrappedMap == other.wrappedMap
    }

    override fun toString(): String {
        return "HashEntity{" +
                "cachedHashCode=" + cachedHashCode +
                ", wrappedMap=" + wrappedMap +
                '}'
    }

    override fun get(key: K): V? {
        return wrappedMap[key]
    }

    override fun put(key: K, value: V): V? {
        return wrappedMap.put(key, value)
    }

    override fun remove(key: K): V? {
        return wrappedMap.remove(key)
    }

    override fun putAll(from: Map<out K, V>) {
        wrappedMap.putAll(from)
    }

    override fun clear() {
        wrappedMap.clear()
    }

    override val keys: MutableSet<K>
        get() = wrappedMap.keys

    override val values: MutableCollection<V>
        get() = wrappedMap.values

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = wrappedMap.entries

    override val size: Int
        get() = wrappedMap.size

    override fun isEmpty(): Boolean {
        return wrappedMap.isEmpty()
    }

    override fun containsKey(key: K): Boolean {
        return wrappedMap.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return wrappedMap.containsValue(value)
    }
}
