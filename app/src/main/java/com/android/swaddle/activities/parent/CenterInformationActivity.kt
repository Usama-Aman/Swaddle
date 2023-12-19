package com.android.swaddle.activities.parent

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.providers.MyProfileActivity
import com.android.swaddle.activities.providers.other_reg_screens.CenterRegistrationActivity
import com.android.swaddle.adapters.CenterRelationsAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityCenterInformationBinding
import com.android.swaddle.databinding.CustomMarkerBinding
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.CenterInfo
import com.android.swaddle.models.CenterInfoResponse
import com.android.swaddle.models.User
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CenterInformationActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityCenterInformationBinding
    private var relationAdapter: CenterRelationsAdapter? = null
    private var relationsList = ArrayList<User>()

    var centerInfo: CenterInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
//        map = binding.centerMap as MapView


        callApiToGetCenterInformation(savedInstanceState)

        if (userManager.user?.type == "provider") {
            binding.imgEdit.viewVisible()
        } else {
            binding.imgEdit.viewGone()
        }

//        val mapFragment: SupportMapFragment? = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(this)

    }

//    var map: MapView? = null


//    private fun setRecyclerView() {
//        val manager = LinearLayoutManager(this)
//        binding.recStaff.layoutManager = manager
//        val adapter = CenterInfoStaffAdapter(this)
//        binding.recStaff.adapter = adapter
//    }

    private fun listener(data: CenterInfo?) {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.imgEdit.setOnClickListener {
            startActivity<CenterRegistrationActivity>("item" to data)
        }
    }

    private fun setRelationsRecyclerView() {
        if (relationAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recStaff.layoutManager = manager
            relationAdapter = CenterRelationsAdapter(this, relationsList, object :
                CenterRelationsAdapter.ItemClickListener {
                override fun onItemClick(position: Int) {
                    if (userManager.user?.id == relationsList[position].id)
                        startActivity<MyProfileActivity>(
                            "user" to relationsList.get(position),
                            "canNotEdit" to false
                        )
                    else
                        startActivity<MyProfileActivity>(
                            "user" to relationsList.get(position),
                            "canNotEdit" to true
                        )
                }
            })
            binding.recStaff.adapter = relationAdapter

            relationAdapter?.setListener(object : CenterRelationsAdapter.ItemClickListener {
                override fun onItemClick(position: Int) {
                    val intent =
                        Intent(this@CenterInformationActivity, MyProfileActivity::class.java)
                    intent.putExtra("user", relationsList[position])
                    startActivity(intent)
                }

            })
        } else {
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }

//        if (relationsList.size > 0) {
//            binding.tvNoRelations.viewGone()
//        } else {
//            binding.tvNoRelations.viewVisible()
//        }
    }

    private fun init() {

    }

    private fun callApiToGetCenterInformation(savedInstanceState: Bundle?) {
        showLoading()
        val call: Call<CenterInfoResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getCenterInformation("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<CenterInfoResponse> {
            override fun onResponse(
                call: Call<CenterInfoResponse>,
                response: Response<CenterInfoResponse>
            ) {
                hideLoading()
                setUpCenterData(response.body()?.data)
                relationsList.clear()
                relationsList.add(response.body()?.data?.provider!!)
                relationsList.addAll(response.body()?.data?.staff!!)
                setRelationsRecyclerView()

                listener(response.body()?.data)
                centerInfo = response.body()?.data

                Log.i("jmap", "response map ready} ${centerInfo?.centerName}")

                val mapFragment: SupportMapFragment? = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(this@CenterInformationActivity)


//                map?.onCreate(savedInstanceState)
//                map?.getMapAsync(this@CenterInformationActivity)
            }

            override fun onFailure(
                call: Call<CenterInfoResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@CenterInformationActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setUpCenterData(item: CenterInfo?) {
        try {
            Log.v("jimage", Constants.IMG_BASE_PATH + item?.centerLogo)

            Glide.with(this@CenterInformationActivity)
                .load(Constants.IMG_BASE_PATH + item?.centerLogo)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivCenterPic)

            binding.tvCenterName.text = item?.centerName ?: ""
            if (item?.website != null) {
                binding.tvWeb.text = item?.website ?: ""
            } else {
                binding.tvWeb.text = "N/A"
            }

            binding.tvLocationInfo.text = item?.location ?: ""
            binding.tvPhoneNo.text = item?.phoneNumber ?: ""
            binding.tvEmail.text = item?.email ?: ""

            var timethu = JSONArray(item?.thursday)

            if (item?.monday == "[null,null]") {
                binding.tvMonday.text = "Closed"
                binding.tvMonday.setTextColor(resources.getColor(R.color.red))
            } else {
                var time = JSONArray(item?.monday)
                binding.tvMonday.text = "${time[0]} - ${time[1]}"
                binding.tvMonday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.tuesday == "[null,null]") {
                binding.tvTuesday.setTextColor(resources.getColor(R.color.red))
                binding.tvTuesday.text = "Closed"
            } else {
                var time = JSONArray(item?.tuesday)
                binding.tvTuesday.text = "${time[0]} - ${time[1]}"
                binding.tvTuesday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.wednesday == "[null,null]") {
                binding.tvWednesday.setTextColor(resources.getColor(R.color.red))
                binding.tvWednesday.text = "Closed"
            } else {
                var time = JSONArray(item?.wednesday)
                binding.tvWednesday.text = "${time[0]} - ${time[1]}"
                binding.tvWednesday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.thursday == "[null,null]") {
                binding.tvThursday.setTextColor(resources.getColor(R.color.red))
                binding.tvThursday.text = "Closed"
            }/*else if (timethu[1] == "null"){
                var time = JSONArray(item?.thursday)
                binding.tvThursday.text = "${time[0]} -   --:--"
            }*/ else {
                var time = JSONArray(item?.thursday)
                binding.tvThursday.text = "${time[0]} - ${time[1]}"
                binding.tvThursday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.friday == "[null,null]") {
                binding.tvFriday.setTextColor(resources.getColor(R.color.red))
                binding.tvFriday.text = "Closed"
            } else {
                var time = JSONArray(item?.friday)
                binding.tvFriday.text = "${time[0]} - ${time[1]}"
                binding.tvFriday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.saturday == "[null,null]") {
                binding.tvSaturday.text = "Closed"
                binding.tvSaturday.setTextColor(resources.getColor(R.color.red))
            } else {
                var time = JSONArray(item?.saturday)
                binding.tvSaturday.text = "${time[0]} - ${time[1]}"
                binding.tvSaturday.setTextColor(resources.getColor(R.color.colorBlack))
            }

            if (item?.sunday == "[null,null]") {
                binding.tvSunday.text = "Closed"
                binding.tvSunday.setTextColor(resources.getColor(R.color.red))
            } else {
                var time = JSONArray(item?.sunday)
                binding.tvSunday.text = "${time[0]} - ${time[1]}"
                binding.tvSunday.setTextColor(resources.getColor(R.color.colorBlack))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var googleMap: GoogleMap? = null

    override fun onMapReady(p0: GoogleMap?) {

        this.googleMap = p0


//        googleMap?.apply {
//            val sydney = LatLng(-33.852, 151.211)
////            addMarker(
////                MarkerOptions()
////                    .position(sydney)
////                    .title("Marker in Sydney")
////            )
//        }

        if (centerInfo?.lat != null) {

            Log.i("jmap", "map ready : ${centerInfo?.lng!!} , ${centerInfo?.lng!!}")
            var TutorialsPoint =
                LatLng(centerInfo?.lat?.toDouble()!!, centerInfo?.lng?.toDouble()!!)


//            addMarker(TutorialsPoint,this@CenterInformationActivity
//            ,centerInfo?.centerLogo!!)

            myCustomMarker(TutorialsPoint)

            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(TutorialsPoint))

            //Move the camera to the user's location and zoom in!

            //   Move the camera to the user's location and zoom in!
//            googleMap?.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        centerInfo?.lat?.toDouble()!!,
//                        centerInfo?.lng?.toDouble()!!
//                    ), 12.0f
//                )
//            )

            val pos = LatLng(centerInfo?.lat?.toDouble()!!, centerInfo?.lng?.toDouble()!!)
            val update = CameraUpdateFactory.newLatLngZoom(pos, 15f)
            googleMap?.moveCamera(update)

        }
    }

    fun myCustomMarker(latLng: LatLng) {


        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(80, 80, conf)
        val canvas1 = Canvas(bmp)

// paint defines the text color, stroke width and size

// paint defines the text color, stroke width and size
        val color = Paint()
        color.setTextSize(35.toFloat())
        color.setColor(Color.BLACK)

// modify canvas

// modify canvas
//        canvas1.drawBitmap()
//        canvas1.drawBitmap(
//            BitmapFactory.decodeResource(
//                resources,
//                R.drawable.ic_person
//            ), 0, 0, color
//        )
//        canvas1.drawText("User Name!", 30f, 40f, color)

//        if(centerInfo?.centerLogo!=null){
//            googleMap?.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//                    .title("Sydney")
//                    .snippet("Population: 4,627,300")
//                    .icon(BitmapDescriptorFactory.fromBitmap()
//
//
//        }
//
//        else {
//            googleMap?.addMarker(MarkerOptions().position(latLng).title(centerInfo?.centerName))
//        }


        googleMap?.addMarker(MarkerOptions().position(latLng).title(centerInfo?.centerName))


    }

    open fun addMarker(
        latLng: LatLng,
        context: Context,
        imageURL: String
    ) {
        val markerOptions = MarkerOptions().position(latLng)
        markerOptions.title(centerInfo?.centerName)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                createCustomMarker(
                    context
                )
            )
        )
        googleMap?.addMarker(markerOptions)
    }


    open fun createCustomMarker(context: Context): Bitmap? {

        val binding: CustomMarkerBinding = CustomMarkerBinding.inflate(layoutInflater)

        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        binding.marker.setLayoutParams(
            ActionBar.LayoutParams(
                52,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        binding.marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        binding.marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        binding.marker.buildDrawingCache()
        val bitmap: Bitmap = Bitmap.createBitmap(
            binding.marker.getMeasuredWidth(),
            binding.marker.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        binding.marker.draw(canvas)
        return bitmap
    }


}