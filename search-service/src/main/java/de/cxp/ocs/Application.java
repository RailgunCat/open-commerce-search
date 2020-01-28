package de.cxp.ocs;

import java.util.Map.Entry;

import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import de.cxp.ocs.config.ApplicationProperties;
import de.cxp.ocs.config.Field;
import de.cxp.ocs.config.IndexConfiguration;
import de.cxp.ocs.elasticsearch.ElasticSearchBuilder;
import de.cxp.ocs.elasticsearch.RestClientBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.spring.autoconfigure.MeterRegistryCustomizer;

@SpringBootApplication
@RefreshScope
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ElasticSearchBuilder getESBuilder(ApplicationProperties properties, MeterRegistry registry) {
		fixFieldConfiguration(properties.getDefaultIndexConfig());
		for (IndexConfiguration indexConfig : properties.getIndexConfig().values()) {
			fixFieldConfiguration(indexConfig);
		}

		RestClientBuilder restClientBuilder = RestClientBuilderFactory.createRestClientBuilder(properties.getConnectionConfiguration());
		return new ElasticSearchBuilder(restClientBuilder);
	}

	private void fixFieldConfiguration(IndexConfiguration indexConfig) {
		for (Entry<String, Field> field : indexConfig.getFieldConfiguration().getFields().entrySet()) {
			if (field.getValue().getName() == null) {
				field.getValue().setName(field.getKey());
			}
		}
	}

	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
			@Value("${spring.application.name}") String applicationName) {
		return registry -> {
			registry.config().commonTags("application", applicationName);
		};
	}
}
