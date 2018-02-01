package com.github.zemke.tippspiel2.view.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.zemke.tippspiel2.view.util.DataTransferObject

@DataTransferObject
class ScoreEventUpdateDto(
        @JsonProperty("Score") val score: List<String>
) : EventUpdateDto()
