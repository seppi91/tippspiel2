package com.github.zemke.tippspiel2.view.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.zemke.tippspiel2.persistence.model.enumeration.FixtureStatus
import com.github.zemke.tippspiel2.view.util.DataTransferObject

@DataTransferObject
class StatusEventUpdateDto(
        @JsonProperty("Status") val status: List<FixtureStatus>
) : EventUpdateDto()
