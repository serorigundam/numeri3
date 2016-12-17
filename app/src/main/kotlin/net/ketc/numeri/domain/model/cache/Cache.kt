package net.ketc.numeri.domain.model.cache

import java.io.Serializable

interface Cacheable<out ID : Serializable> : Serializable {
    val id: ID
}

interface ConversionCache<in O : Serializable, out C : Cacheable<ID>, ID : Serializable> {
    fun get(id: ID): C?
    /**
     * @param obj is converted to [C] and cache
     * @return cached data
     */
    fun putOrGet(obj: O): C
}

