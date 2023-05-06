package com.example.weatherapp

import com.example.weatherapp.Constants
import com.example.weatherapp.R



import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient:FusedLocationProviderClient
    private var mProgressDialog: Dialog?=null
    var binding: ActivityMainBinding?=null
    private lateinit var tv_main:TextView
    private lateinit var tv_desc:TextView
    private lateinit var tvTemp:TextView
    private lateinit var mSharedPrefences:SharedPreferences
    private lateinit var tv_sunrise_time:TextView
    private lateinit var tv_sunset_time:TextView
    private lateinit var tv_humidity:TextView
    private lateinit var tv_max:TextView
    private lateinit var tvMin:TextView
    private lateinit var tvSpeed:TextView
    private lateinit var tvName:TextView
    private lateinit var tvCountry:TextView
    private lateinit var speed:TextView
    private lateinit var main:ImageView
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        tv_main=findViewById(R.id.tv_main)
        binding=ActivityMainBinding.inflate(layoutInflater)
        tv_desc=findViewById(R.id.tv_main_description)
        tvTemp=findViewById(R.id.tv_temp)
        tvSpeed=findViewById(R.id.tv_speed)
        tv_sunrise_time=findViewById(R.id.tv_sunrise_time)
        tv_sunset_time=findViewById(R.id.tv_sunset_time)
        tv_humidity=findViewById(R.id.tv_humidity)
        tv_max=findViewById(R.id.tv_max)
        tvMin=findViewById(R.id.tv_min)
        speed=findViewById(R.id.tv_speed)
        tvName=findViewById(R.id.tv_name)
        tvCountry=findViewById(R.id.tv_country)
        main=findViewById(R.id.iv_main)
        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        mSharedPrefences=getSharedPreferences(Constants.PREFENCE_NAME,Context.MODE_PRIVATE)
        setupUI()
        if(!isLocationEnabled()){
            MotionToast.darkColorToast(this,
                "Your location provider is turned off",
                "Please turn it on",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this,www.sanju.motiontoast.R.font.helvetica_regular))
            val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            Dexter.withActivity(this).withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION).withListener(object :MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        requestLocationData()
                    }
                    if(report.isAnyPermissionPermanentlyDenied){
                        MotionToast.darkColorToast(this@MainActivity,
                            "You have denied location permissions",
                            "Please turn it on for the app to work",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@MainActivity,www.sanju.motiontoast.R.font.helvetica_regular))
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
        }
    }
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = com.google.android.gms.location.LocationRequest()

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback=object:LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val mLastLocation: Location? = p0.lastLocation
            val latitude= mLastLocation?.latitude
            Log.i("Current Latitute","$latitude")
            val longitude= mLastLocation?.longitude
            Log.i("Current longitude","$longitude")
            if (latitude != null) {
                if (longitude != null) {
                    getLocationWeatherDetails(latitude,longitude)
                }
            }


        }
    }
    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){
        if (Constants.isNetworkAvailable(this@MainActivity)) {

            // TODO (STEP 1: Make an api call using retrofit.)
            // START
            /**
             * Add the built-in converter factory first. This prevents overriding its
             * behavior but also ensures correct behavior when using converters that consume all types.
             */
            val retrofit: Retrofit = Retrofit.Builder()
                // API base URL.
                .baseUrl(Constants.BASE_URL)
                /** Add converter factory for serialization and deserialization of objects. */
                /**
                 * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
                 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
                 */
                .addConverterFactory(GsonConverterFactory.create())
                /** Create the Retrofit instances. */
                .build()
            // END

            // TODO (STEP 5: Further step for API call)
            // START
            /**
             * Here we map the service interface in which we declares the end point and the API type
             *i.e GET, POST and so on along with the request parameter which are required.
             */
            val service: WeatherService =
                retrofit.create(WeatherService::class.java)

            /** An invocation of a Retrofit method that sends a request to a web-server and returns a response.
             * Here we pass the required param in the service
             */
            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )
            showCustomProgressDialog()
            // Callback methods are executed using the Retrofit callback executor.
            listCall.enqueue(object : Callback<WeatherResponse> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {

                    // Check weather the response is success or not.

                    if (response.isSuccessful) {

                        hideProgressDialog()
                        /** The de-serialized response body of a successful response. */
                        val weatherList: WeatherResponse? = response.body()

                        val weatherResponseJsonString= Gson().toJson(weatherList)
                        val editor=mSharedPrefences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                        editor.apply()


                        setupUI()
                        Log.i("Response Result", "$weatherList")
                    } else {
                        // If the response is not success then we check the response code.
                        when (val sc = response.code()) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error $sc")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Error",t!!.message.toString())
                    hideProgressDialog()
                }
            })
        }else{
            MotionToast.darkColorToast(this@MainActivity,
                "No internet Connection Available",
                "Please turn it on for the App to work",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this@MainActivity,www.sanju.motiontoast.R.font.helvetica_regular))
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun showCustomProgressDialog(){
        mProgressDialog= Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }
    private fun hideProgressDialog(){
        if(mProgressDialog!=null){
            mProgressDialog!!.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh->{
                requestLocationData()
                true

            }
            else -> {
                return super.onOptionsItemSelected(item)}
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupUI(){
        val weatherResponseJsonString=mSharedPrefences.getString(Constants.WEATHER_RESPONSE_DATA,"")

        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList=Gson().fromJson(weatherResponseJsonString,WeatherResponse::class.java)
            for(i in weatherList?.weather?.indices!!){
                Log.i("Weather Name",weatherList?.weather.toString())
                tv_main.text =weatherList.weather[i].main

                tv_desc.text=weatherList.weather[i].description

                tvTemp.text=weatherList.main.temp.toString()+getUnit(application.resources.configuration.locales.toString())
                tv_sunrise_time.text=unixTime(weatherList.sys.sunrise)
                tv_sunset_time.text=unixTime(weatherList.sys.sunset)
                tv_humidity.text=weatherList.main.humidity.toString() +"%"
                tv_max.text=weatherList.main.temp_max.toString() +" max"
                tvMin.text=weatherList.main.temp_min.toString()+" min"
                tvName.text=weatherList.name
                speed.text=weatherList.wind.speed.toString()
                tvCountry.text=weatherList.sys.country

                when(weatherList.weather[i].icon){
                    "01d"-> main.setImageResource(R.drawable.sunny)
                    "02d"->main.setImageResource(R.drawable.cloud)
                    "03d"->main.setImageResource(R.drawable.cloud)
                    "04d"->main.setImageResource(R.drawable.cloud)
                    "04n"->main.setImageResource(R.drawable.cloud)
                    "10d"->main.setImageResource(R.drawable.rain)
                    "11d"->main.setImageResource(R.drawable.storm)
                    "13d"->main.setImageResource(R.drawable.snowflake)
                    "01n"->main.setImageResource(R.drawable.cloud)
                    "02n"->main.setImageResource(R.drawable.cloud)
                    "03n"->main.setImageResource(R.drawable.cloud)
                    "10n"->main.setImageResource(R.drawable.cloud)
                    "11n"->main.setImageResource(R.drawable.rain)
                    "13n"->main.setImageResource(R.drawable.snowflake)
                    "50d"->main.setImageResource(R.drawable.mist)
                    "50n"->main.setImageResource(R.drawable.mist)

                }




            }
        }

    }

    private fun getUnit(value: String): String {
        var values="°C"
        if("US"==value||"LR"==value||"MM"==value){
            values="°F"
        }
        return values

    }
    private fun unixTime(timex:Long):String?{
        val date= Date(timex *1000L)
        val sdf=SimpleDateFormat("HH:mm",Locale.UK)
        sdf.timeZone=TimeZone.getDefault()
        return sdf.format(date)

    }
}