package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.model.TwitterUser
import java.io.Serializable

interface Cacheable<out ID : Serializable> : Serializable {
    val id: ID
}

interface Cache<out C : Cacheable<ID>, ID : Serializable> {
    fun get(id: ID): C?
}

interface ConversionCache<in O : Serializable, out C : Cacheable<ID>, ID : Serializable> : Cache<C, ID> {

    /**
     * @param obj is converted to [C] and cache
     * @return cached data
     */
    fun put(obj: O): TwitterUser
}