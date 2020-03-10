package pt.kitsupixel.kpanime.adapters

import android.graphics.PorterDuff
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
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
        if (it.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    } else {
        View.GONE
    }
}

@BindingAdapter("imageCardUrl")
fun setImageCardUrl(imageView: ImageView, url: String) {
    imageView.load(url) {
        crossfade(true)
        placeholder(R.drawable.loading_animation)
    }
}

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    imageView.load(url) {
        crossfade(true)
        placeholder(R.drawable.loading_animation)
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
            textView.text = String.format(
                textView.context.resources.getString(R.string.episode_text),
                episode.number
            )
        } else {
            textView.text = String.format(
                textView.context.resources.getString(R.string.batch_text),
                episode.number
            )
        }
    }
}

@BindingAdapter("episodeImageButtonTint")
fun episodeImageButtonTint(imageButton: ImageButton, value: Boolean?) {
    imageButton.colorFilter = null
    if (value != null && value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            imageButton.setColorFilter(
                imageButton.context.resources.getColor(
                    when (imageButton.id) {
                        R.id.download_image_button -> R.color.primaryColor
                        else -> R.color.watched
                    }, null
                ),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }
}