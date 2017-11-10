package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import tech.ketc.numeri.infra.entity.Image

@Dao
interface ImageDao : IDao<Image> {

    @Query("SELECT * FROM image WHERE id=:id LIMIT 1")
    fun selectById(id: String): Image?

    @Query("SELECT COUNT(id) FROM image")
    fun count(): Int

    @Query("DELETE FROM image WHERE NOT EXISTS(SELECT * FROM image as im where image.timestamp > im.timestamp)")
    fun deleteOldest()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override fun insert(t: Image)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override fun insertAll(vararg t: Image)
}