package pt.kitsupixel.kpanime.ui.detail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.EpisodeItemAdapter
import pt.kitsupixel.kpanime.adapters.EpisodeItemClickListener
import pt.kitsupixel.kpanime.databinding.ActivityDetailBinding
import pt.kitsupixel.kpanime.ui.episode.EpisodeActivity
import timber.log.Timber

class DetailActivity : AppCompatActivity() {

    private val viewModel: DetailViewModel by lazy {
        ViewModelProvider(this, DetailViewModel.Factory(this.application, showId)).get(
            DetailViewModel::class.java
        )
    }


    private val args: DetailActivityArgs by navArgs()

    private lateinit var binding: ActivityDetailBinding

    private lateinit var viewModelAdapter: EpisodeItemAdapter

    private var showId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the slide in animation
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        showId = args.showId

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setClickListener()

        setRecyclerView()
    }

    private fun setRecyclerView() {
        binding.detailEpisodesRecyclerview.apply {
            //setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.episodes.observe(this, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }
        })
    }

    private fun setClickListener() {
        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                startActivity(
                    Intent(this, EpisodeActivity::class.java)
                        .putExtra("showId", showId)
                        .putExtra("episodeId", episodeId)
                )
            })

        viewModel.show.observe(this, Observer { show ->
            when (show?.favorite) {
                true -> binding.favouriteFab.setImageResource(R.drawable.ic_favourite)
                else -> binding.favouriteFab.setImageResource(R.drawable.ic_unfavourite)
            }
            // Workaround for bug on lib 28.0.0
            binding.favouriteFab.hide()
            binding.favouriteFab.show()
        })

        viewModel.eventFavorite.observe(this, Observer { isFavourite ->
            if (isFavourite != null) {
                if (isFavourite == true)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        "This show was added to your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isFavourite == false)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        "This show was removed from your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("favourite is $isFavourite")

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
}
