package com.github.zemke.tippspiel2.service

import com.github.zemke.tippspiel2.persistence.model.Fixture
import com.github.zemke.tippspiel2.persistence.model.Standing
import com.github.zemke.tippspiel2.persistence.repository.BetRepository
import com.github.zemke.tippspiel2.persistence.repository.StandingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
open class StandingService(
        @Autowired private val standingRepository: StandingRepository,
        @Autowired private val betRepository: BetRepository
) {

    /**
     * Update standings based on the new fixtures.
     *
     * TODO Make sure fixtures are processed only once. Maybe a `processed` flag?
     * TODO Make sure this method is only called for fixtures which have ended.
     * TODO What to do when there are changes to fixtures after they were believed to have ended?
     */
    @Transactional
    open fun updateByFixtures(fixtures: List<Fixture>): List<Standing> {
        @Suppress("NAME_SHADOWING")
        val fixtures = fixtures.filter { it.goalsHomeTeam != null && it.goalsAwayTeam != null }

        if (fixtures.isEmpty()) return emptyList()

        val bets = betRepository.findByFixtureIn(fixtures)

        val standingsAffectedByBets = standingRepository.findByUserInAndBettingGameIn(
                bets.map { it.user }.distinct(),
                bets.map { it.bettingGame }.distinct())

        fixtures.forEach { fixture ->
            val betsForFixture = bets.filter { bet -> fixture == bet.fixture }

            betsForFixture.forEach { betForFixture ->
                val standingForBet = standingsAffectedByBets.find { standing ->
                    standing.user == betForFixture.user
                            && standing.bettingGame == betForFixture.bettingGame
                }!!

                val pointsForBet = calcPoints(
                        betForFixture.goalsHomeTeamBet, betForFixture.goalsAwayTeamBet,
                        fixture.goalsHomeTeam!!, fixture.goalsAwayTeam!!
                )
                standingForBet.points += pointsForBet
                changeStatsByNewPoints(standingForBet, pointsForBet)
            }
        }

        // TODO Change Standing#missedBets for all the users who forgot to place a bet.

        return standingRepository.save(standingsAffectedByBets)
    }

    private fun changeStatsByNewPoints(standing: Standing, pointsForBet: Int) {
        when (pointsForBet) {
            5 -> standing.exactBets.inc()
            3 -> standing.goalDifferenceBets.inc()
            1 -> standing.winnerBets.inc()
            0 -> standing.wrongBets.inc()
        }
    }

    private fun calcPoints(homeBet: Int, awayBet: Int, homeActual: Int, awayActual: Int): Int {
        return if (homeBet == homeActual && awayBet == awayActual) {
            5
        } else if (homeBet - awayBet == homeActual - awayActual) {
            3
        } else if ((homeActual > awayActual && homeBet > awayBet)
                || (homeActual < awayActual && homeBet < awayBet)) {
            1
        } else {
            0
        }
    }
}
