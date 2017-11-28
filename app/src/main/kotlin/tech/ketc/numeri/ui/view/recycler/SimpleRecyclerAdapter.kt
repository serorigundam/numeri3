package tech.ketc.numeri.ui.view.recycler

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class SimpleRecyclerAdapter<VH : RecyclerView.ViewHolder, Value>(private val creator: () -> VH,
                                                                 private val binder: (holder: VH, position: Int) -> Unit) : RecyclerView.Adapter<VH>() {

    val values = ArrayList<Value>()

    override fun onBindViewHolder(holder: VH, position: Int) = binder(holder, position)

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = creator()

}