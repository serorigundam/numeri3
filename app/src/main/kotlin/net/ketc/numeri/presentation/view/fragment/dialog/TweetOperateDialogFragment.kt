package net.ketc.numeri.presentation.view.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AppCompatActivity
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.fragment.dialog.TweetOperateDialogPresenter
import net.ketc.numeri.presentation.view.component.TweetOperatorDialogFactory
import net.ketc.numeri.presentation.view.fragment.FragmentInterface
import net.ketc.numeri.util.android.parent

class TweetOperateDialogFragment : BottomSheetDialogFragment(), TweetOperateDialogFragmentInterface {
    override val activity: AppCompatActivity
        get() = parent
    val presenter = TweetOperateDialogPresenter(this)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tweet = (arguments.getSerializable(EXTRA_TWEET) as? Tweet) ?: throw IllegalStateException()
        val client = (arguments.getSerializable(EXTRA_CLIENT) as?TwitterClient) ?: throw IllegalStateException()
        val dialogFactory = TweetOperatorDialogFactory(context, tweet, presenter, { presenter.onError(it) })
        return dialogFactory.create(client)
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun dismiss() {
        presenter.onDestroyView()
        super.dismiss()
    }

    companion object {
        private val EXTRA_TWEET = "EXTRA_TWEET"
        private val EXTRA_CLIENT = "EXTRA_CLIENT"
        fun create(tweet: Tweet, client: TwitterClient) = TweetOperateDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_TWEET, tweet)
                putSerializable(EXTRA_CLIENT, client)
            }
        }
    }
}

interface TweetOperateDialogFragmentInterface : FragmentInterface