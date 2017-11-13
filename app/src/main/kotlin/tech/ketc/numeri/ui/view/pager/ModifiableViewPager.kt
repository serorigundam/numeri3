package tech.ketc.numeri.ui.view.pager

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
class ModifiableViewPager<in ID : Serializable, in F : Fragment>(private val fm: FragmentManager)
    : FragmentStatePagerAdapter(fm) {
    private val mContents = ArrayList<Content<ID, F>>()

    private var previousContents = ArrayList<Content<ID, F>>()

    override fun getItem(position: Int): Fragment = mContents[position].fragment

    override fun getCount(): Int = mContents.count()

    fun setContents(contents: List<Content<ID, F>>) {
        previousContents = mContents
        val diffSize = contents.size != mContents.size
        var isChange = diffSize
        if (!diffSize) {
            mContents.forEachIndexed { i, (id, _) ->
                if (contents[i].id != id) {
                    isChange = true
                    return@forEachIndexed
                }
            }
        }
        if (!isChange) return
        mContents.clear()
        mContents.addAll(contents)
        notifyDataSetChanged()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        super.restoreState(state, loader)
        if (state != null) {
            val bundle = state as Bundle
            val keys = bundle.keySet()
            val fragments = ArrayList<Pair<Int, F>>()
            val ids = ArrayList<Pair<Int, ID>>()
            keys.forEach {
                if (it.startsWith("f")) {
                    val index = Integer.parseInt(it.substring(1))
                    val fragment = fm.getFragment(bundle, it) as? F
                    fragment?.let { f -> fragments.add(index to f) }
                    notifyDataSetChanged()
                } else if (it.startsWith("i")) {
                    val index = Integer.parseInt(it.substring(1))
                    val id = bundle.getSerializable(it) as? ID
                    id?.let { ids.add(index to id) }
                }
            }
            val size = ids.size
            if (fragments.size != size) throw InternalError()
            (0..(size - 1)).mapTo(mContents) { Content(ids[it].second, fragments[it].second) }
            notifyDataSetChanged()
        }
    }

    override fun saveState(): Parcelable {
        val state = super.saveState() as? Bundle ?: throw InternalError()
        mContents.forEachIndexed { i, (id, _) ->
            state.putSerializable("i$i", id)
        }
        return state
    }

    override fun getItemPosition(`object`: Any): Int {
        val nonChange = mContents.any { content ->
            content.fragment == `object`
                    && mContents.indexOf(content) == previousContents.indexOf(content)
        }
        return if (nonChange) PagerAdapter.POSITION_UNCHANGED else PagerAdapter.POSITION_NONE
    }

    data class Content<out ID : Serializable, out F : Fragment>(val id: ID, val fragment: F)
}