package tech.ketc.numeri.util.android.ui.recycler

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId
import tech.ketc.numeri.util.android.ui.enableRippleEffect

class SimpleRecyclerAdapter<Value>(private val mStrTransformer: (Value) -> String,
                                   private val mOnSItemSelected: (Value) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val values: MutableList<Value> = ArrayList()
    var isBottomEmpty: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (value) insertEmpty()
                else removeEmpty()
            }
        }

    private fun removeEmpty() {
        notifyItemRemoved(values.size)
    }

    private fun insertEmpty() {
        notifyItemInserted(values.size)
    }

    companion object {
        private val TYPE_EMPTY = 100
        private val TYPE_VALUE = 200
    }

    override fun getItemCount() = if (isBottomEmpty) values.size + 1 else values.size

    override fun getItemViewType(position: Int) = if (position <= values.lastIndex) TYPE_VALUE
    else TYPE_EMPTY

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SimpleViewHolder -> holder.apply {
                val value = values[position]
                bind(mStrTransformer(value))
                itemView.setOnClickListener { mOnSItemSelected(value) }
            }
            else -> {
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_VALUE -> SimpleViewHolder(parent.context)
        TYPE_EMPTY -> EmptyViewHolder(parent.context)
        else -> throw RuntimeException()
    }

    class SimpleViewHolder(context: Context) : RecyclerView.ViewHolder(context.relativeLayout {
        lparams(matchParent, wrapContent)
        textView {
            id = R.id.content_text
            textSize = 16F
            gravity = Gravity.CENTER
            textColor = context.getColor(context.getResourceId(android.R.attr.textColorPrimary))
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }
    }) {
        private val textView: TextView = itemView.findViewById(R.id.content_text)

        init {
            itemView.enableRippleEffect()
        }

        fun bind(displayStr: String) {
            textView.text = displayStr
        }
    }

    class EmptyViewHolder(context: Context) : RecyclerView.ViewHolder(context.frameLayout {
        lparams(matchParent, dip(88))
    })
}