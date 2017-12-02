package tech.ketc.numeri.util.android.ui.recycler

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import kotlinx.coroutines.experimental.async
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.response.response
import tech.ketc.numeri.util.copy
import tech.ketc.numeri.util.logTag
import java.lang.ref.WeakReference


@Suppress("UNCHECKED_CAST")
abstract class DataSourceAdapter
<Key, Value, in VH : RecyclerView.ViewHolder>(private val mOwner: LifecycleOwner,
                                              private val mDataSource: DataSource<Key, Value>,
                                              private val mVHCreator: () -> VH)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_PROGRESS = 0
        val TYPE_VALUE = 1
    }

    var pageSize = 10

    private val mValues = ArrayList<Value>()

    private val mItemCount: Int
        get() = mValues.size

    private val currentLatestPosition: Int
        get() = mItemCount - 1

    private var mIsProgress = false
    private var mStoreLiveData: MutableLiveData<List<Value>>? = null

    private var mProgressViewHolderRef: WeakReference<ProgressViewHolder>? = null

    /**
     * be careful about the adapter state when making changes to items
     */
    protected fun items() = mValues

    protected fun store() = mStoreLiveData

    fun setStoreLiveData(store: MutableLiveData<List<Value>>) {
        mStoreLiveData = store
    }

    fun restore(): Boolean {
        val values = mStoreLiveData?.value
        values ?: return false
        mValues.addAll(values)
        notifyDataSetChanged()
        return true
    }

    private fun checkProgress() {
        if (mIsProgress) throw IllegalStateException()
    }

    var error: (Throwable) -> Unit = {}

    fun loadAfter(complete: () -> Unit = {}) {
        checkProgress()
        mIsProgress = true
        val item = mValues.firstOrNull()
        if (item == null) loadInitial(complete)
        else bindLaunch(mOwner) {
            val listRes = async {
                response { mDataSource.loadAfter(mDataSource.getKey(item), pageSize) }
            }.await()
            val list = listRes.orError(error) ?: return@bindLaunch
            mValues.addAll(0, list)
            mStoreLiveData?.value = mValues.copy()
            notifyItemRangeInserted(0, list.size)
            complete()
            mIsProgress = false
        }
    }

    fun loadBefore(complete: () -> Unit = {}) {
        checkProgress()
        mIsProgress = true
        val item = mValues.lastOrNull()
        if (item == null) loadInitial(complete)
        else bindLaunch(mOwner) {
            val listRes = async {
                response { mDataSource.loadBefore(mDataSource.getKey(item), pageSize) }
            }.await()
            val list = listRes.orError(error) ?: return@bindLaunch
            val last = mValues.size
            mValues.addAll(list)
            mStoreLiveData?.value = mValues.copy()
            notifyItemRangeInserted(last, list.size)
            complete()
            mIsProgress = false
        }
    }

    fun loadInitial(complete: () -> Unit = {}) {
        checkProgress()
        mIsProgress = true
        bindLaunch(mOwner) {
            val listRes = async {
                response { mDataSource.loadInitial(pageSize) }
            }.await()
            val list = listRes.orError(error) ?: return@bindLaunch
            mValues.addAll(list)
            mStoreLiveData?.value = mValues.copy()
            notifyDataSetChanged()
            complete()
            mIsProgress = false
        }
    }

    override fun getItemCount(): Int {
        return mItemCount + if (mValues.size > 1) 1 else 0
    }

    protected fun getItem(position: Int) = mValues[position]


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

    private fun createOrGetProgressVH(context: Context): ProgressViewHolder {
        fun createRef(): WeakReference<ProgressViewHolder> = ProgressViewHolder(context).also { holder ->
            Logger.v(logTag, "create ProgressViewHolder")
            holder.itemView.setOnClickListener {
                holder.change(true)
                loadBefore { holder.change(false) }
            }
        }.let { WeakReference(it).also { mProgressViewHolderRef = it } }

        val ref = mProgressViewHolderRef
        return if (ref == null) {
            createRef().get()!!
        } else {
            ref.get() ?: createRef().get()!!
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PROGRESS -> createOrGetProgressVH(parent.context)
            TYPE_VALUE -> mVHCreator()
            else -> throw RuntimeException("unknown viewType $viewType")
        }
    }

    abstract fun onBindValueViewHolder(holder: VH, position: Int)
}