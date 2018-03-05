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
import org.springframework.context.annotation.Configuration
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.endpoint.PollingConsumer
import org.springframework.integration.util.DynamicPeriodicTrigger
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import java.util.logging.Logger

// TODO Use Spring Integration's Java DSL for better readability of the Integration flow.
// TODO Find a way to test and introduce sophisticated logging.

@Configuration
@EnableIntegration
open class FootballDataIntegrationConfig {

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
        private val LOGGER = Logger.getLogger(FootballDataIntegrationConfig::class.java.name) // TODO Get to know Spring Boot logging best practices.
    }

    @Bean
    @InboundChannelAdapter(channel = "footballDataChannel", poller = [(Poller("footballDataPoller"))])
    open fun footballDataAdapter(): Message<FootballDataFixtureWrappedListDto> {
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
    open fun footballDataPoller(): PollingConsumer = with(PollingConsumer(footballDataChannel(), MessageHandler { it.payload })) {
        setTrigger(footballDataTrigger())
        this
    }

    @Bean
    open fun footballDataTrigger(): DynamicPeriodicTrigger = DynamicPeriodicTrigger(0)

    @Bean
    open fun footballDataChannel(): QueueChannel = QueueChannel()

    @Bean
    @ServiceActivator(inputChannel = "footballDataChannel")
    open fun footballDataServiceActivator(message: Message<FootballDataFixtureWrappedListDto>) {
        val messageHeaderAccessor = IntegrationMessageHeaderAccessor(message)
        val competition = messageHeaderAccessor.getHeader(COMPETITION_HEADER_NAME, Competition::class.java)

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
