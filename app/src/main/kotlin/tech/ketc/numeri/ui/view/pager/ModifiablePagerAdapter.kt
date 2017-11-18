package tech.ketc.numeri.ui.view.pager

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import tech.ketc.numeri.util.Logger
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
class ModifiablePagerAdapter<ID : Serializable, F : Fragment>(private val mFm: FragmentManager)
    : FragmentStatePagerAdapter(mFm) {
    private val mContents = ArrayList<Content<ID, F>>()

    private var mPreviousContents = ArrayList<Content<ID, F>>()

    override fun getItem(position: Int): Fragment = mContents[position].fragment

    fun getContent(position: Int): Content<ID, F> = mContents[position]

    override fun getCount(): Int = mContents.count()

    override fun getPageTitle(position: Int) = mContents[position].name

    fun setContents(contents: List<Content<ID, F>>) {
        mPreviousContents = mContents
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
            val names = ArrayList<Pair<Int, String>>()
            keys.forEach {
                when {
                    it.startsWith("f") -> {
                        val index = Integer.parseInt(it.substring(1))
                        val fragment = mFm.getFragment(bundle, it) as? F
                        fragment?.let { fragments.add(index to it) }
                    }
                    it.startsWith("i") -> {
                        val index = Integer.parseInt(it.substring(1))
                        val id = bundle.getSerializable(it) as? ID
                        id?.let { ids.add(index to it) }
                    }
                    it.startsWith("n") -> {
                        val index = Integer.parseInt(it.substring(1))
                        val name = bundle.getString(it)
                        name?.let { names.add(index to it) }
                    }
                }
            }
            val size = ids.size
            if (fragments.size != size) throw InternalError()
            (0..(size - 1)).mapTo(mContents) { Content(ids[it].second, fragments[it].second, names[it].second) }
            mPreviousContents.addAll(mContents)
            Logger.v(javaClass.name, "restoreState")
            Logger.v(javaClass.name, mContents.joinToString(",") { "${it.id} ${it.fragment} ${it.name}" })
            notifyDataSetChanged()
        }
    }

    override fun saveState(): Parcelable? {
        val state = super.saveState() as? Bundle ?: return null
        mContents.forEachIndexed { i, (id, _, name) ->
            state.putSerializable("i$i", id)
            state.putString("n$i", name)
        }
        Logger.v(javaClass.name, "saveState")
        return state
    }

    override fun getItemPosition(`object`: Any): Int {
        val nonChange = mContents.any { content ->
            content.fragment.id == (`object` as Fragment).id
                    && mContents.indexOf(content) == mPreviousContents.indexOf(content)
        }
        return if (nonChange) PagerAdapter.POSITION_UNCHANGED else PagerAdapter.POSITION_NONE
    }

    data class Content<out ID : Serializable, out F : Fragment>(val id: ID, val fragment: F, val name: String)
}