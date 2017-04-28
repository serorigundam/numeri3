package net.ketc.numeri.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.github.chrisbanes.photoview.PhotoView
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.MediaEntity
import net.ketc.numeri.domain.model.MediaType
import net.ketc.numeri.presentation.presenter.fragment.media.ImageMediaPresenter
import net.ketc.numeri.presentation.view.activity.MediaActivityInterface
import net.ketc.numeri.presentation.view.component.photoView
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.parent
import net.ketc.numeri.util.android.save
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.rx.singleTask
import org.jetbrains.anko.*

class ImageMediaFragment : ApplicationFragment<ImageMediaPresenter>(), ImageMediaFragmentInterface {

    override val activity: AppCompatActivity
        get() = parent
    override val presenter: ImageMediaPresenter = ImageMediaPresenter(this)
    override val mediaEntity: MediaEntity by lazy { arguments.getSerializable(EXTRA_MEDIA_ENTITY) as MediaEntity }

    private lateinit var photo: PhotoView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createView(context)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.initialize()
        photo.download(mediaEntity.url + ":orig", presenter, false,
                success = { progressBar.visibility = View.GONE })

        fun toggle() {
            (parent as? MediaActivityInterface)?.let(MediaActivityInterface::toggle)
        }
        photo.setOnPhotoTapListener { _, _, _ -> toggle() }
        photo.setOnOutsidePhotoTapListener { toggle() }
    }

    private fun createView(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, matchParent)
        fitsSystemWindows = true
        photoView {
            photo = this
            setZoomable(true)
        }.lparams(matchParent, matchParent)
        progressBar {
            progressBar = this
            visibility = View.VISIBLE
        }.lparams(dip(64), dip(64)) {
            centerInParent()
            scrollBarStyle = R.style.ProgressBar
        }
    }

    override fun saveImage() {
        singleTask(MySchedulers.io, autoDisposable = presenter) {
            photo.save()
        } error {
            parent.toast(getString(R.string.save_failure))
            it.printStackTrace()
        } success {
            parent.toast(getString(R.string.save_complete))
        }
    }

    companion object {
        val EXTRA_MEDIA_ENTITY = "EXTRA_MEDIA_ENTITY"
        fun create(mediaEntity: MediaEntity): Fragment {
            if (mediaEntity.type != MediaType.PHOTO)
                throw IllegalArgumentException("mediaEntity is must be of photo type")
            return ImageMediaFragment().apply {
                arguments = bundleOf(EXTRA_MEDIA_ENTITY to mediaEntity)
            }
        }
    }
}

interface ImageMediaFragmentInterface : FragmentInterface {
    val mediaEntity: MediaEntity
    fun saveImage()
}

