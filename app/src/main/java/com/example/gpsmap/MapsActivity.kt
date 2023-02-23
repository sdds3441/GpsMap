package com.example.gpsmap

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gpsmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    val PERM_FLAG=99
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isPermitted()){
            startProcess()
        }else{
            ActivityCompat.requestPermissions(this,permissions,PERM_FLAG)
        }

    }

    fun isPermitted():Boolean{

        for(perm in permissions){
            if (ContextCompat.checkSelfPermission(this,perm)!=PERMISSION_GRANTED){
                return false
            }
        }

        return true
    }

    fun startProcess(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
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

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

        setUpdateLocationListener()

        }

    //위치정보 받아오기
    lateinit var fusedLocationClient:FusedLocationProviderClient
    lateinit var locationCallback:LocationCallback

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority=LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    for ((i, location) in it.locations.withIndex()) {
                        Log.d("로케이션", "$i ${location.latitude},${location.longitude}}")
                        setLastLocation(location)
                    }
                }
            }

        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
    fun setLastLocation(location: Location){
        val myLocation=LatLng(location.latitude,location.longitude)
        val marker=MarkerOptions()
            .position(myLocation)
            .title("I am here!")
        val cameraOption=CameraPosition.Builder()
            .target(myLocation)
            .zoom(15.0f)
            .build()
        val camera=CameraUpdateFactory.newCameraPosition(cameraOption)

        mMap.clear()

        mMap.addMarker(marker)
        mMap.moveCamera(camera)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERM_FLAG->{
                var check=true
                for(grant in grantResults){
                    if (grant!= PERMISSION_GRANTED){
                    check = false
                    break
                    }
                }
                if (check){
                    startProcess()
                }else{
                    Toast.makeText(this,"권한을 승인해야지만 앱을 사용할 수 있습니다.",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}