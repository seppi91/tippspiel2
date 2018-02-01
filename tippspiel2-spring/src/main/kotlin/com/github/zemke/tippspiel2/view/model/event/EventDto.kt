package com.github.zemke.tippspiel2.view.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.zemke.tippspiel2.view.util.DataTransferObject
import java.util.*


@DataTransferObject
data class EventDto(
        @JsonProperty("Timestamp") val timestamp: Date,
        @JsonProperty("Resource") val resource: EventResourceTarget,
        @JsonProperty("Id") val id: Long,
        @JsonProperty("URI") val uRI: String,
        @JsonProperty("Updates") val updates: List<EventUpdateDto>
) {

    // TODO They're actually received from footbal-data.org as first-upper-remaining-lowercased.
    enum class EventResourceTarget {
        COMPETITION, FIXTURE
    }
}
