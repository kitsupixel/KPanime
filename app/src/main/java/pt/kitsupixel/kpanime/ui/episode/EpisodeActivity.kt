package pt.kitsupixel.kpanime.ui.episode

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.KPApplication
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.LinkItemAdapter
import pt.kitsupixel.kpanime.adapters.LinkItemClickListener
import pt.kitsupixel.kpanime.databinding.ActivityEpisodeBinding
import pt.kitsupixel.kpanime.domain.Link
import timber.log.Timber

class EpisodeActivity : AppCompatActivity() {

    private val viewModel: EpisodeViewModel by lazy {
        ViewModelProvider(this, EpisodeViewModel.Factory(this.application, showId, episodeId))
            .get(
                EpisodeViewModel::class.java
            )
    }

    private lateinit var binding: ActivityEpisodeBinding

    private lateinit var viewModelAdapter: LinkItemAdapter

    private var showId: Long = 0L
    private var episodeId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the slide in animation
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_episode)

        showId = intent.getLongExtra("showId", 0L)
        episodeId = intent.getLongExtra("episodeId", 0L)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setClickListeners()

        setRecyclerView()

        setSwipeRefresh()

        if (!BuildConfig.noAds) {
            val lastAdShown = (application as KPApplication).getTimeLastAd()
            val now = System.currentTimeMillis()
            if (lastAdShown + 60000 < now) {
                setInterstitialAd()
                (application as KPApplication).setTimeLastAd()
            }
        }
    }

    private fun setRecyclerView() {
        binding.episodeRecyclerView.apply {
            setHasFixedSize(true)

            layoutManager =
                if (this.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT)
                    GridLayoutManager(context, 2)
                else
                    GridLayoutManager(context, 4)
            adapter = viewModelAdapter
        }

        viewModel.links.observe(this, Observer { links ->
            links?.apply {
                viewModelAdapter.submitList(links)
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

    private fun setClickListeners() {
        viewModelAdapter =
            LinkItemAdapter(LinkItemClickListener { link ->
                linkClicked(link)
            })
    }

    private fun linkClicked(link: Link) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(link.link)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Service unavailable", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setSwipeRefresh() {
        binding.episodeSwipeRefresh.setOnRefreshListener {
            Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }

        viewModel.refreshing.observe(this, Observer { refreshing ->
            refreshing?.apply {
                binding.episodeSwipeRefresh.isRefreshing = refreshing
            }
        })
    }

    private fun setInterstitialAd() {
        val mInterstitialAd = InterstitialAd(this)

        if (BuildConfig.AdmobTest) {
            Timber.i("Using test ad")
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712" // Test Ads
        }

        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mInterstitialAd.show()
            }
        }
    }
}
