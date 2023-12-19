package com.android.swaddle.adapters.notification_adapter


import android.annotation.SuppressLint
import android.content.Context
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemNotificationBinding
import com.android.swaddle.helper.CustomTypefaceSpan
import com.android.swaddle.helper.Spanny
import com.android.swaddle.models.NotificationDetail
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList

class NotificationsAdapter(
    private val context: Context,
    var list: ArrayList<NotificationDetail>,
    val notificationInterface: NotificationInterface
) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    interface NotificationInterface {
        fun onNotificationClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemNotificationBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_notification,
            parent,
            false
        )
        return NotificationViewHolder(
            binding,
            this@NotificationsAdapter,
            context
        )
    }

    fun setItems(mList: ArrayList<NotificationDetail>) {
        list.clear()
        list.addAll(mList)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val adapter: NotificationsAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(item: NotificationDetail, position: Int) {
            Glide.with(context).load(Constants.IMG_BASE_PATH + item.userDetails?.profilePicture)
                .circleCrop().placeholder(R.drawable.ic_user_placeholder)
                .into(binding.ivProfileImage)
            val fontBold = ResourcesCompat.getFont(context, R.font.font_semi_bold)
            val fontregular = ResourcesCompat.getFont(context, R.font.font_regular)
            val string = Spanny(
                if(item.type == "todays_activity_missing" || item.type == "center_closing_alert")
                    "Reminder"
                else
                    item.userDetails?.firstName + " " + item.userDetails?.lastName,
                ForegroundColorSpan(context.resources?.getColor(R.color.grey_900) ?: 0),
                AbsoluteSizeSpan(12, true),
                CustomTypefaceSpan(fontBold!!)
            )
                .append(
                    "  " + item.notification ?: "",
                    ForegroundColorSpan(context.resources?.getColor(R.color.colorBlack50) ?: 0),
                    AbsoluteSizeSpan(10, true),
                    CustomTypefaceSpan(fontregular!!)
                )
            binding.tvComment.text = string
            binding.tvTime.text = getTimeAgo(
                DateConverter.convertDateToMillis(item.createdAt ?: ""), context
            )

            binding.root.setOnClickListener {
                notificationInterface.onNotificationClicked(adapterPosition)
            }

        }

        private fun getTimeAgo(time: Long, ctx: Context): String? {
            var time = time
            if (time < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                time *= 1000
            }
            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }

            // TODO: localize
            val diff = now - time
            return if (diff < 2000) {
                ctx.resources.getString(R.string.just_now)
            } else if (diff < MINUTE_MILLIS) {
                (diff / SECOND_MILLIS).toString() + " " + ctx.resources
                    .getString(R.string.seconds_ago)
                //return ctx.getResources().getString(R.string.just_now);
            } else if (diff < 2 * MINUTE_MILLIS) {
                //return ctx.getResources().getString(R.string.a_minute_ago);
                (diff / MINUTE_MILLIS).toString() + " " + ctx.resources
                    .getString(R.string.minutes_ago)
            } else if (diff < 50 * MINUTE_MILLIS) {
                if (Locale.getDefault().displayLanguage
                        .equals("english", ignoreCase = true)
                ) {
                    (diff / MINUTE_MILLIS).toString() + " " + ctx.resources
                        .getString(R.string.minutes_ago)
                } else {
                    ctx.resources.getString(R.string.ago) + " " + diff / MINUTE_MILLIS + " " + ctx.resources
                        .getString(R.string.minutes)
                }
            } else if (diff < 90 * MINUTE_MILLIS) {
                ctx.resources.getString(R.string.an_hour_ago)
            } else if (diff < 24 * HOUR_MILLIS) {
                if (Locale.getDefault().displayLanguage
                        .equals("english", ignoreCase = true)
                ) {
                    (diff / HOUR_MILLIS).toString() + " " + ctx.resources
                        .getString(R.string.hours_ago)
                } else {
                    ctx.resources.getString(R.string.ago) + " " + diff / HOUR_MILLIS + " " + ctx.resources
                        .getString(R.string.hours)
                }
            } else if (diff < 48 * HOUR_MILLIS) {
                ctx.resources.getString(R.string.yesterday)
            } else {
                if (Locale.getDefault().displayLanguage
                        .equals("english", ignoreCase = true)
                ) {
                    (diff / DAY_MILLIS).toString() + " " + ctx.resources
                        .getString(R.string.days_ago)
                } else {
                    ctx.resources.getString(R.string.ago) + " " + diff / DAY_MILLIS + " " + ctx.resources
                        .getString(R.string.days)
                }
            }
        }
    }
}