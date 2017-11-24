package tech.ketc.numeri.ui.fragment.overlay

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*

class ProgressFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = context!!.relativeLayout {
        lparams(matchParent, matchParent)
        progressBar().lparams(dip(56), dip(56)) {
            centerInParent()
            scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        }
    }
}