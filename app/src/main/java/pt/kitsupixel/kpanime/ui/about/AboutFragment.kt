package pt.kitsupixel.kpanime.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.AboutFragmentBinding


class AboutFragment : Fragment() {

    companion object {
        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: AboutFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.about_fragment,
            container,
            false
        )

        binding.coffeeButton.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse("https://paypal.me/kitsupixel")
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, resources.getString(R.string.service_unavailable), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        return binding.root
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
