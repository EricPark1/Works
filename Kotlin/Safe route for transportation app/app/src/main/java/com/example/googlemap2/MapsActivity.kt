package com.example.googlemap2

//import com.odsay.odsayandroidsdk.ODsayService
import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.googlemap2.data.shortestpath
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.ODsayService
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import org.json.JSONException
import java.io.IOException
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    val apiKey = "OS7XNruKFeFJEzM3R8bqO99UAiJqmNoWLXWjG7bXAUY"
    val PERM_FLAG = 99
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var did: Boolean = false
    var currentLocationLat: String = "0.0"
    var currentLocationLon: String = "0.0"
    var destinationLat: String = "0.0"
    var destinationLon: String = "0.0"
    var markerNum: Int = 0
    var gson = Gson()


    //widget
    lateinit var mSearchText: EditText
    lateinit var mSubmitBtn: Button
    lateinit var mtv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mSearchText = findViewById(R.id.google_search)
        mSubmitBtn = findViewById(R.id.button)
        mtv = findViewById(R.id.textview)
        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERM_FLAG)
        }
    }


    fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun init() {
//         mSearchText.setOnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN || event?.action == KeyEvent.KEYCODE_ENTER) {
//                //execute method for searching
//                geoLocate()
//            }
//            false
//        }
        mSubmitBtn.setOnClickListener {
            if (mSearchText.text.toString() != "") {
                geoLocate()
            }
        }
//        mSearchText.setOnEditorActionListener( object: TextView.OnEditorActionListener {
//            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN || event?.action == KeyEvent.KEYCODE_ENTER) {
//                //execute method for searching
//                geoLocate()
//            }
//                return false
//        }
//
//        })
    }

    fun geoLocate() {
        var searchString: String = mSearchText.text.toString()
        var geocoder: Geocoder = Geocoder(this)
//        var address: Address
        var addresses: List<Address> = arrayListOf<Address>()
        try {
            addresses = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (addresses.isNotEmpty()) {
            val address= addresses[0]
            markDestination(address)
            var userInput : DialogInterface.OnClickListener = object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when (which){
                        DialogInterface.BUTTON_POSITIVE -> showUpPath()
                        DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(this@MapsActivity, "???????????? ?????? ??????????????????", Toast.LENGTH_LONG).show()
                    }
                }

            }
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("????????? ???????????? ?????????????????????????\n").setCancelable(false).setPositiveButton("???!", userInput)
                .setNegativeButton("?????????!", userInput).setTitle("?????? ?????? ?????? ??????").show();
//            val temp = "????????? ???????????? ?????????????????????????\n" + address.featureName
//            dialogBuilder.setMessage(temp).setCancelable(false).setPositiveButton("???!", DialogInterface.OnClickListener(){
//                dialog, id -> showUpPath()
//            }).setNegativeButton("?????????!", DialogInterface.OnClickListener(){
//                dialog, id -> dialog.cancel()
//            })
//            val alert = dialogBuilder.create()
//            alert.setTitle("?????? ?????? ?????? ??????")
//            alert.show()
        }
    }

    fun startProcess() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        init()
    }

    fun markDestination(add: Address) {
        destinationLon = add.longitude.toString()
        destinationLat = add.latitude.toString()
        var location = LatLng(destinationLat.toDouble(), destinationLon.toDouble())
        val markerOption = MarkerOptions().position(location).title("???????")
        val camera = CameraPosition.Builder().target(location).zoom(15.5f).build()
        val cameraOption = CameraUpdateFactory.newCameraPosition(camera)
        mMap.addMarker(markerOption)
        markerNum++
        mMap.animateCamera(cameraOption)
    }


    fun showUpPath(){
        Log.d("I am here: ", "Hello")
        var odsayServices: ODsayService = ODsayService.init(this, apiKey)
        odsayServices.setReadTimeout(5000)
        odsayServices.setConnectionTimeout(5000)

        val onResultCallbackListener = object : OnResultCallbackListener {
            override fun onSuccess(odsayData: ODsayData?, api: API?) {
                try{
                    if(api == API.SEARCH_PUB_TRANS_PATH){
                        val path = odsayData!!.json.optString("result")
//                        Log.d("path0: ", path)
                        val jsonstring = gson.fromJson(path, shortestpath::class.java)
//                        Log.d("path0: ", jsonstring.toString())

                        if(!jsonstring.equals("")){
                            createTextView(jsonstring)
                        }
//                        path = odsayData!!.json.getJSONObject("result").getString("path")[1]
//                        Log.d("path1: ", path)
                    }
                }catch(e : JSONException){
                    Toast.makeText(this@MapsActivity,"????????? ???????????? ????????????", Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(p0: Int, p1: String?, p2: API?) {
                Log.d("API??????: %s", p1!!)
            }

        }
//        Log.d("?????????: ", currentLocationLon+ ":"+ currentLocationLat +":" + destinationLon +":" + destinationLat)
        try {
            odsayServices.requestSearchPubTransPath(
                currentLocationLon,
                currentLocationLat,
                destinationLon,
                destinationLat,
                "0",
                "",
                "0",
                onResultCallbackListener
            )
        }catch (e : Exception){
            Log.d("????????? ??????????????? ??????", e.printStackTrace().toString())
        }
        
    }

    fun createTextView(path : shortestpath){
//        var input= "a"
        val dist = "???????????? ???????????? ??????: " + path.pointDistance + "m\n"
//        var transition = "?????? ???/???: "
//        if(path.outTrafficCheck == 0){
//            transition += "??????\n"
//        }else{
//            transition += "??????\n"
//        }
        var pathes = ArrayList<String>()
        var tt: String
        for( i in path.path.iterator()){
            var temp0 = "????????????: "

            when(i.pathType){
                1 -> temp0 += "?????????\n\n"
                2 -> temp0 += "??????\n\n"
                3 -> temp0 += "????????? + ??????\n\n"
            }
            val temp = "??? ????????????: " + i.info.totalTime + "???\n\n"
            val temp2 = "?????????: " + i.info.firstStartStation + "\n\n"
            val temp3 = "?????????: " + i.info.lastEndStation + "\n\n"
            var temp4 = ""

            for( j in i.subPath.iterator()){
                when (j.trafficType) {
                    1 -> /*?????????*/ temp4 += "(?????????)????????????: ${j.sectionTime}???\n????????????: ${
                        j.lane.joinToString(separator = " -> ") { it -> it.name }
                    }\n??????: ${j.passStopList.stations.joinToString(separator = " -> ") { it -> it.stationName }}\n\n"
                    2 -> /* ??????*/ temp4 += "?????? ????????????: ${j.distance}m\n(??????)????????????: ${j.sectionTime}???\n\n?????????: ${j.lane.joinToString("???, "){it -> it.busNo}
                    }???\n" +
                            "?????????: ${j.passStopList.stations.joinToString(separator = " -> ") { it -> it.stationName }}\n\n"
                    3 -> /* ?????? */ temp4 += if (j.distance == 0) {
                        "\n"
                    } else {
                        "(??????)????????????: ${j.sectionTime}???\n?????? ??????: ${j.distance}m\n\n"
                    }
                }

            }
            pathes.add(temp.toString()+ temp0 + temp2 + temp3 + temp4 + "??????!\n\n")
        }
        var a = Random.nextInt(0, pathes.size)
//        while(a < pathes.size && a > 0){
//            a = Random.nextInt()
//        }
        val parentGroup = mtv.parent as ViewGroup?
        var notice = "\n****?????? ????????? ???????????????!****\n\n"
        parentGroup?.removeAllViews()
        setContentView(mtv)
//        var kkk = SpannableString(dist.plus(notice).plus(pathes[a])) as CharSequence//pathes.joinToString(separator = "\n"){ it -> it}))
        mtv.text = dist.plus(notice).plus(pathes[a])//pathes.joinToString(separator = "\n"){ it -> it})
        mtv.movementMethod = ScrollingMovementMethod()
        mtv.setTypeface(null, Typeface.BOLD)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setUpdateLocationListener()
    }

    //??? ?????? ???????????? ??????
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for ((i, location) in it.locations.withIndex()) {
                        Log.d("????????????", "$i ${location.latitude}, ${location.longitude}")
                        setLastLocation(location)
                    }
                }
            }
        }
        //???????????? ?????? ?????? ?????? (locationRequest, locationCallback)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun setLastLocation(location: Location?) {
        currentLocationLat = location!!.latitude.toString()
        currentLocationLon = location.longitude.toString()
        val myLocation = LatLng(location.latitude, location.longitude)
        val markerOption = MarkerOptions().position(myLocation).title("??? ???????????????!")
        val camera = CameraPosition.Builder().target(myLocation).zoom(15.5f).build()
        val cameraOption = CameraUpdateFactory.newCameraPosition(camera)
        mMap.isMyLocationEnabled = true
        if (markerNum >= 3) {
            mMap.clear()
            mMap.addMarker(
                MarkerOptions().position(LatLng(currentLocationLat.toDouble(), currentLocationLon.toDouble()))
                    .title("??? ???????????????!")
            )
            mMap.addMarker(
                MarkerOptions().position(LatLng(destinationLat.toDouble(), destinationLon.toDouble())).title("???????")
            )
        }
//        mMap.clear()
        mMap.addMarker(markerOption)
        markerNum++
        if (did) {
            //do nothing
        } else {
            mMap.animateCamera(cameraOption)
            did = true
        }


    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERM_FLAG -> {
                var check = true
                for (grant in grantResults) {
                    if (grant != PERMISSION_GRANTED) {
                        check = false
                        break
                    }
                }
                if (check) {
                    startProcess()
                } else {
                    Toast.makeText(this, "????????? ???????????? ?????????????????????", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}