package com.android.swaddle.utils.slider

import android.net.Uri
import android.os.Bundle
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.BaseActivity.PlayerVUtil.Companion.cacheFile
import com.android.swaddle.databinding.ActivityLoginBinding
import com.android.swaddle.databinding.ActivityVideoDisplayBinding
import com.android.swaddle.utils.CustomToasts

import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import kotlinx.android.synthetic.main.activity_video_play.*
import java.io.IOException


class VideoPlayActivity : BaseActivity(), ExtractorMediaSource.EventListener,
    Player.EventListener {
    override fun onLoadError(error: IOException?) {
        CustomToasts.failureToast(this@VideoPlayActivity, error?.localizedMessage ?: "")
    }

    lateinit var binding: ActivityVideoDisplayBinding
    private var player: SimpleExoPlayer? = null
    private var mediaSource: MediaSource? = null
    private var simpleExoPlayerView: PlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_video_play)
        val link = intent?.extras?.getString("LINK_URL", "") ?: ""
        val title = intent?.extras?.getString("TITLE", "Gallery") ?: "Gallery"
        tvTitle.text = title
        btnClose.setOnClickListener {
            onBackPressed()
        }
        //video_view
        val bandwidthMeter = DefaultBandwidthMeter() //TrackSelection.Factory
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val controller = DefaultLoadControl()
        val extractor = DefaultExtractorsFactory()
        val renderer = DefaultRenderersFactory(this@VideoPlayActivity)
        mediaSource = ExtractorMediaSource(
            Uri.parse(link),
            cacheFile,
            extractor,
            null,
            this@VideoPlayActivity,
            link
        )
        simpleExoPlayerView = binding.videoView
        player = ExoPlayerFactory.newSimpleInstance(
            this@VideoPlayActivity,
            renderer,
            trackSelector,
            controller
        )
        player?.addListener(this@VideoPlayActivity)
        simpleExoPlayerView?.player = player
        player?.prepare((mediaSource as ExtractorMediaSource), true, true)
        player?.playWhenReady = true
        player?.seekTo(0, 0)
    }

    override fun onBackPressed() {
        overridePendingTransition(-1, -1)
        super.onBackPressed()
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

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()

    }

}