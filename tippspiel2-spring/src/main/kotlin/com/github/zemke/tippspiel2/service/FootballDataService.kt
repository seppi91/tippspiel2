package com.github.zemke.tippspiel2.service

import com.github.zemke.tippspiel2.view.model.FootballDataCompetitionDto
import com.github.zemke.tippspiel2.view.model.FootballDataFixtureWrappedListDto
import com.github.zemke.tippspiel2.view.model.FootballDataTeamWrappedListDto
import org.springframework.http.ResponseEntity

interface FootballDataService {

    fun requestCompetition(competitionId: Long): FootballDataCompetitionDto
    fun requestFixtures(competitionId: Long): FootballDataFixtureWrappedListDto
    fun requestTeams(competitionId: Long): FootballDataTeamWrappedListDto
    fun requestFixturesAsEntity(competitionId: Long): ResponseEntity<FootballDataFixtureWrappedListDto>
}
