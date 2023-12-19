package com.android.swaddle.utils.slider

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.capitalized
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.utils.CustomToasts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.slider.*


import java.io.File
import java.io.FileOutputStream
import java.io.Serializable


class MediaSliderActivity : BaseActivity() {
    private var downloadID: Long = 0
    private var mPager: ViewPager? = null
    private var sliderMediaNumber: TextView? = null
    private var playbackPosition: Long = 0
    private var currentWindow = 0
    private var isTitleVisible = false
    private var isMediaCountVisible = false
    private var isNavigationVisible = false
    private var title: String = ""
    private var urlList: ArrayList<MediaModel> = ArrayList()
    private var titleTextColor: String? = null
    private var titleBackgroundColor: String? = null
    var left: ImageView? = null
    var right: ImageView? = null
    private var startPosition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.slider)

        val hideOptionDots = intent?.extras?.getBoolean("HIDE_DOTS") ?: false
        val list = intent?.extras?.getSerializable("MEDIA_LIST") as ArrayList<MediaModel>
        val isTitleVisible = intent?.extras?.getBoolean("TITLE_VISIBLE") ?: false
        val isMediaCountVisible = intent?.extras?.getBoolean("MEDIA_COUNT_VISIBLE") ?: false
        val isNavigationVisible = intent?.extras?.getBoolean("NAVIGATION_VISIBLE") ?: false
        val title = intent?.extras?.getString("TITLE", "") ?: "Gallery"
        val titleBgColor = intent?.extras?.getString("TITLE_BG", null)
        val titleTextColor = intent?.extras?.getString("TITLE_CLR", null)
        val position = intent?.extras?.getInt("POSITION", 0) ?: 0
        loadMediaSliderView(
            list,
            isTitleVisible,
            isMediaCountVisible,
            isNavigationVisible,
            title,
            titleBgColor,
            titleTextColor ?: "",
            position
        )

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        haulerView.setOnDragDismissedListener {
            finish() // finish activity when dismissed
        }

        if (hideOptionDots) {
            image_dots.visibility = View.INVISIBLE
        } else {
            image_dots.visibility = View.VISIBLE
        }
    }

    private fun loadMediaSliderView(
        mediaUrlList: ArrayList<MediaModel>,
        isTitleVisible: Boolean,
        isMediaCountVisible: Boolean,
        isNavigationVisible: Boolean,
        title: String,
        titleBackgroundColor: String?,
        titleTextColor: String,
        startPosition: Int
    ) {
        urlList = mediaUrlList
        this.isTitleVisible = isTitleVisible
        this.isMediaCountVisible = isMediaCountVisible
        this.isNavigationVisible = isNavigationVisible
        this.title = title
        this.titleBackgroundColor = titleBackgroundColor
        this.titleTextColor = titleTextColor
        this.startPosition = startPosition
        initViewsAndSetAdapter()
    }

    private fun setStartPosition() {
        if (startPosition >= 0) {
            if (startPosition > urlList.size) {
                mPager?.currentItem = urlList.size - 1
                return
            }
            mPager?.currentItem = startPosition
        } else {
            mPager?.currentItem = 0
        }
        mPager?.offscreenPageLimit = 0
        checkPage(startPosition)

        if (urlList.size < 2) {
            right?.visibility = View.GONE
            left?.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewsAndSetAdapter() {
        val statusLayout: RelativeLayout = findViewById(R.id.status_holder)
        val sliderTitle: TextView = findViewById(R.id.title)
        val btnClose: ImageView = findViewById(R.id.btnClose)
        val tvUserName: TextView = findViewById(R.id.tvUserName)

        image_dots.setOnClickListener {
            menuView.animate().alpha(1f).setDuration(100)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        menuView.visibility = View.VISIBLE
                    }
                })
        }

        closeMenuButton.setOnClickListener {
            menuView.visibility = View.GONE
        }




        btnCopyLink.setOnClickListener {
            menuView.visibility = View.GONE

            val url = urlList.get(mPager!!.currentItem).url


            Log.e("dataUrl", url)
            if (urlList.get(mPager!!.currentItem).type.equals("video")) {

                // download video
                var str = urlList.get(mPager!!.currentItem).url
                var delimiter = "/"

                val parts = str.split(delimiter)
                val videoFileName = parts[1]
                Log.e("fileNa,e", parts[1])
                beginDownload(url, videoFileName)

            } else {
                Glide.with(this).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        saveImage(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        CustomToasts.successToast(this@MediaSliderActivity, "Failed to Save image")
                    }
                })
            }
        }

        tvUserName.text = title.capitalized()
        btnClose.setOnClickListener {
            onBackPressed()
        }
        sliderMediaNumber = findViewById<TextView?>(R.id.number)

        left = findViewById(R.id.left_arrow)

        right = findViewById(R.id.right_arrow)
        mPager = findViewById<ViewPager?>(R.id.pager)
        val pagerAdapter: PagerAdapter = ScreenSlidePagerAdapter(this@MediaSliderActivity, urlList)
        mPager?.adapter = pagerAdapter
        mPager?.setPageTransformer(false, ForegroundToBackgroundTransformer())
        if (isNavigationVisible) {
            left?.viewVisible()
            right?.viewVisible()
            left?.setOnClickListener {
                val i = mPager?.currentItem
                mPager?.currentItem = i?.minus(1) ?: 0
                sliderMediaNumber?.text = "${(mPager?.currentItem?.plus(1))}/${urlList.size}"
            }
            right?.setOnClickListener {
                val i = mPager?.currentItem
                mPager?.currentItem = i?.plus(1) ?: 1
                sliderMediaNumber?.text = "${(mPager?.currentItem?.plus(1))}/${urlList.size}"
            }
            if (urlList.size == 0) {
                left?.viewGone()
                right?.viewGone()
            }
        }
        //        DragPinchManager(mPager ?: ViewPager(this@MediaSliderActivity))
        //        mPager?.setOnTouchListener(this@MediaSliderActivity)
        setStartPosition()
        val hexRegex = "/^#(?:(?:[\\da-f]{3}){1,2}|(?:[\\da-f]{4}){1,2})$/i"
        if (isTitleVisible || isMediaCountVisible) {
            if (titleBackgroundColor != null && (titleBackgroundColor?.matches(hexRegex.toRegex())) == true) {
                statusLayout.setBackgroundColor(Color.parseColor(titleBackgroundColor))
            } else {
                statusLayout.setBackgroundColor(this@MediaSliderActivity.resources.getColor(R.color.transparent))
            }
        }
        if (isTitleVisible) {
            sliderTitle.visibility = View.VISIBLE
            sliderTitle.text = title
            if (titleTextColor != null && titleTextColor?.matches(hexRegex.toRegex()) == true) {
                sliderTitle.setTextColor(Color.parseColor(titleTextColor))
            }
        }
        if (isMediaCountVisible) {
            sliderMediaNumber?.visibility = View.VISIBLE
            sliderMediaNumber?.text = "${(mPager?.currentItem?.plus(1))}/${urlList.size}"
        }

        mPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {


            }

            override fun onPageSelected(i: Int) {
                checkPage(i)

            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
    }

    fun checkPage(i: Int) {
        if (isNavigationVisible) {

            when (i) {
                0 -> {
                    left?.viewGone()
                    right?.viewVisible()
                }
                (urlList.size - 1) -> {
                    right?.viewGone()
                    left?.viewVisible()
                }
                else -> {
                    left?.viewVisible()
                    right?.viewVisible()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(onDownloadComplete)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun saveImage(image: Bitmap): String? {
        var savedImagePath: String? = null

        var str = urlList.get(mPager!!.currentItem).url
        var delimiter = "/"

        val parts = str.split(delimiter)
        val imageFileName = parts[parts.lastIndex]
        Log.e("fileNa,e", parts[parts.lastIndex])

        val storageDir =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/DUET"
            )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
                CustomToasts.successToast(this, "Successfully saved image")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
        }
        return savedImagePath
    }

    private fun galleryAddPic(imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        sendBroadcast(mediaScanIntent)
    }

    private inner class ScreenSlidePagerAdapter(
        private val context: BaseActivity,
        private val urlList: ArrayList<MediaModel>
    ) :
        FragmentPagerAdapter(
            context.supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {

        override fun getItem(position: Int): Fragment {
            return PagerGalleryFragment.newInstance(urlList[position], position)
        }

        override fun getCount(): Int {
            return urlList.size
        }

    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID === id) {
                Toast.makeText(this@MediaSliderActivity, "Download Completed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun beginDownload(path: String, title: String) {
        val file = File(getExternalFilesDir(null), "DuetVideos")
        /*
        Create a DownloadManager.Request with all the information necessary to start the download
         */
        val request = DownloadManager.Request(Uri.parse(path))
            .setTitle(title) // Title of the Download Notification
            .setDescription("Downloading...") // Description of the Download Notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
            .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            //.setRequiresCharging(false) // Set if charging is required to begin the download
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    class MediaModel(
        internal var url: String,
        internal var type: String,
        internal var thumb: String
    ) : Serializable
}