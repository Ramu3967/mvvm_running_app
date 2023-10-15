package com.example.myapplication.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.util.RunConstants.hide
import com.example.myapplication.util.RunConstants.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            // changing to material toolbar
            setSupportActionBar(toolbar)
            // unable to find it directly using View Binding, hence fm was used
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            bottomNavigationView.setupWithNavController(navHostFragment.navController)

            // making the bottom_nav_view visible for just the 3/5 fragments
            navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id){
                    R.id.runFragment, R.id.statisticsFragment, R.id.settingsFragment -> bottomNavigationView.show()
                    else->bottomNavigationView.hide()
                }
            }
        }
    }
}