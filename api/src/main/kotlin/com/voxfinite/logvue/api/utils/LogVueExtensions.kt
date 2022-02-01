package com.voxfinite.logvue.api.utils

fun <K, V> hashMapEntityOf(mapToWrap: MutableMap<K, V>): HashMapEntity<K, V> = HashMapEntity(mapToWrap)