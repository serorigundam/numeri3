package tech.ketc.numeri.ui.fragment.users

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.ui.components.IRecyclerUIComponent
import tech.ketc.numeri.ui.components.RecyclerUIComponent
import tech.ketc.numeri.ui.model.UsersViewModel
import tech.ketc.numeri.ui.view.recycler.users.UsersRecyclerAdapter
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.android.ui.recycler.ProgressViewHolder
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class UsersFragment : Fragment(), AutoInject, IRecyclerUIComponent by RecyclerUIComponent() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: UsersViewModel by viewModel { mViewModelFactory }
    private val mInfo by lazy { arg.getSerializable(EXTRA_INFO) as Info }
    private val mAdapter by lazy { UsersRecyclerAdapter(this::createViewHolder, this::createProgressViewHolder) }
    private val mClient: TwitterClient
        get() = mInfo.client
    private val mTargetId: Long
        get() = mInfo.targetId
    private var mNextCursor = -1L
    private var mSavedAdapterPosition: Int? = null


    companion object {
        private val EXTRA_INFO = "EXTRA_INFO"
        private val SAVED_NEXT_CURSOR = "SAVED_NEXT_CURSOR"
        private val SAVED_CURRENT_ADAPTER_POSITION = "SAVED_CURRENT_ADAPTER_POSITION"

        fun create(client: TwitterClient, targetId: Long, type: UsersViewModel.Type) = UsersFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_INFO, Info(client, targetId, type))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(ctx)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.run(this::restore)
        initialize()
    }

    private fun initialize() {
        mModel.setType(mInfo.type)
        recycler.adapter = mAdapter
        mModel.storedRelations.takeIf { it.isNotEmpty() }?.let {
            mAdapter.addAll(it)
            mSavedAdapterPosition?.let {
                recycler.scrollToPosition(it)
            }
        } ?: bindLaunch { next() }
    }

    private suspend fun next() {
        val (relations, cursor) = mModel.getClientUserRelationList(mClient, mTargetId, mNextCursor).await().orError {
            toast(R.string.message_failed_get_users)
        } ?: return
        mAdapter.addAll(relations)
        mNextCursor = cursor
    }

    private fun createViewHolder(): UsersRecyclerAdapter.UserViewHolder {
        return UsersRecyclerAdapter.UserViewHolder(ctx, this, mModel, mModel, mClient)
    }

    private fun createProgressViewHolder() = ProgressViewHolder(ctx).apply {
        itemView.setOnClickListener {
            setProgress(true)
            bindLaunch {
                next()
                setProgress(false)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(SAVED_NEXT_CURSOR, mNextCursor)
        if (mAdapter.itemCount > 0) {
            val topChild = recycler.getChildAt(0)
            val position = recycler.getChildAdapterPosition(topChild)
            outState.putInt(SAVED_CURRENT_ADAPTER_POSITION, position)
        } else {
            outState.putInt(SAVED_CURRENT_ADAPTER_POSITION, -1)
        }
        super.onSaveInstanceState(outState)
    }

    private fun restore(savedInstanceState: Bundle) {
        mNextCursor = savedInstanceState.getLong(SAVED_NEXT_CURSOR)
        mSavedAdapterPosition = savedInstanceState.getInt(SAVED_CURRENT_ADAPTER_POSITION)
                .takeIf { it != -1 }
    }

    data class Info(val client: TwitterClient, val targetId: Long, val type: UsersViewModel.Type) : Serializable
}