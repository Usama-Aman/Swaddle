@file:Suppress("DEPRECATION")

package com.android.swaddle.utils.slider

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.rtoshiro.view.video.FullscreenVideoLayout
import java.io.IOException

class PagerGalleryFragment : BaseFragment() {

    private var mDataStoryModel = MediaSliderActivity.MediaModel("", "", "")

    private lateinit var imageCover: TouchImageView
    private var mPosition = 0
    lateinit var rootView: View
    private var mainRL: RelativeLayout? = null

    var videoView: FullscreenVideoLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mDataStoryModel = it.getSerializable("STORY_ITEM") as MediaSliderActivity.MediaModel
            mPosition = it.getInt("POSITION", 0)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.gallery_item, null)
        mainRL = rootView.findViewById(R.id.mainRL)
        videoView = rootView.findViewById(R.id.videoView)

        imageCover = rootView.findViewById(R.id.imageCover)
        return rootView
    }

//    override fun onResume() {
//        super.onResume()
//        if (mDataStoryModel.type == "video") {
//            player?.playWhenReady = true
//            imageCover.viewGone()
//            simpleExoPlayerView?.viewVisible()
//        }
//    }

//    override fun onPause() {
//        super.onPause()
//        if (mDataStoryModel.type == "video") {
//            player?.playWhenReady = false
//            imageCover.viewVisible()
//            simpleExoPlayerView?.viewGone()
//        }
//    }
//
//    override fun onRecycle() {
//        player?.release()
//        player = null
//        super.onRecycle()
//    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mDataStoryModel.type == "image") {
            imageCover.viewVisible()
            videoView?.viewGone()
            Glide.with(imageCover).load(mDataStoryModel.url).fitCenter()
                .placeholder(R.drawable.img_main_logo)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
        } else {

            imageCover.viewGone()
            videoView?.viewVisible()

            videoView?.setActivity(mActivity)

            val videoUri = Uri.parse(mDataStoryModel.url)
            try {
                videoView?.setVideoURI(videoUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

//        if (mDataStoryModel.url.contains("duetmedia.s3")) {

//        } else {
//            if (mDataStoryModel.url.contains("com.codingpixel.duet")) {
//                if (mDataStoryModel.type == "image") {
//                    imageCover.viewVisible()
//                    simpleExoPlayerView?.viewGone()
//                    Glide.with(imageCover).load(mDataStoryModel.url).fitCenter()
//                        .placeholder(R.drawable.ic_image_placeholder)
//                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
//                } else {
//                    //            imageCover.viewGone()
//                    //            simpleExoPlayerView?.viewVisible()
//                    imageCover.viewVisible()
//                    simpleExoPlayerView?.viewGone()
//                    Glide.with(imageCover).load(mDataStoryModel.thumb).fitCenter()
//                        .placeholder(R.drawable.ic_image_placeholder)
//                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
//
//                    mediaSource = ExtractorMediaSource(
//                        Uri.parse(mDataStoryModel.url),
//                        cacheFile,
//                        extractor,
//                        null,
//                        ExtractorMediaSource.EventListener { },
//                        mDataStoryModel.url
//                    )
//                    player?.prepare(mediaSource, true, false)
//                    player?.seekTo(0, 0)
//                    simpleExoPlayerView?.player = player
//                    simpleExoPlayerView?.useController = false
//                    player?.playWhenReady = false
//                }
//            } else {
//                if (mDataStoryModel.type == "image") {
//                    imageCover.viewVisible()
//                    simpleExoPlayerView?.viewGone()
//                    if (File(mDataStoryModel.url).isFile) {
//                        Glide.with(imageCover).load(mDataStoryModel.url).fitCenter()
//                            .placeholder(R.drawable.ic_image_placeholder)
//                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
//                    } else {
//                        Glide.with(imageCover)
//                            .load(mDataStoryModel.url).fitCenter()
//                            .placeholder(R.drawable.ic_image_placeholder)
//                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
//                    }
//
//                } else {
//                    //            imageCover.viewGone()
//                    //            simpleExoPlayerView?.viewVisible()
//                    imageCover.viewVisible()
//                    simpleExoPlayerView?.viewGone()
//                    Glide.with(imageCover)
//                        .load(mDataStoryModel.thumb).fitCenter()
//                        .placeholder(R.drawable.ic_image_placeholder)
//                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageCover)
//                    if (File(mDataStoryModel.url).isFile) {
//                        mediaSource = ExtractorMediaSource(
//                            Uri.parse(mDataStoryModel.url),
//                            cacheFile,
//                            extractor,
//                            null,
//                            ExtractorMediaSource.EventListener { },
//                            mDataStoryModel.url
//                        )
//                    } else {
//                        mediaSource =
//                            ExtractorMediaSource(
//                                Uri.parse(mDataStoryModel.url),
//                                cacheFile,
//                                extractor,
//                                null,
//                                ExtractorMediaSource.EventListener { },
//                                mDataStoryModel.url
//                            )
//                    }
//
//                    player?.prepare(mediaSource, true, false)
//                    player?.seekTo(0, 0)
//                    simpleExoPlayerView?.player = player
//                    simpleExoPlayerView?.useController = false
//                    player?.playWhenReady = false
//                }
//            }
//        }


    companion object {
        fun newInstance(
            item: MediaSliderActivity.MediaModel,
            position: Int
        ): PagerGalleryFragment {
            val frag = PagerGalleryFragment()
            val bundle = Bundle()
            bundle.putSerializable("STORY_ITEM", item)
            bundle.putInt("POSITION", position)
            frag.arguments = bundle
            return frag
        }
    }
}