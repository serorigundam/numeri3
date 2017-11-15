package tech.ketc.numeri.ui.fragment.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.jetbrains.anko.support.v4.ctx
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg

class MessageDialogFragment : DialogFragment() {
    private val mMessageId by lazy { arg.getInt(EXTRA_MESSAGE) }
    private val mPositiveId by lazy { arg.getInt(EXTRA_POSITIVE) }
    private val mNegativeId by lazy { arg.getInt(EXTRA_NEGATIVE) }
    private val mRequestCode by lazy { arg.getInt(EXTRA_REQUEST_CODE) }

    companion object {
        private val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        private val EXTRA_POSITIVE = "EXTRA_POSITIVE"
        private val EXTRA_NEGATIVE = "EXTRA_NEGATIVE"
        fun create(requestCode: Int, @StringRes messageId: Int, @StringRes positiveId: Int = R.string.yes, @StringRes negativeId: Int = R.string.cancel) = MessageDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_MESSAGE, messageId)
                putInt(EXTRA_POSITIVE, positiveId)
                putInt(EXTRA_NEGATIVE, negativeId)
                putInt(EXTRA_REQUEST_CODE, requestCode)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        fun listener(id: Int) = DialogInterface.OnClickListener { _, _ ->
            (act as? OnDialogItemSelectedListener)?.onDialogItemSelected(mRequestCode, id)
            if (targetFragment != null) {
                (targetFragment as? OnDialogItemSelectedListener
                        ?: throw IllegalStateException()).onDialogItemSelected(mRequestCode, id)
            }
        }
        return AlertDialog.Builder(ctx).setMessage(mMessageId)
                .setPositiveButton(mPositiveId, listener(mPositiveId))
                .setNegativeButton(mNegativeId, listener(mNegativeId))
                .create()
    }
}