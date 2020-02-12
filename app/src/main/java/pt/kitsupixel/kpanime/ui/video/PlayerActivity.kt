package pt.kitsupixel.kpanime.ui.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.videolan.libvlc.util.VLCVideoLayout
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.ActivityPlayerBinding
import timber.log.Timber


class PlayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by lazy {
        ViewModelProvider(this, PlayerViewModel.Factory(this.application)).get(
            PlayerViewModel::class.java
        )
    }

    lateinit var binding: ActivityPlayerBinding

    lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        filePath = intent.getStringExtra("filePath") ?: ""

        Timber.i("FileName: $filePath")
    }

    override fun onStart() {
        super.onStart()

        if (filePath != "") {
            viewModel.initializeVideo(binding.videoLayout, filePath)
        }
    }


}
