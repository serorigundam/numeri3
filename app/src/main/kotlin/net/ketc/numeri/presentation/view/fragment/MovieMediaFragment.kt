package net.ketc.numeri.presentation.view.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.MediaEntity
import net.ketc.numeri.domain.model.MediaType
import net.ketc.numeri.presentation.presenter.fragment.media.MovieMediaPresenter
import net.ketc.numeri.presentation.view.activity.MediaActivityInterface
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.fadeOut
import net.ketc.numeri.util.android.parent
import org.jetbrains.anko.*

class MovieMediaFragment : ApplicationFragment<MovieMediaPresenter>(), MovieMediaFragmentInterface {
    override val activity: AppCompatActivity by lazy { parent }
    override val presenter: MovieMediaPresenter = MovieMediaPresenter(this)
    private lateinit var video: VideoView
    val mediaEntity: MediaEntity by lazy { arguments.getSerializable(ImageMediaFragment.EXTRA_MEDIA_ENTITY) as MediaEntity }
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createView(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener { (parent as? MediaActivityInterface)?.toggle() }
        initializeVideo()
    }

    private fun initializeVideo() {
        val variants = mediaEntity.variants
        val variant = variants.filter { it.bitrate > 0 }
                .minBy { it.bitrate } ?: variants[0]

        fun thumbLoaded() {
            progressBar.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener {
                it.fadeOut().end { visibility = View.GONE }.execute()
                thumbImage.fadeOut().end { visibility = View.GONE }.execute()
                progressBar.visibility = View.VISIBLE

                val uri = Uri.parse(variant.url)
                video.setOnPreparedListener {
                    progressBar.visibility = View.GONE
                    it.isLooping = true
                    val mediaController = MediaController(context)
                    video.setMediaController(mediaController)
                    video.start()
                }
                video.setVideoURI(uri)
            }
        }

        thumbImage.download(mediaEntity.url, presenter, false,
                error = { thumbLoaded() },
                success = { thumbLoaded() })
    }

    private lateinit var thumbImage: ImageView
    private lateinit var playButton: ImageButton

    private fun createView(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, matchParent)
        videoView {
            video = this
            backgroundColor = ctx.getColor(R.color.transparent)
        }.lparams(matchParent, wrapContent) {
            centerVertically()
        }

        imageView {
            thumbImage = this
            backgroundColor = ctx.getColor(R.color.transparent)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }.lparams(matchParent, matchParent)

        imageButton {
            playButton = this
            background = ctx.getDrawable(R.drawable.ripple_transparent)
            image = ctx.getDrawable(R.drawable.ic_play_circle_outline_white_48dp)
            visibility = View.GONE
        }.lparams(dip(64), dip(64)) {
            centerInParent()
        }

        progressBar {
            progressBar = this
            visibility = View.VISIBLE
        }.lparams(dip(64), dip(64)) {
            centerInParent()
            scrollBarStyle = R.style.ProgressBar
        }
    }

    companion object {
        private val EXTRA_MEDIA_ENTITY = "EXTRA_MEDIA_ENTITY"
        fun create(mediaEntity: MediaEntity): MovieMediaFragment {
            if (mediaEntity.type == MediaType.PHOTO)
                throw IllegalArgumentException("mediaEntity is must be not of photo type")
            return MovieMediaFragment().apply {
                arguments = bundleOf(EXTRA_MEDIA_ENTITY to mediaEntity)
            }
        }
    }
}

interface MovieMediaFragmentInterface : FragmentInterface