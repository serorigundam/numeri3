package tech.ketc.numeri.ui.view.recycler.users

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.graphics.drawable.Animatable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.Job
import org.jetbrains.anko.backgroundColor
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.ClientUserRelation
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserRelation
import tech.ketc.numeri.domain.twitter.model.getIconUrl
import tech.ketc.numeri.ui.activity.user.UserInfoActivity
import tech.ketc.numeri.ui.components.IUserUIComponent
import tech.ketc.numeri.ui.components.UserUIComponent
import tech.ketc.numeri.ui.model.delegate.IFriendshipHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.android.ui.enableRippleEffect
import tech.ketc.numeri.util.android.ui.recycler.ProgressViewHolder
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.coroutine.dispose
import tech.ketc.numeri.util.twitter4j.showTwitterError
import java.lang.ref.WeakReference

class UsersRecyclerAdapter(private val mCreator: () -> UserViewHolder,
                           private val mProgressViewHolderCreator: () -> ProgressViewHolder)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mValues = ArrayList<ClientUserRelation>()


    companion object {
        private val TYPE_PROGRESS = 100
        private val TYPE_USER = 200
    }

    fun addAll(values: List<ClientUserRelation>) {
        mValues.addAll(values)
        notifyItemRangeInserted(mValues.size, values.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_USER -> mCreator()
        TYPE_PROGRESS -> mProgressViewHolderCreator()
        else -> throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is UserViewHolder -> holder.bind(mValues[position])
        is ProgressViewHolder -> {
        }
        else -> throw IllegalArgumentException()
    }

    override fun getItemCount(): Int = mValues.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position <= mValues.lastIndex)
            TYPE_USER
        else
            TYPE_PROGRESS
    }

    class UserViewHolder(private val mContext: Context,
                         private val mOwner: LifecycleOwner,
                         private val mImageLoadable: IImageLoadable,
                         private val mHandler: IFriendshipHandler,
                         private val mClient: TwitterClient,
                         component: IUserUIComponent = UserUIComponent())
        : RecyclerView.ViewHolder(component.createView(mContext)),
            IUserUIComponent by component {

        private val jobList = ArrayList<Job>()
        private var mUseOrigIcon = false

        init {
            mUseOrigIcon = mContext.pref.getBoolean(mContext.getString(R.string.pref_key_use_orig_icon), false)
            itemView.enableRippleEffect()
            val observer = createLifecycleObserver()
            mOwner.lifecycle.addObserver(observer)
            observer.vhDestroyed = {
                mOwner.lifecycle.removeObserver(observer)
            }
        }

        fun bind(clientUserRelation: ClientUserRelation) {
            jobList.forEach(Job::dispose)
            followButton.isClickable = false
            val (user, relation) = clientUserRelation
            userNameText.text = user.name
            screenNameText.text = user.screenName
            descriptionText.text = user.description
            setIcon(user)
            setRelation(relation, false)
            followButton.visibility = if (mClient.id == user.id) View.GONE else View.VISIBLE
            followButton.setOnClickListener {
                followButton.isClickable = false
                updateRelation(clientUserRelation)
            }
            itemView.setOnClickListener { UserInfoActivity.start(mContext, mClient, user.id) }
        }

        private fun setIcon(user: TwitterUser) {
            iconImage.setImageDrawable(null)
            iconBack.backgroundColor = mContext.getColor(R.color.image_background_transparency)
            bindLaunch(mOwner) {
                val (bitmap, _) = mImageLoadable.loadImage(user.getIconUrl(true), false).await().nullable()
                        ?: return@bindLaunch
                iconBack.backgroundColor = mContext.getColor(R.color.transparent)
                iconImage.setImageBitmap(bitmap)
                iconImage.fadeIn()
            }.also { jobList.add(it) }
        }

        private fun setRelation(relation: UserRelation, animate: Boolean) {
            val drawableId = if (relation.isFollowing) {
                if (animate) R.drawable.vector_anim_person
                else R.drawable.ic_person_white_24dp
            } else {
                if (animate) R.drawable.vector_anim_person_add
                else R.drawable.ic_person_add_white_24dp
            }
            val drawable = itemView.context.getDrawable(drawableId)
            followButton.setImageDrawable(drawable)
            (drawable as? Animatable)?.start()
            followButton.isClickable = true
        }

        private fun updateRelation(relation: ClientUserRelation) {
            bindLaunch(mOwner) {
                mHandler.updateFriendship(mClient, relation).await().orError {
                    mContext.showTwitterError(it)
                } ?: return@bindLaunch
                setRelation(relation.relation, true)
            }.also { jobList.add(it) }
        }

        private fun createLifecycleObserver() = object : LifecycleObserver {
            val ref = WeakReference<UserViewHolder>(this@UserViewHolder)
            var vhDestroyed = {}
            private fun dispose() {
                vhDestroyed()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                ref.get()?.let { holder ->
                    holder.mUseOrigIcon = mContext.pref.getBoolean(mContext.getString(R.string.pref_key_use_orig_icon), false)
                } ?: dispose()
            }
        }
    }
}