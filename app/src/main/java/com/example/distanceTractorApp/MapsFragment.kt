package com.example.distanceTractorApp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.distanceTractorApp.databinding.FragmentMapsBinding
import com.example.distanceTractorApp.model.Result
import com.example.distanceTractorApp.service.TrackerService
import com.example.distanceTractorApp.util.Constant
import com.example.distanceTractorApp.util.ExtentionFunction.disable
import com.example.distanceTractorApp.util.ExtentionFunction.enable
import com.example.distanceTractorApp.util.ExtentionFunction.hide
import com.example.distanceTractorApp.util.ExtentionFunction.show
import com.example.distanceTractorApp.util.MapUtil.calculatedElapsedTime
import com.example.distanceTractorApp.util.MapUtil.calculatedTotalDistance
import com.example.distanceTractorApp.util.MapUtil.setCameraPosition
import com.example.distanceTractorApp.util.Permission
import com.example.distanceTractorApp.util.Permission.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MapsFragment : Fragment(),OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,EasyPermissions.PermissionCallbacks {



    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private  val TAG = "MapsFragment"
    var locationList= mutableListOf<LatLng>()
    var polylineList= mutableListOf<Polyline>()
    var started = MutableLiveData<Boolean>()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var startTime=0L
    private var stoptime=0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
         fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireActivity())
         return binding.root



    }

    override fun onStart() {
        binding.startButton.setOnClickListener {
            onStartButtonClicked()
        }
        binding.stopButton.setOnClickListener {
            onStopButtonClicked()
        }
        binding.resetButton.setOnClickListener {
            onResetButtionClicked()
        }

        super.onStart()
    }

//    override fun onResume() {
//        binding.hintText.hide();
//        super.onResume()
//    }

    @SuppressLint("MissingPermission")
    private fun onResetButtionClicked() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnowenLocation=LatLng(it.result.latitude,it.result.longitude)
            for(polyLine in polylineList){
                polyLine.remove()
            }
            map.animateCamera(CameraUpdateFactory.newCameraPosition(setCameraPosition(lastKnowenLocation)))
            locationList.clear()
            binding.resetButton.hide()
            binding.startButton.enable()
            binding.startButton.show()

        }


    }

    private fun onStopButtonClicked() {
        stopForegroundSerive()
        binding.startButton.show()

    }

    private fun onStartButtonClicked() {
         if(Permission.hasBackgroundLocationPermission(requireContext())){
             if(Permission.isLocationEnabled(requireContext())){
                 startCountdown()
                 binding.startButton.hide()
                 binding.startButton.disable()
                 binding.stopButton.show()

             }
             else{
                 val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                 startActivity(intent)
             }


             }
        else{
             requestBackgroundLocationPermission(this)

         }

    }



    private fun sendActionCommandToService(s: String){
        Log.d(TAG, "sendActionCommandToService() called with: s = $s")
        Intent(requireContext(),TrackerService::class.java).apply {
            this.action=s
            requireContext().startService(this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady() called with: googleMap = $googleMap")
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.uiSettings.apply {

            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }
        observeTrackerService()


    }

    private fun observeTrackerService(){

        TrackerService.locationList.observe(viewLifecycleOwner,{
            if(it!=null){
               locationList =it
                if(locationList.size>1){
                    binding.stopButton.enable()
                    binding.startButton.hide()
                }
                drawPollyLine()
                followPollyLine()

            }
        })
        TrackerService.startTime.observe(viewLifecycleOwner,{
            startTime=it
        })

        TrackerService.stopTime.observe(viewLifecycleOwner,{
            stoptime=it
            if(stoptime!=0L){
                showBiggerPicture()
                displayReult()
            }
        })
        TrackerService.started.observe(viewLifecycleOwner,{
            started.value=it
            if(started.value==true){
                binding.startButton.hide()
                binding.stopButton.enable()
            }

        })

    }

    private fun drawPollyLine(){
    val polyline=    map.addPolyline(PolylineOptions().apply {
            width(10f)
            color(android.graphics.Color.BLUE)
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(ButtCap())
            addAll(locationList)



        })
        polylineList.add(polyline)
    }

    private fun stopForegroundSerive(){
        binding.startButton.disable()
        sendActionCommandToService(Constant.ACTION_SERVICE_STOP)
    }
    private fun followPollyLine(){
        if(locationList.isNotEmpty()){
            Log.d(TAG, "followPollyLine() called")
            map.animateCamera((CameraUpdateFactory.newCameraPosition(setCameraPosition(locationList.last()))),500,null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(
            TAG,
            "onViewCreated() called with: view = $view, savedInstanceState = $savedInstanceState"
        )
        val mapsFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapsFragment.getMapAsync(this)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.hintText.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.startButton.show()

        }
        return false

    }


    private fun startCountdown() {
        binding.textTimer.show()
        binding.stopButton.disable()
      val timer:CountDownTimer=object :CountDownTimer(4000,1000){
          override fun onTick(millisUntilFinished: Long) {
              binding.textTimer.show()
              binding.stopButton.disable()
              val currentSecond=millisUntilFinished/1000
              if(currentSecond.toString()=="0"){
                  binding.textTimer.text=getString(R.string.go)
                  binding.textTimer.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
              }
              else{
                  binding.textTimer.text=currentSecond.toString()
                  binding.textTimer.setTextColor(ContextCompat.getColor(requireContext(),R.color.red))
              }
          }

          override fun onFinish() {
             sendActionCommandToService(Constant.ACTION_SERVICE_START)
              binding.textTimer.hide()
          }

      }
        timer.start()

    }

    private fun showBiggerPicture(){
        val bound= LatLngBounds.Builder()
        if(locationList.size!=0) {
            for (location in locationList) {
                bound.include(location)
            }
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bound.build(), 500), 1000, null)
        }
    }

    private fun displayReult() {
        val result = Result(
            calculatedTotalDistance(locationList),
            calculatedElapsedTime(startTime, stoptime)
        )
        lifecycleScope.launch {
            delay(1000)
        val direction= MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
          findNavController().navigate(direction)
            binding.resetButton.show()


        }

        binding.stopButton.hide()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }








}