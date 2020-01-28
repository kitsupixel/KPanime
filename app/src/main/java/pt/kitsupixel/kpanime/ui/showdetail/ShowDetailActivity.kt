package pt.kitsupixel.kpanime.ui.showdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.EpisodeItemAdapter
import pt.kitsupixel.kpanime.adapters.EpisodeItemClickListener
import pt.kitsupixel.kpanime.databinding.ActivityShowDetailBinding
import pt.kitsupixel.kpanime.ui.shows.ShowsViewModel
import timber.log.Timber

class ShowDetailActivity : AppCompatActivity() {

    private val viewModel: ShowDetailViewModel by lazy {
        ViewModelProviders.of(this, ShowDetailViewModel.Factory(this.application, showId))
            .get(ShowDetailViewModel::class.java)
    }

    private lateinit var binding: ActivityShowDetailBinding

    private lateinit var viewModelAdapter: EpisodeItemAdapter

    private var showId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )

        showId = intent.getLongExtra("showId", 0L)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_show_detail
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                episodeClicked(episodeId)
            })

        binding.root.findViewById<RecyclerView>(R.id.show_episodes_recyclerview).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        viewModel.episodes.observe(this, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }
        })

        viewModel.eventFavorite.observe(this, Observer { isFavourite ->
            if (isFavourite != null) {
                if (isFavourite == true)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "This show was added to your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isFavourite == false)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "This show was removed from your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()

                Timber.i("favourite is $isFavourite")

                viewModel.eventFavoriteClear()
            }
        })
    }

    override fun finish() {
        super.finish()
        // Slide out animation
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

    private fun episodeClicked(episodeId: Long) {
        Toast.makeText(this.applicationContext, "Episode $episodeId clicked", Toast.LENGTH_SHORT)
            .show()
    }
}
