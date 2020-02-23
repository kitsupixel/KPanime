package pt.kitsupixel.kpanime.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.EpisodeItemAdapter
import pt.kitsupixel.kpanime.adapters.EpisodeItemClickListener
import pt.kitsupixel.kpanime.adapters.EpisodeItemDownloadClickListener
import pt.kitsupixel.kpanime.adapters.EpisodeItemWatchedClickListener
import pt.kitsupixel.kpanime.databinding.ActivityDetailBinding
import pt.kitsupixel.kpanime.ui.episode.EpisodeActivity
import timber.log.Timber


class DetailActivity : AppCompatActivity() {

    private val viewModel: DetailViewModel by lazy {
        ViewModelProvider(this, DetailViewModel.Factory(this.application, showId)).get(
            DetailViewModel::class.java
        )
    }


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

        showId = intent.getLongExtra("showId", 0L)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setupViews()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.detail_refresh_menu -> {
                viewModel.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViews() {
        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                startActivity(
                    Intent(this, EpisodeActivity::class.java)
                        .putExtra("showId", showId)
                        .putExtra("episodeId", episodeId)
                )
            }, EpisodeItemDownloadClickListener { episodeId ->
                viewModel.toggleEpisodeDownloaded(episodeId)
            }, EpisodeItemWatchedClickListener { episodeId ->
                viewModel.toggleEpisodeWatched(episodeId)
            }
            )

        viewModel.show.observe(this, Observer { show ->
            when (show?.favorite) {
                true -> binding.favouriteFab.setImageResource(R.drawable.ic_favourite)
                else -> binding.favouriteFab.setImageResource(R.drawable.ic_unfavourite)
            }
            // Workaround for bug on lib 28.0.0
            binding.favouriteFab.hide()
            binding.favouriteFab.show()

            when (show?.watched) {
                true -> binding.watchedFab.setImageResource(R.drawable.ic_watched)
                else -> binding.watchedFab.setImageResource(R.drawable.ic_unwatch)
            }

            if (show?.title != null) {
                this.title = show.title
            }
            // Workaround for bug on lib 28.0.0
            binding.watchedFab.hide()
            binding.watchedFab.show()
        })

        viewModel.eventFavorite.observe(this, Observer { isFavourite ->
            if (isFavourite != null) {
                if (isFavourite == true)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.added_to_favorites),
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isFavourite == false)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.removed_from_favorites),
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("favourite is $isFavourite")

                viewModel.eventFavoriteClear()
            }
        })

        viewModel.eventWatched.observe(this, Observer { isWatched ->
            if (isWatched != null) {
                if (isWatched == true)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.saved_as_watched),
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isWatched == false)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.unsaved_watched),
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("watched is $isWatched")

                viewModel.eventWatchedClear()
            }
        })

        viewModel.eventEpisodeDownloaded.observe(this, Observer { isDownloaded ->
            if (isDownloaded != null) {
                if (isDownloaded == true)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.save_episode_downloaded),
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isDownloaded == false)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.unsave_episode_downloaded),
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("downloaded episode is $isDownloaded")

                viewModel.eventEpisodeDownloadedClear()
            }
        })

        viewModel.eventEpisodeWatched.observe(this, Observer { isWatched ->
            if (isWatched != null) {
                if (isWatched == true)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.saved_episode_watched),
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isWatched == false)
                    Snackbar.make(
                        this.findViewById(android.R.id.content)!!,
                        resources.getString(R.string.unsave_episode_watched),
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("watched episode is $isWatched")

                viewModel.eventEpisodeWatchedClear()
            }
        })

        binding.detailEpisodesRecyclerview.apply {
            //setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.episodes.observe(this, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }
        })

        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        ViewCompat.requestApplyInsets(binding.detailCordinatorLayout)
        ViewCompat.setNestedScrollingEnabled(binding.detailEpisodesRecyclerview, false)

        val adView: AdView = binding.adView
        val adRequest: AdRequest = AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build()

        adView.loadAd(adRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
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
