package pt.kitsupixel.kpanime.adapters

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.domain.Episode


@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?) {
    view.visibility = if (it != null) View.GONE else View.VISIBLE
}


@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, it: Any?) {
    view.visibility = if (it == null) View.GONE else View.VISIBLE
}

@BindingAdapter("disabledIfNull")
fun disabledIfNull(view: View, it: Any?) {
    view.isEnabled = it != null
}


@BindingAdapter("goneIfFalse")
fun goneIfNotFalse(view: View, it: Any?) {
    view.visibility = if (it == false) View.GONE else View.VISIBLE
}

@BindingAdapter("goneIfTrue")
fun goneIfTrue(view: View, it: Any?) {
    view.visibility = if (it == true) View.GONE else View.VISIBLE
}

@BindingAdapter("goneIfEmpty")
fun goneIfEmpty(view: View, it: List<Any>?) {
    view.visibility = if (it != null) {
        if (it.size > 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("imageCardUrl")
fun setImageCardUrl(imageView: ImageView, url: String) {
    Glide.with(imageView.context)
        .load(url)
        .apply(
            RequestOptions()
                .error(R.drawable.ic_broken_image)
                .placeholder(R.drawable.loading_animation)
                .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
        )
        .into(imageView)
}

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    if (url != null) {
        Glide.with(imageView.context)
            .load(url)
            .apply(
                RequestOptions()
                    .fitCenter()
                    .error(R.drawable.ic_broken_image)
                    .placeholder(R.drawable.loading_animation)
            )
            .into(imageView)
    } else {
        Glide.with(imageView.context)
            .load(R.drawable.ic_broken_image)
            .into(imageView)
    }
}

@BindingAdapter("convertTextToHtml")
fun convertTitle(textView: TextView, text: String?) {
    if (text != null) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            textView.text = Html.fromHtml(text)
        }
    }
}

@BindingAdapter("progressIndicator")
fun progressIndicator(progressBar: ProgressBar, value: Int) {
    if (value == 0) progressBar.isIndeterminate = true
    else {
        progressBar.isIndeterminate = false
        progressBar.progress = value
    }
}

@BindingAdapter("progressText")
fun progressText(textView: TextView, value: Int) {
    textView.text = "${value}%"
}

@BindingAdapter("episodeOrBatch")
fun episodeOrBatch(textView: TextView, episode: Episode?) {
    if (episode != null) {
        if (episode.type == "episode") {
            textView.text = String.format(textView.context.resources.getString(R.string.episode_text), episode.number)
        } else {
            textView.text = String.format(textView.context.resources.getString(R.string.batch_text), episode.number)
        }
    }
}