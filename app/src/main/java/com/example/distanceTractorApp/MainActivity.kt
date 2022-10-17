package com.example.distanceTractorApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.distanceTractorApp.util.Permission

class MainActivity : AppCompatActivity() {
    private  lateinit var navController: NavController;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController=findNavController(R.id.my_nav_host_fragment)
        if(Permission.hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)

        }
    }
}