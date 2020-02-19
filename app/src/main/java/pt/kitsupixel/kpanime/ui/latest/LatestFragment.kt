package pt.kitsupixel.kpanime.ui.latest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ReleaseItemAdapter
import pt.kitsupixel.kpanime.adapters.ReleaseItemClickListener
import pt.kitsupixel.kpanime.databinding.LatestFragmentBinding
import pt.kitsupixel.kpanime.ui.detail.DetailActivity
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber


class LatestFragment : Fragment() {

    companion object {
        fun newInstance() = LatestFragment()
    }

    private val viewModel: LatestViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, LatestViewModel.Factory(activity.application))
            .get(LatestViewModel::class.java)
    }

    private lateinit var binding: LatestFragmentBinding

    private lateinit var viewModelAdapter: ReleaseItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.latest_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        setupView()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    private fun setupView() {
        // Click Listener for Recycler View
        viewModelAdapter = ReleaseItemAdapter(ReleaseItemClickListener { showId, _ ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })

        // Initialize Recycler View
        binding.latestRecyclerView.apply {
            setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.episodes.observe(viewLifecycleOwner, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }

            if (BuildConfig.Logging) Timber.i(episodes.size.toString())
        })

        // Set action of swipe to refresh
        binding.latestSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
    }

}
