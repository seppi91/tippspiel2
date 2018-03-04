package com.github.zemke.tippspiel2.service

import com.github.zemke.tippspiel2.persistence.model.Fixture
import com.github.zemke.tippspiel2.persistence.model.Standing
import com.github.zemke.tippspiel2.persistence.repository.BetRepository
import com.github.zemke.tippspiel2.persistence.repository.StandingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class StandingService(
        @Autowired private val standingRepository: StandingRepository,
        @Autowired private val betRepository: BetRepository
) {

    /**
     * Update standings based on the new fixtures.
     */
    fun updateByFixtures(fixtures: List<Fixture>): List<Standing> {
//        val bets = betRepository.findByFixtureIn(fixtures)
//
//        bets
//                .map { it. }

        return emptyList<Standing>()
    }
}
