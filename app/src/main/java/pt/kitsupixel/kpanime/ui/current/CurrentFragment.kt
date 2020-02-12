package pt.kitsupixel.kpanime.ui.current

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ShowItemAdapter
import pt.kitsupixel.kpanime.adapters.ShowItemClickListener
import pt.kitsupixel.kpanime.databinding.CurrentFragmentBinding
import pt.kitsupixel.kpanime.domain.Show
import timber.log.Timber
import java.util.*


class CurrentFragment : Fragment() {

    companion object {
        fun newInstance() =
            CurrentFragment()
    }

    private val viewModel: CurrentViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, CurrentViewModel.Factory(activity.application))
            .get(CurrentViewModel::class.java)
    }

    private lateinit var binding: CurrentFragmentBinding

    private lateinit var viewModelAdapter: ShowItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.current_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setClickListener()

        setRecyclerView()

        setSwipeRefresh()

        return binding.root
    }

    private fun setRecyclerView() {
        binding.currentRecyclerView.apply {
            setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        setAdapterToShows()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun setAdapterToShows() {
        viewModel.shows.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapter.submitList(shows)
            }
        })
    }

    private fun setClickListener() {
        viewModelAdapter = ShowItemAdapter(ShowItemClickListener { showId ->
            Navigation.findNavController(this.view!!)
                .navigate(
                    CurrentFragmentDirections.actionGlobalDetailActivity()
                        .setShowId(showId)
                )
        })
    }

    private fun setSwipeRefresh() {
        binding.currentSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val myActionMenuItem: MenuItem? = menu.findItem(R.id.app_bar_search)
        myActionMenuItem?.isVisible = true
        val searchView = myActionMenuItem?.actionView as android.widget.SearchView

        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
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

}
