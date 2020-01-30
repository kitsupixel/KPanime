package pt.kitsupixel.kpanime.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import pt.kitsupixel.kpanime.MainNavDirections
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ShowItemAdapter
import pt.kitsupixel.kpanime.adapters.ShowItemClickListener
import pt.kitsupixel.kpanime.databinding.HomeFragmentBinding


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, HomeViewModel.Factory(activity.application))
            .get(HomeViewModel::class.java)
    }

    private lateinit var binding: HomeFragmentBinding

    private lateinit var viewModelAdapter: ShowItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.home_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModelAdapter = ShowItemAdapter(ShowItemClickListener { showId ->
            showClicked(showId)
        })

        viewModel.navigateEvent.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showClicked(it)
            }
        })

        binding.homeRecyclerView.apply {
            layoutManager =
                if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT)
                    GridLayoutManager(context, 3)
                else
                    GridLayoutManager(context, 5)

            adapter = viewModelAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setAdapterToShows()
    }

    private fun setAdapterToShows() {
        viewModel.shows.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapter.submitList(shows)
            }
        })
    }

    private fun showClicked(showId: Long) {
        val directions = MainNavDirections.actionGlobalDetailFragment().setShowId(showId)
        Navigation.findNavController(this.view!!).navigate(directions)
        viewModel.navigateEventClear()
    }

}
