package com.android.swaddle.adapters.spinneradapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.android.swaddle.R
import com.android.swaddle.activities.providers.class_room_ui.ClassRoomChildDetail
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class AuthorizedAdultsSpinnerAdapter(
    private val contex: Context,
    private val listClass: ArrayList<User>
) : ArrayAdapter<User>(contex, 0, listClass) {

    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)!!
    }

    fun setItems(list: ArrayList<User>) {
        listClass.clear()
        listClass.addAll(list)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var vv = convertView
        vv = LayoutInflater.from(contex).inflate(
            R.layout.item_authorized_adults_spinner, parent, false
        )
        val textViewName = vv?.findViewById<TextView>(R.id.tvSpinner)
        val ivChild = vv?.findViewById<CircleImageView>(R.id.ivChild)

        textViewName?.text = listClass[position].firstName + " " + listClass[position].firstName
        ivChild?.let {
            Glide.with(contex)
                .load(Constants.IMG_BASE_PATH + listClass.get(position).profilePicture)
                .placeholder(R.drawable.ic_acc_circle_small).into(it)
        }
        return vv
    }
}