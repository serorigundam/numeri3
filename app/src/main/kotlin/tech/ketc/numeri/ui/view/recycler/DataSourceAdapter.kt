package tech.ketc.numeri.ui.view.recycler

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.ui.enableRippleEffect
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask


@Suppress("UNCHECKED_CAST")
abstract class DataSourceAdapter
<Key, out Value, in VH : RecyclerView.ViewHolder>(protected val owner: LifecycleOwner,
                                                  private val dataSource: DataSource<Key, Value>,
                                                  private val creator: () -> VH)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_PROGRESS = 0
        val TYPE_VALUE = 1
    }

    var pageSize = 10

    private val values = ArrayList<Value>()

    private val itemCountInternal: Int
        get() = values.size

    private val currentLatestPosition: Int
        get() = itemCountInternal - 1

    private var isProgress = false

    private fun checkProgress() {
        if (isProgress) throw IllegalStateException()
    }

    fun loadAfter(complete: () -> Unit = {}) {
        checkProgress()
        isProgress = true
        val item = values.firstOrNull()
        if (item == null) loadInitial(complete)
        else BindingLifecycleAsyncTask {
            dataSource.loadAfter(dataSource.getKey(item), pageSize)
        }.run(owner) {
            it.ifPresent {
                values.addAll(0, it)
                notifyItemRangeInserted(0, it.size)
            }
            isProgress = false
            complete()
        }
    }

    fun loadBefore(complete: () -> Unit = {}) {
        checkProgress()
        isProgress = true
        val item = values.lastOrNull()
        if (item == null) loadInitial(complete)
        else BindingLifecycleAsyncTask {
            dataSource.loadBefore(dataSource.getKey(item), pageSize)
        }.run(owner) {
            it.ifPresent {
                val last = values.lastIndex
                values.addAll(values.size, it)
                notifyItemRangeInserted(last, it.size)
            }
            isProgress = false
            complete()
        }
    }

    fun loadInitial(complete: () -> Unit = {}) {
        checkProgress()
        isProgress = true
        BindingLifecycleAsyncTask {
            dataSource.loadInitial(pageSize)
        }.run(owner) {
            it.ifPresent {
                values.addAll(it)
                notifyDataSetChanged()
            }
            isProgress = false
            complete()
        }
    }

    override fun getItemCount(): Int {
        return itemCountInternal + if (values.size > 1) 1 else 0
    }

    protected fun getItem(position: Int) = values[position]


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position <= currentLatestPosition) {
            onBindValueViewHolder(holder as VH, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > currentLatestPosition) {
            TYPE_PROGRESS
        } else {
            TYPE_VALUE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PROGRESS -> ProgressViewHolder(parent.context).also { holder ->
                holder.itemView.setOnClickListener {
                    holder.change(true)
                    loadBefore { holder.change(false) }
                }
            }
            TYPE_VALUE -> creator()
            else -> throw RuntimeException("unknown viewType $viewType")
        }
    }

    abstract fun onBindValueViewHolder(holder: VH, position: Int)

    class ProgressViewHolder(context: Context) : RecyclerView.ViewHolder(context.relativeLayout {
        lparams(matchParent, wrapContent)
        textView {
            id = R.id.read_more_text
            textSize = 16F
            text = context.getString(R.string.read_more)
            gravity = Gravity.CENTER
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }

        progressBar {
            id = R.id.progress_bar
            visibility = View.INVISIBLE
        }.lparams(dimen(R.dimen.progress_bar_size),
                dimen(R.dimen.progress_bar_size)) {
            centerInParent()
            scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        }
    }) {
        private val text: TextView
        private val progress: ProgressBar

        init {
            itemView.enableRippleEffect()
            text = itemView.findViewById(R.id.read_more_text)
            progress = itemView.findViewById(R.id.progress_bar)
        }

        fun change(isProgress: Boolean) {
            if (isProgress) {
                text.visibility = View.INVISIBLE
                progress.visibility = View.VISIBLE
                itemView.isClickable = false
            } else {
                text.visibility = View.VISIBLE
                progress.visibility = View.INVISIBLE
                itemView.isClickable = true
            }
        }
    }
}