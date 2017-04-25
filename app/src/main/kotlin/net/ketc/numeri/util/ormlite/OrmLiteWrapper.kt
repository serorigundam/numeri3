package net.ketc.numeri.util.ormlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.stmt.Where
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import net.ketc.numeri.Numeri
import net.ketc.numeri.util.log.d
import java.io.Serializable
import java.sql.SQLException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class DataBaseHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
    }

    override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        private val DB_VERSION = 1
        private val DB_NAME = "numeri.db"
    }

}

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createAll(data: Collection<E>) = data.forEach { create(it) }

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createOrUpdateAll(data: Collection<E>) = data.forEach { createOrUpdate(it) }

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.createIfNotExistsAll(data: Collection<E>) = data.forEach { createIfNotExists(it) }

fun <E : Entity<ID>, ID : Serializable> Dao<E, ID>.delete(where: Where<E, ID>.() -> Unit) {
    val deleteBuilder = deleteBuilder()
    where(deleteBuilder.where())
    deleteBuilder.delete()
}

interface Entity<out ID : Serializable> : Serializable {
    val id: ID
}

class Transaction(private val helper: DataBaseHelper) {
    fun <E : Entity<ID>, ID : Serializable> dao(tableClass: KClass<E>): Dao<E, ID> = helper.getDao(tableClass.java)
}

object DataBaseHelperFactory {
    var create: () -> DataBaseHelper = { getHelper() }
}

fun getHelper(context: Context = Numeri.application): DataBaseHelper = OpenHelperManager.getHelper(context, DataBaseHelper::class.java)

/**
 * this method performs operations related to Database
 *
 * @param handle handle
 */
inline fun <R> transaction(crossinline handle: Transaction.() -> R) = connectDataBase { connectionSource, helper ->
    var throwable: Throwable? = null
    try {
        return@connectDataBase TransactionManager.callInTransaction(connectionSource) {
            try {
                handle(Transaction(helper))
            } catch (e: Throwable) {
                throwable = e
                throw e
            }
        }!!
    } catch (e: SQLException) {
        throwable?.run {
            if (this !is SQLException) {
                throw this
            }
        }
        throw e
    }
}

/**
 * create tables
 * @param tables tables
 */
fun createTable(vararg tables: KClass<out Entity<*>>) = connectDataBase { connectionSource, _ ->
    tables.forEach {
        TableUtils.createTableIfNotExists(connectionSource, it.java)
    }
}

/**
 * drop table
 * @param table table
 */
fun clearTable(vararg table: KClass<out Entity<*>>) = connectDataBase { connectionSource, _ ->
    table.forEach {
        TableUtils.clearTable(connectionSource, it.java)
    }
}

val dataBaseConnectLock = ReentrantLock()

inline fun <R> connectDataBase(func: (ConnectionSource, DataBaseHelper) -> R): R {
    d("database connect lock", "hold count = ${dataBaseConnectLock.holdCount}")
    return dataBaseConnectLock.withLock {
        val helper = DataBaseHelperFactory.create()
        var connectionSource: ConnectionSource? = null
        try {
            connectionSource = helper.connectionSource
            func(connectionSource, helper)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            connectionSource?.run {
                try {
                    this.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                    throw e
                }
            }
            OpenHelperManager.releaseHelper()
        }
    }
}