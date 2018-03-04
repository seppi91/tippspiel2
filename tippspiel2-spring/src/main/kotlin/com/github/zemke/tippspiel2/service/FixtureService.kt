package com.github.zemke.tippspiel2.service

import com.github.zemke.tippspiel2.persistence.model.Competition
import com.github.zemke.tippspiel2.persistence.model.Fixture
import com.github.zemke.tippspiel2.persistence.model.enumeration.FixtureStatus
import com.github.zemke.tippspiel2.persistence.repository.FixtureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FixtureService(
        @Autowired private var fixtureRepository: FixtureRepository
) {

    fun saveMany(fixtures: List<Fixture>): List<Fixture> = fixtureRepository.save(fixtures)

    fun findUnfinishedFixtures() = fixtureRepository.findByStatusNotIn(FixtureStatus.finalStatuses())

    fun findFixturesByCompetitionAndManualFalse(competition: Competition) = fixtureRepository.findByCompetitionAndManualFalse(competition)

    fun getById(fixtureId: Long): Fixture? = fixtureRepository.findOne(fixtureId)
}
