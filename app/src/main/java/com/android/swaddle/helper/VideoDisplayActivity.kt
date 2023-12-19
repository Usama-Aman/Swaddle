package com.android.swaddle.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityLoginBinding
import com.android.swaddle.databinding.ActivityVideoDisplayBinding
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.io.IOException


class VideoDisplayActivity : BaseActivity(), Player.EventListener,
    ExtractorMediaSource.EventListener {

    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var topBar: RelativeLayout
    private lateinit var videoView: PlayerView
    private lateinit var imageMedia: PhotoView
    private lateinit var rlRetake: RelativeLayout
    private lateinit var rlSend: RelativeLayout
    private lateinit var content: RelativeLayout

    private var player: SimpleExoPlayer? = null
    private var mediaSource: MediaSource? = null
    private var simpleExoPlayerView: PlayerView? = null

    private var path = ""
    private var type = ""

    private lateinit var binding: ActivityVideoDisplayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setFullScreen()
        setNoLayoutLimits()

        path = intent?.extras?.getString("path", "") ?: ""
        type = intent?.extras?.getString("type", "") ?: ""

        initIds()
    }

    private fun buildMediaSourceNew(uri: Uri): MediaSource? {

        Log.e("app ame", applicationInfo.name)

        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(
                this@VideoDisplayActivity,
                Util.getUserAgent(this@VideoDisplayActivity, applicationInfo.name)
            )
        return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun initIds() {

        ivBack = binding.ivBack
        topBar = binding.topBar
        videoView = binding.videoView
        tvTitle = binding.tvTitle
        imageMedia = binding.imageMedia
        rlRetake = binding.rlRetake
        rlSend = binding.rlSend
        content = binding.content


        if (type == Constants.video) {
            imageMedia.viewGone()
            videoView.viewVisible()
            tvTitle.text = "Proceed Video"
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val controller = DefaultLoadControl()
            val extractor = DefaultExtractorsFactory()
            val renderer = DefaultRenderersFactory(this@VideoDisplayActivity)

            mediaSource = buildMediaSourceNew(Uri.parse(path))

            simpleExoPlayerView = binding.videoView
            player = ExoPlayerFactory.newSimpleInstance(
                this@VideoDisplayActivity,
                renderer,
                trackSelector,
                controller
            )

            player?.addListener(this@VideoDisplayActivity)
            simpleExoPlayerView?.player = player
            player?.prepare(mediaSource as ExtractorMediaSource, true, true)
            player?.playWhenReady = true
            player?.seekTo(0, 0)
            player?.repeatMode = Player.REPEAT_MODE_ONE
        } else {
            tvTitle.text = "Proceed Image"
            imageMedia.viewVisible()
            videoView.viewGone()
            Glide.with(this).load(path)
//                .placeholder(R.drawable.)
                .into(imageMedia)
        }

        clickListeners()
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayerView?.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayerView?.onPause()
        player?.playWhenReady = false
        //        player?.stop(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
    }

    private fun clickListeners() {
        ivBack.setOnClickListener {
            finish()
        }
        rlRetake.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("path", path)
            resultIntent.putExtra("type", type)
            resultIntent.putExtra("retry", true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        rlSend.setOnClickListener {

            val resultIntent = Intent()
            resultIntent.putExtra("path", path)
            resultIntent.putExtra("type", type)
            resultIntent.putExtra("retry", false)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }


    override fun onLoadError(error: IOException?) {
        error?.printStackTrace()
        hideLoading()
    }


}
