package net.ketc.numeri.presentation.view.component

import android.graphics.drawable.Animatable
import android.view.View
import io.reactivex.disposables.Disposable
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.RelationType
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.component.TwitterUserViewPresenter
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreViewHolder
import net.ketc.numeri.presentation.view.component.ui.ITwitterUserViewUI
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.fadeIn
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import org.jetbrains.anko.backgroundResource


class UserViewHolder(ui: ITwitterUserViewUI, override val autoDisposable: AutoDisposable, client: TwitterClient, onClick: (Pair<TwitterUser, UserRelation?>) -> Unit)
    : ReadableMoreViewHolder<Pair<TwitterUser, UserRelation?>>(ui, onClick), ITwitterUserViewUI by ui {
    private val presenter = TwitterUserViewPresenter(this, autoDisposable, client)
    private var previousLoadImageDisposable: Disposable? = null

    init {
        itemView.backgroundResource = ctx.getResourceId(android.R.attr.selectableItemBackground)
        itemView.isClickable = true
    }

    override fun bind(value: Pair<TwitterUser, UserRelation?>) {
        itemView.setOnClickListener { onClick(value) }
        val user = value.first
        val relation = value.second
        previousLoadImageDisposable?.takeUnless { it.isDisposed }?.dispose()
        previousLoadImageDisposable = iconImage.download(user.iconUrl, autoDisposable,
                success = { iconImage.fadeIn().execute() })
        screenNameText.text = user.screenName
        userNameText.text = user.name
        descriptionText.text = user.description
        setState(relation, true)
        followButton.setOnClickListener { relation?.let { presenter.updateState(it) } }
    }

    fun setState(userRelation: UserRelation?, onBind: Boolean = false) {
        if (userRelation == null) {
            followButton.visibility = View.INVISIBLE
            return
        } else {
            followButton.visibility = View.VISIBLE
        }
        val drawableId = when (userRelation.type) {
            RelationType.NOTHING, RelationType.FOLLOWED -> {
                if (onBind) R.drawable.ic_person_add_white_24dp
                else R.drawable.vector_anim_person_add
            }
            RelationType.FOLLOWING, RelationType.MUTUAL -> {
                if (onBind) R.drawable.ic_person_white_24dp
                else R.drawable.vector_anim_person
            }
            else -> {
                followButton.visibility = View.INVISIBLE
                null
            }
        }
        drawableId?.let {
            val drawable = ctx.getDrawable(it)
            followButton.setImageDrawable(drawable)
            (drawable as? Animatable)?.start()
        }
    }
}