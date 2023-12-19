package com.android.swaddle.utils


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import com.android.swaddle.R
import com.bumptech.glide.Glide


class BadgeView : RelativeLayout {

    private lateinit var parentView: View
    private lateinit var mImageBadge: ImageView
    private lateinit var mImageIcon: ImageView
    private lateinit var mCardView: CardView

    private var mImageCornerRadius: Int = 0
    private var mBadgeCornerDistance: Int = 0
    private var mBadgeColor: Int = 0
    private var mBadgeStrokeColor: Int = 0

    private var mHasBadge: Boolean = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context) {
        parentView = View.inflate(context, R.layout.badge_view, this)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        parentView = View.inflate(context, R.layout.badge_view, this)

        mImageBadge = parentView.findViewById(R.id.badge_view_image_badge)
        mImageIcon = parentView.findViewById(R.id.badge_view_image_icon)
        mCardView = parentView.findViewById(R.id.badge_view_card_view)

        val a = context.obtainStyledAttributes(attrs, R.styleable.BadgeView, 0, 0)

        mHasBadge = a.getBoolean(R.styleable.BadgeView_hasBadge, false)
        mImageIcon.setImageResource(a.getResourceId(R.styleable.BadgeView_imageDrawable, 0))
        mBadgeCornerDistance = a.getDimensionPixelSize(R.styleable.BadgeView_badgeCornerDistance, 1)
        mBadgeColor = a.getColor(R.styleable.BadgeView_badgeColor, 1)
        mBadgeStrokeColor = a.getColor(R.styleable.BadgeView_badgeStrokeColor, 1)
        mImageCornerRadius = a.getDimensionPixelSize(R.styleable.BadgeView_imageCornerRadius, 0)

        mImageBadge.setImageResource(R.drawable.badge)

        mCardView.radius = mImageCornerRadius.toFloat()

        val param = mImageBadge.layoutParams as LayoutParams
        param.setMargins(
            mBadgeCornerDistance,
            mBadgeCornerDistance,
            mBadgeCornerDistance,
            mBadgeCornerDistance
        )
        mImageBadge.layoutParams = param

        val gd = mImageBadge.drawable as GradientDrawable
        gd.setColor(mBadgeColor)
        gd.setSize(28, 28)
        gd.setStroke(6, mBadgeStrokeColor)

        if (!mHasBadge) mImageBadge.visibility = View.GONE
        a.recycle()
    }

    fun getImageView(): ImageView {
        return mImageIcon
    }

    fun getHasBadge(): Boolean {
        return mHasBadge
    }

    fun setHasBadge(value: Boolean) {
        mHasBadge = value
        if (!mHasBadge) mImageBadge.visibility = View.GONE
        else mImageBadge.visibility = View.VISIBLE
        invalidate()

    }

    fun setBadgeColor(color: Int) {
        val gd = mImageBadge.drawable as GradientDrawable
        gd.setColor(color)
    }

    fun setIconDrawable(drawable: Int) {
        mImageIcon.setImageResource(drawable)
        invalidate()
        requestLayout()
    }

    fun setImageUri(uri: String) {
        Glide.with(context).load(uri.toUri()).placeholder(R.drawable.ic_user_placeholder_new)
            .into(mImageIcon)
    }
}