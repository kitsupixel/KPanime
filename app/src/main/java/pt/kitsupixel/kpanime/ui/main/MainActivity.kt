package pt.kitsupixel.kpanime.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.ActivityMainBinding
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupNavigation()

        setSupportActionBar(binding.toolbar)
    }

    /**
     * Setup Navigation for this Activity
     */
    private fun setupNavigation() {
        val navController = this.findNavController(R.id.nav_host_fragment)
        val bottomNavigation: BottomNavigationView = binding.navView

        NavigationUI.setupWithNavController(bottomNavigation, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }


    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }
}
