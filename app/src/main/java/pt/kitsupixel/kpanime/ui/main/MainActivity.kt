package pt.kitsupixel.kpanime.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var lastAdShown: Long = 0L

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

    fun hideActionBar() {
        supportActionBar?.hide()
        binding.navView.visibility = View.GONE
    }

    fun showActionBar() {
        supportActionBar?.show()
        binding.navView.visibility = View.VISIBLE
    }

    fun getTimeLastAd(): Long {
        return lastAdShown
    }

    fun setTimeLastAd() {
        lastAdShown = System.currentTimeMillis()
    }
}
