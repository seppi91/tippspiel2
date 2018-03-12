package com.github.zemke.tippspiel2.integration

import com.github.zemke.tippspiel2.core.properties.FootballDataProperties
import com.github.zemke.tippspiel2.persistence.model.Competition
import com.github.zemke.tippspiel2.persistence.model.Fixture
import com.github.zemke.tippspiel2.persistence.model.Team
import com.github.zemke.tippspiel2.service.FixtureService
import com.github.zemke.tippspiel2.service.FootballDataService
import com.github.zemke.tippspiel2.service.StandingService
import com.github.zemke.tippspiel2.view.model.FootballDataFixtureDto
import com.github.zemke.tippspiel2.view.model.FootballDataFixtureWrappedListDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.dsl.IntegrationFlowAdapter
import org.springframework.integration.dsl.IntegrationFlowDefinition
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.util.DynamicPeriodicTrigger
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class FootballDataIntegrationFlowAdapter : IntegrationFlowAdapter() {

    @Autowired
    private lateinit var footballDataService: FootballDataService


    @Autowired
    private lateinit var footballDataProperties: FootballDataProperties

    @Autowired
    private lateinit var fixtureService: FixtureService

    @Autowired
    private lateinit var standingService: StandingService

    companion object {

        private const val COMPETITION_HEADER_NAME = "competition"

        // TODO Get to know Spring Boot logging best practices.
        // TODO Also implement sophisticated logging for for this Integraion Flow.
        private val LOGGER = Logger.getLogger(FootballDataIntegrationFlowAdapter::class.java.name)
    }

    override fun buildFlow(): IntegrationFlowDefinition<*> =
            IntegrationFlows
                    .from(this@FootballDataIntegrationFlowAdapter::footballDataAdapter, { Pollers.trigger(footballDataTrigger()) })
                    .handle({ it: Message<FootballDataFixtureWrappedListDto> -> footballDataServiceActivator(it) })


    @Bean
    private fun footballDataAdapter(): Message<FootballDataFixtureWrappedListDto> {
        LOGGER.info("Inbounding")

        val competition: Competition? = null // TODO Get active competition.
        val fixtureResponseEntities = footballDataService.requestFixturesAsEntity(competition!!.id)

        return MessageBuilder.createMessage(
                fixtureResponseEntities.body,
                createFootballDataMessageHeaders(
                        competition,
                        fixtureResponseEntities.headers[footballDataProperties.requestsTillResetHeader],
                        fixtureResponseEntities.headers[footballDataProperties.secondsTillResetHeader])
        )
    }

    @Bean
    private fun footballDataTrigger(): DynamicPeriodicTrigger = DynamicPeriodicTrigger(0)

    @ServiceActivator
    private fun footballDataServiceActivator(message: Message<FootballDataFixtureWrappedListDto>) {
        val messageHeaderAccessor = IntegrationMessageHeaderAccessor(message)
        val competition = messageHeaderAccessor.getHeader(COMPETITION_HEADER_NAME, Competition::class.java)
                ?: throw Exception("There is no competition header.")

        val fixtures = fixtureService.findFixturesByCompetitionAndManualFalse(competition)
        val teams = fixtures.fold(arrayListOf()) { acc: ArrayList<Team>, fixture: Fixture ->
            with(acc) { addAll(listOf(fixture.homeTeam, fixture.awayTeam)); this }
        }

        val fixturesToSave = message.payload.fixtures
                .map { FootballDataFixtureDto.fromDto(it, teams, competition) }
                .filter { fixtures.contains(it) }

        if (fixturesToSave.isNotEmpty()) {
            fixtureService.saveMany(fixturesToSave)
            standingService.updateStandings()
        }

        val requestsTillResetHeaderValue = messageHeaderAccessor.getHeader(
                footballDataProperties.requestsTillResetHeader, Int::class.java)
        val secondsTillResetHeaderValue = messageHeaderAccessor.getHeader(
                footballDataProperties.secondsTillResetHeader, Int::class.java)

        val millisUntilNextPoll = if (requestsTillResetHeaderValue == null || secondsTillResetHeaderValue == null) {
            LOGGER.warning("Falling back to polling with interval of ${footballDataProperties.fallbackPollingInterval}")
            footballDataProperties.fallbackPollingInterval
        } else {
            (requestsTillResetHeaderValue / secondsTillResetHeaderValue) * 1000
        }

        footballDataTrigger().period = millisUntilNextPoll.toLong()
    }

    private fun createFootballDataMessageHeaders(competition: Competition, requestsTillReset: List<String>?,
                                                 secondsTillReset: List<String>?): MessageHeaders {
        return MessageHeaders(mapOf(
                Pair(COMPETITION_HEADER_NAME, competition),
                Pair(footballDataProperties.requestsTillResetHeader,
                        parseHeaderValue(requestsTillReset, footballDataProperties.requestsTillResetHeader)),
                Pair(footballDataProperties.secondsTillResetHeader,
                        parseHeaderValue(secondsTillReset, footballDataProperties.secondsTillResetHeader))
        ))
    }

    private fun parseHeaderValue(headerValue: List<String>?, headerNameForLogging: String): Int? {
        var resultingRequestsTillReset: Int? = null

        if (headerValue == null || headerValue.isEmpty()) {
            LOGGER.warning("$headerNameForLogging is not set: $headerValue")
        } else if (headerValue.size > 1) {
            LOGGER.warning("$headerNameForLogging is ambiguous: $headerValue")
        } else {
            resultingRequestsTillReset = headerValue[0].toInt()
        }

        return resultingRequestsTillReset
    }
}
