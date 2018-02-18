package com.github.zemke.tippspiel2.persistence.model.enumeration

/**
 * [https://www.football-data.org/blog](https://www.football-data.org/blog)
 */
enum class FixtureStatus {
    SCHEDULED,
    TIMED,
    IN_PLAY,
    FINISHED,
    POSTPONED,
    CANCELED;

    companion object {

        fun finalStatuses() = listOf(CANCELED, FINISHED)
    }
}
