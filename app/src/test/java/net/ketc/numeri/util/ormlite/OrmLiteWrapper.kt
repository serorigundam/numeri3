package net.ketc.numeri.util.ormlite

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import java.io.Serializable
import kotlin.reflect.KClass

object DaoFactoryHolder {
    fun <E : Entity<ID>, ID : Serializable> getDao(tableClass: KClass<E>): Dao<E, ID> = DaoManager.createDao(ConnectionSourceHolder.connectionSource, tableClass.java)
}

object ConnectionSourceHolder {
    var connectionSource: ConnectionSource = JdbcConnectionSource("jdbc:h2:~/test")
}