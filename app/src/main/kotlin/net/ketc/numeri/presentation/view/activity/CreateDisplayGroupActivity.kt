package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import net.ketc.numeri.presentation.presenter.activity.CreateDisplayGroupPresenter
import net.ketc.numeri.presentation.view.activity.ui.CreateDisplayGroupActivityUI
import net.ketc.numeri.presentation.view.activity.ui.ICreateDisplayGroupActivityUI
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class CreateDisplayGroupActivity : ApplicationActivity<CreateDisplayGroupPresenter>(),
        CreateDisplayGroupActivityInterface,
        ICreateDisplayGroupActivityUI by CreateDisplayGroupActivityUI() {
    override val ctx: Context = this
    override val presenter: CreateDisplayGroupPresenter = CreateDisplayGroupPresenter(this)
    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        presenter.initialize(savedInstanceState)
        groupNameEdit.setOnEditorActionListener { _, actionId, _ -> done(actionId) }
    }

    private fun done(actionId: Int): Boolean = if (!done) {
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                presenter.addGroup(groupNameEdit.text.toString())
                finish()
                true
            }
            else -> false
        }
    } else false

    companion object {
        fun start(context: Context) {
            context.startActivity<CreateDisplayGroupActivity>()
        }
    }
}

interface CreateDisplayGroupActivityInterface : ActivityInterface