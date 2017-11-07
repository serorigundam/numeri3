package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert


interface IDao<T> {
    @Insert
    fun insertAll(vararg t: T)

    @Insert
    fun insert(t: T)

    @Delete
    fun delete(t: T)

    @Delete
    fun deleteAll(vararg t: T)
}