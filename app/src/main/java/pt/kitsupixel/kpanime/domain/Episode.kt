package pt.kitsupixel.kpanime.domain

import org.joda.time.DateTime

data class Episode(
    val id: Long,
    val show_id: Long,
    val number: String,
    val type: String,
    val released_on: String
) {
    fun releasedToLocalDate(): String {
        return DateTime.parse(this.released_on).toLocalDate().toString()
    }
    fun typeCapitalized(): String {
        return this.type.capitalize()
    }
}