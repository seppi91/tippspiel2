package com.github.zemke.tippspiel2.core.config

import com.github.zemke.tippspiel2.core.profile.Dev
import com.github.zemke.tippspiel2.core.properties.EmbeddedDataSourceProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import java.net.URI
import java.nio.file.Paths
import javax.sql.DataSource

@Configuration
open class DataSourceConfig {

    @Autowired
    private lateinit var embeddedDataSourceProperties: EmbeddedDataSourceProperties

    @Bean
    @ConfigurationProperties("spring.datasource")
    @Primary
    @Dev
    open fun embeddedPostgresDatabaseDataSource(): DataSource {
        startEmbeddedPostgresDatabase()
        return DataSourceBuilder
                .create()
                .build();
    }

    private fun startEmbeddedPostgresDatabase() {
        val uri = URI.create(embeddedDataSourceProperties.url.substring(5))
        EmbeddedPostgres { "9.1.0-1" }
                .start(EmbeddedPostgres.cachedRuntimeConfig(
                        Paths.get(embeddedDataSourceProperties.embeddedDirectory)),
                        uri.host, uri.port, uri.path.substring(1),
                        embeddedDataSourceProperties.username, embeddedDataSourceProperties.password, emptyList())
    }
}