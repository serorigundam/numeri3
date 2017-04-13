package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import net.ketc.numeri.presentation.presenter.activity.CreateDisplayGroupPresenter
import net.ketc.numeri.presentation.view.activity.ui.CreateDisplayGroupActivityUI
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class CreateDisplayGroupActivity : ApplicationActivity<CreateDisplayGroupPresenter>(), CreateDisplayGroupActivityInterface {
    override val ctx: Context = this
    override val presenter: CreateDisplayGroupPresenter = CreateDisplayGroupPresenter(this)
    private val ui = CreateDisplayGroupActivityUI()
    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        setSupportActionBar(ui.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        presenter.initialize(savedInstanceState)
        val groupNameEdit = ui.groupNameEdit
        groupNameEdit.setOnEditorActionListener { _, actionId, _ ->
            if (!done) {
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        presenter.addGroup(groupNameEdit.text.toString())
                        finish()
                        return@setOnEditorActionListener true
                    }
                    else -> return@setOnEditorActionListener false
                }
            }
            return@setOnEditorActionListener false
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity<CreateDisplayGroupActivity>()
        }
    }
}

interface CreateDisplayGroupActivityInterface : ActivityInterface {

}