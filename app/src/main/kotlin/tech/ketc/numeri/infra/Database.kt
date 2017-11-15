package tech.ketc.numeri.infra

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import tech.ketc.numeri.infra.dao.*
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.*

@Database(entities = arrayOf(AccountToken::class, TlGroupToTlInfo::class,
        TimelineInfo::class, TimelineGroup::class), version = 1)
@TypeConverters(TlType.Converter::class)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun timeLineGroupDao(): TimelineGroupDao
    abstract fun timeLineInfoDao(): TimelineInfoDao
}

@Database(entities = arrayOf(Image::class), version = 1)
@TypeConverters(MimeType.Converter::class)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}