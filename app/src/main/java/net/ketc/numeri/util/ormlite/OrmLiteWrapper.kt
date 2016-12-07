package net.ketc.numeri.util.ormlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import java.io.Serializable
import java.sql.SQLException
import javax.inject.Inject
import kotlin.reflect.KClass
import net.ketc.numeri.Injectors

private val DB_VERSION = 1
private val DB_NAME = "numeri.db"

class DataBaseHelper private constructor() {

    val helper: OrmLiteSqliteOpenHelper

    @Inject
    lateinit var application: Context

    init {
        Injectors.appComponent.inject(this)
        helper = object : OrmLiteSqliteOpenHelper(application, DB_NAME, null, DB_VERSION) {
            override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
            }

            override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
            }
        }
    }

    companion object {
        val INSTANCE = DataBaseHelper()
    }

}


fun <E : Entity<ID>, ID : Serializable> dao(tableClass: KClass<E>) = DataBaseHelper.INSTANCE.helper.getDao<Dao<E, ID>, E>(tableClass.java)

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createAll(data: Collection<E>) = data.forEach { create(it) }

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createOrUpdateAll(data: Collection<E>) = data.forEach { createOrUpdate(it) }

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createIfNotExistsAll(data: Collection<E>) = data.forEach { createIfNotExists(it) }


interface Entity<out ID : Serializable> : Serializable {
    val id: ID
}

/**
 * this method performs operations related to Database
 *
 * @param handle handle
 */
fun <R> transaction(handle: () -> R) = connect {
    var r: R? = null
    TransactionManager.callInTransaction(it) {
        r = handle()
    }
    r ?: throw InternalError()
}

fun createTable(vararg table: KClass<out Entity<*>>) = connect { connectionSource ->
    table.forEach {
        TableUtils.createTableIfNotExists(connectionSource, it.java)
    }
}

private inline fun <R> connect(func: (ConnectionSource) -> R): R {
    var connectionSource: ConnectionSource? = null
    try {
        connectionSource = DataBaseHelper.INSTANCE.helper.getConnectionSource()
        return func(connectionSource)
    } catch (e: Exception) {
        e.printStackTrace()
        throw RuntimeException(e)
    } finally {
        if (connectionSource != null) {
            try {
                connectionSource.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}
