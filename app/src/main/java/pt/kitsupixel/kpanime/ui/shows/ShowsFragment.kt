package pt.kitsupixel.kpanime.ui.shows

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.MainNavDirections
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ShowItemAdapter
import pt.kitsupixel.kpanime.adapters.ShowItemClickListener
import pt.kitsupixel.kpanime.databinding.ShowsFragmentBinding
import pt.kitsupixel.kpanime.domain.Show
import java.util.*


class ShowsFragment : Fragment() {

    companion object {
        fun newInstance() = ShowsFragment()
    }

    private val viewModel: ShowsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, ShowsViewModel.Factory(activity.application))
            .get(ShowsViewModel::class.java)
    }

    private lateinit var binding: ShowsFragmentBinding

    private lateinit var viewModelAdapter: ShowItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.shows_fragment,
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

        binding.root.findViewById<RecyclerView>(R.id.shows_recycler_view).apply {
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
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val myActionMenuItem: MenuItem? = menu.findItem(R.id.app_bar_search)
        myActionMenuItem?.isVisible = true
        val searchView = myActionMenuItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterResults(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterResults(newText)
                return true
            }
        })
    }

    private fun setAdapterToShows() {
        viewModel.shows.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapter.submitList(shows)
            }
        })
    }

    fun filterResults(query: String?) {
        if (query != null || query != "") {
            viewModel.shows.removeObservers(viewLifecycleOwner)

            val queryLower = query?.toLowerCase(Locale.getDefault()).toString()
            val filteredList: MutableList<Show> = mutableListOf()
            val currentShows = viewModel.shows.value
            if (currentShows != null) {
                for (show in currentShows) {
                    if (show.title.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                        filteredList.add(show)
                    }
                }
            }

            viewModelAdapter.submitList(filteredList)
        } else {
            setAdapterToShows()
        }
    }

    private fun showClicked(showId: Long) {
        val directions = MainNavDirections.actionGlobalDetailFragment().setShowId(showId)
        Navigation.findNavController(this.view!!).navigate(directions)
        viewModel.navigateEventClear()
    }

}

