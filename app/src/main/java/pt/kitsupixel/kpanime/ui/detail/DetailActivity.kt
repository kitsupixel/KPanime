package pt.kitsupixel.kpanime.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.appbar.CollapsingToolbarLayout
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

    private fun setupViews() {
        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                startActivity(Intent(this, EpisodeActivity::class.java)
                    .putExtra("showId", showId)
                    .putExtra("episodeId", episodeId)
                )
            })

        viewModel.show.observe(this, Observer { show ->
            when (show?.favorite) {
                true -> binding.favouriteFab.setImageResource(R.drawable.ic_favourite)
                else -> binding.favouriteFab.setImageResource(R.drawable.ic_unfavourite)
            }

            if (show?.title != null) {
                this.title = show.title
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
