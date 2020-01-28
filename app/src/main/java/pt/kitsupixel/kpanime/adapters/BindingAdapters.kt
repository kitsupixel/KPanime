package pt.kitsupixel.kpanime.adapters

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import pt.kitsupixel.kpanime.R


@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?) {
    view.visibility = if (it != null) View.GONE else View.VISIBLE
}

@BindingAdapter("goneIfFalse")
fun goneIfNotFalse(view: View, it: Any?) {
    view.visibility = if (it == false) View.GONE else View.VISIBLE
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
                .override(135, 180) // resizes the image to these dimensions (in pixel)
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
