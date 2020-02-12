package pt.kitsupixel.kpanime.ui.latest

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

        setClickListener()

        setRecyclerView()

        setSwipeRefresh()

        return binding.root
    }

    private fun setRecyclerView() {
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    private fun setClickListener() {
        viewModelAdapter = ReleaseItemAdapter(ReleaseItemClickListener { showId ->
            Navigation.findNavController(this.view!!)
                .navigate(
                    LatestFragmentDirections.actionGlobalDetailActivity()
                        .setShowId(showId)
                )
        })
    }

    private fun setSwipeRefresh() {
        binding.latestSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
    }

}
