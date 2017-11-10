package tech.ketc.numeri.infra

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import tech.ketc.numeri.infra.dao.AccountDao
import tech.ketc.numeri.infra.dao.ImageDao
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.infra.entity.AccountToken
import tech.ketc.numeri.infra.entity.Image

@Database(entities = arrayOf(AccountToken::class), version = 1)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}

@Database(entities = arrayOf(Image::class), version = 1)
@TypeConverters(MimeType.Converter::class)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}