package pt.kitsupixel.kpanime.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.ui.current.CurrentFragment
import pt.kitsupixel.kpanime.ui.home.HomeFragment
import pt.kitsupixel.kpanime.ui.latest.LatestFragment
import pt.kitsupixel.kpanime.ui.shows.ShowsFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_home,
    R.string.tab_latest,
    R.string.tab_current,
    R.string.tab_shows
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> HomeFragment()
            1 -> HomeFragment()//LatestFragment()
            2 -> CurrentFragment()
            else -> ShowsFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}