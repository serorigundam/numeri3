package tech.ketc.numeri.ui.view.recycler.users

import android.content.Context
import android.graphics.drawable.Animatable
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.model.ClientUserRelation
import tech.ketc.numeri.domain.twitter.model.UserRelation
import tech.ketc.numeri.ui.components.IUserUIComponent
import tech.ketc.numeri.ui.components.UserUIComponent
import tech.ketc.numeri.util.android.ui.enableRippleEffect
import tech.ketc.numeri.util.android.ui.recycler.ProgressViewHolder

class UsersRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        TYPE_USER -> UserViewHolder(parent.context)
        TYPE_PROGRESS -> ProgressViewHolder(parent.context)
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

    class UserViewHolder(ctx: Context, component: IUserUIComponent = UserUIComponent())
        : RecyclerView.ViewHolder(component.createView(ctx)),
            IUserUIComponent by component {
        init {
            itemView.enableRippleEffect()
        }

        fun bind(clientUserRelation: ClientUserRelation) {
            val (user, relation) = clientUserRelation
            userNameText.text = user.name
            screenNameText.text = user.screenName
            descriptionText.text = user.description
            setRelation(relation, true)
        }

        private fun setRelation(relation: UserRelation, animate: Boolean) {
            val drawableId = if (relation.isFollowed) {
                if (animate) R.drawable.ic_person_add_white_24dp
                else R.drawable.vector_anim_person_add
            } else {
                if (animate) R.drawable.ic_person_white_24dp
                else R.drawable.vector_anim_person
            }
            val drawable = itemView.context.getDrawable(drawableId)
            followButton.setImageDrawable(drawable)
            (drawable as? Animatable)?.start()
        }
    }
}