package net.ketc.numeri.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.fragment.user.display.FollowersPresenter
import net.ketc.numeri.presentation.presenter.fragment.user.display.FriendsPresenter
import net.ketc.numeri.presentation.presenter.fragment.user.display.UsersPresenter
import net.ketc.numeri.presentation.view.SimplePagerContent
import net.ketc.numeri.presentation.view.activity.UserInfoActivity
import net.ketc.numeri.presentation.view.component.UserViewHolder
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreRecyclerAdapter
import net.ketc.numeri.presentation.view.component.ui.TwitterUserViewUI
import net.ketc.numeri.util.android.defaultInit
import net.ketc.numeri.util.android.parent
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.find


class UsersFragment : ApplicationFragment<UsersPresenter>(), UsersFragmentInterface, SimplePagerContent {
    override val contentName: String by lazy { typeToName() }
    override val activity: AppCompatActivity
        get() = parent
    override val clientId: Long by lazy { arguments!!.getLong(EXTRA_CLIENT_ID) }
    override val targetUserId: Long by lazy { arguments!!.getLong(EXTRA_TARGET_USER_ID) }
    override val type: UsersFragmentInterface.Type by lazy {
        UsersFragmentInterface.Type.values()
                .first { it.typeStr == arguments!!.getString(EXTRA_TYPE) }
    }
    override val presenter: UsersPresenter by lazy { createPresenter() }

    private var mReadableMoreAdapter: ReadableMoreRecyclerAdapter<Pair<TwitterUser, UserRelation?>>? = null
    private val readableMoreAdapter: ReadableMoreRecyclerAdapter<Pair<TwitterUser, UserRelation?>>
        get() = mReadableMoreAdapter ?: throw IllegalStateException("TwitterClient is not set")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createView(context!!)
    }

    private fun typeToName(): String {
        return when (type) {UsersFragmentInterface.Type.FRIENDS -> "フォロー"
            UsersFragmentInterface.Type.FOLLOWERS -> "フォロワー"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        find<RecyclerView>(R.id.users_recycler).defaultInit()
        presenter.initialize()
    }


    private fun createPresenter() = when (type) {
        UsersFragmentInterface.Type.FOLLOWERS -> {
            FollowersPresenter(this)
        }
        UsersFragmentInterface.Type.FRIENDS -> {
            FriendsPresenter(this)
        }
    }

    override fun setClient(client: TwitterClient) {
        val recycler = find<RecyclerView>(R.id.users_recycler)
        mReadableMoreAdapter = ReadableMoreRecyclerAdapter(presenter, {
            UserViewHolder(TwitterUserViewUI(context!!), presenter, client, { UserInfoActivity.start(context!!, clientId, targetUserId) })
        }, presenter)
        recycler.adapter = readableMoreAdapter
    }

    override fun setUserRelationPairList(list: List<Pair<TwitterUser, UserRelation?>>) {
        val itemCount = readableMoreAdapter.itemCount
        val first = itemCount == 0
        readableMoreAdapter.addAll(list)
        if (first) {
            readableMoreAdapter.isReadMoreEnabled = true
        }
    }


    companion object {
        val EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID"
        val EXTRA_TARGET_USER_ID = "EXTRA_TARGET_USER_ID"
        val EXTRA_TYPE = "EXTRA_TYPE"
        fun create(clientId: Long, targetUserId: Long, type: UsersFragmentInterface.Type) = UsersFragment().apply {
            arguments = Bundle().apply {
                putLong(EXTRA_CLIENT_ID, clientId)
                putLong(EXTRA_TARGET_USER_ID, targetUserId)
                putString(EXTRA_TYPE, type.name)
            }
        }

        private fun createView(ctx: Context) = ctx.recyclerView {
            lparams(matchParent, matchParent)
            id = R.id.users_recycler
            isVerticalScrollBarEnabled = true
        }
    }
}

interface UsersFragmentInterface : FragmentInterface {
    val clientId: Long
    val targetUserId: Long
    val type: Type

    fun setUserRelationPairList(list: List<Pair<TwitterUser, UserRelation?>>)
    fun setClient(client: TwitterClient)
    enum class Type(val typeStr: String) {
        FRIENDS("FRIENDS"), FOLLOWERS("FOLLOWERS")
    }
}