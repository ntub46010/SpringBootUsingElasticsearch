package com.vincent.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.vincent.es.repository.StudentEsRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        var httpHost = new HttpHost("localhost", 9200);
        var restClient = RestClient.builder(httpHost).build();
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    @Bean
    public StudentEsRepository studentEsRepository(ElasticsearchClient client) {
        var repo = new StudentEsRepository(client, "student");
        repo.init();
        return repo;
    }
}