package pt.kitsupixel.kpanime.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import pt.kitsupixel.kpanime.KPApplication
import pt.kitsupixel.kpanime.R
import timber.log.Timber


class settingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = settingsFragment()
    }

    private val THEME_PREFERENCE = "theme_preference"
    private lateinit var application: KPApplication

    lateinit var preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        application = KPApplication()

        preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Timber.i("SharedPreferences changed: %s", key)
            if (key == THEME_PREFERENCE) {
                application.setAppTheme(sharedPreferences.getString(key, "system"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.VISIBLE
    }
}
