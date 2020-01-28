package pt.kitsupixel.kpanime.ui.latest

import android.content.Intent
import android.content.res.Configuration
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ReleaseItemAdapter
import pt.kitsupixel.kpanime.adapters.ReleaseItemClickListener
import pt.kitsupixel.kpanime.databinding.LatestFragmentBinding
import pt.kitsupixel.kpanime.domain.Show
import pt.kitsupixel.kpanime.ui.showdetail.ShowDetailActivity
import timber.log.Timber


class LatestFragment : Fragment() {

    companion object {
        fun newInstance() = LatestFragment()
    }

    private val viewModel: LatestViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, LatestViewModel.Factory(activity.application))
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

        viewModelAdapter = ReleaseItemAdapter(ReleaseItemClickListener { showId ->
            showClicked(showId)
        })

        viewModel.navigateEvent.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showClicked(it)
            }
        })

        binding.root.findViewById<RecyclerView>(R.id.latest_recycler_view).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.episodes.observe(viewLifecycleOwner, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }

            Timber.i(episodes.size.toString())
        })
        setHasOptionsMenu(true)
    }

    private fun showClicked(showId: Long) {
        val intent = Intent(this.context, ShowDetailActivity::class.java)

        val args = Bundle()
        args.putLong("showId", showId)
        intent.putExtras(args)

        startActivity(intent)

        viewModel.navigateEventClear()
    }

}
