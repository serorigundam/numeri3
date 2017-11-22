package tech.ketc.numeri.util.android.ui.recycler


interface DataSource<Key, Value> {
    fun getKey(item: Value): Key
    fun loadAfter(currentNewestKey: Key, pageSize: Int): List<Value>
    fun loadBefore(currentOldestKey: Key, pageSize: Int): List<Value>
    fun loadInitial(pageSize: Int): List<Value>
}