package pt.kitsupixel.kpanime.domain

import org.joda.time.DateTime

data class Episode(
    val id: Long,
    val show_id: Long,
    val number: String,
    val released_on: String
) {
    fun toLocalDate(): String {
        return DateTime.parse(this.released_on).toLocalDate().toString()
    }

}