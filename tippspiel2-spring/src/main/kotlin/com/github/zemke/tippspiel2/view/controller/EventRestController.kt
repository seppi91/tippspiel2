package com.github.zemke.tippspiel2.view.controller

import com.github.zemke.tippspiel2.view.model.event.EventDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/events")
class EventRestController {

    @PostMapping("")
    fun receiveEvent(@RequestBody event: EventDto): ResponseEntity<Void> {
        if (event.resource == EventDto.EventResourceTarget.COMPETITION) {

        } else if (event.resource == EventDto.EventResourceTarget.FIXTURE) {

        } else {

        }

        TODO()
    }
}
