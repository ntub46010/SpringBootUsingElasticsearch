package com.vincent.es.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.CreateOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import com.vincent.es.entity.Student;
import com.vincent.es.util.IOSupplier;
import com.vincent.es.util.SearchInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentEsRepository {
    private final ElasticsearchClient client;
    private final String indexName;

    public StudentEsRepository(ElasticsearchClient client, String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    public void init() {
        deleteIndex();
        createIndex();
    }

    public void createIndex() {
        var propertyMap = getPropertyMappings();
        var request = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(TypeMapping.of(b -> b.properties(propertyMap)))
                .build();

        execute(() -> client.indices().create(request));
    }

    public void deleteIndex() {
        var request = DeleteIndexRequest.of(b -> b.index(indexName));
        execute(() -> client.indices().delete(request));
    }

    public Student insert(Student doc) {
        var request = new CreateRequest.Builder<Student>()
                .index(indexName)
                .id(doc.getId())
                .document(doc)
                .build();

        return execute(() -> {
            var createResponse = client.create(request);
            doc.setId(createResponse.id());
            return doc;
        });
    }

    public List<Student> insert(List<Student> docs) {
        var builder = new BulkRequest.Builder().index(indexName);

        docs.forEach(doc -> {
            var createOp = new CreateOperation.Builder<Student>()
                    .id(doc.getId())
                    .document(doc)
                    .build();
            var bulkOp = BulkOperation.of(b -> b.create(createOp));
            builder.operations(bulkOp);
        });

        var bulkRequest = builder.build();

        return execute(() -> {
            var bulkResponse = client.bulk(bulkRequest);
            List<BulkResponseItem> items = bulkResponse.items();
            for (var i = 0; i < items.size(); i++) {
                var id = items.get(i).id();
                docs.get(i).setId(id);
            }

            return docs;
        });
    }

    public Optional<Student> findById(String id) {
        var request = new GetRequest.Builder()
                .index(indexName)
                .id(id)
                .build();

        var getResponse = execute(() -> client.get(request, Student.class));
        return Optional.ofNullable(getResponse.source());
    }

    public Student save(Student doc) {
        var request = new IndexRequest.Builder<Student>()
                .index(indexName)
                .id(doc.getId())
                .document(doc)
                .build();

        return execute(() -> {
            var indexResponse = client.index(request);
            doc.setId(indexResponse.id());
            return doc;
        });
    }

    public void deleteById(String id) {
        var request = new DeleteRequest.Builder()
                .index(indexName)
                .id(id)
                .build();

        execute(() -> client.delete(request));
    }

    @SuppressWarnings({"squid:S112"})
    public List<Student> find(SearchInfo info) {
        var request = new SearchRequest.Builder()
                .index(indexName)
                .query(info.getBoolQuery()._toQuery())
                .sort(info.getSortOptions())
                .from(info.getFrom())
                .size(info.getSize())
                .build();

        try {
            var searchResponse = client.search(request, Student.class);
            return searchResponse
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // 範例為求方便，只簡單做例外處理
            throw new RuntimeException(e);
        }
    }

    private Map<String, Property> getPropertyMappings() {
        var englishIssuedDateProperty = DateProperty.of(b -> b)._toProperty();
        return Map.of("englishIssuedDate", englishIssuedDateProperty);
    }

    @SuppressWarnings({"squid:S112"})
    private <V> V execute(IOSupplier<V> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            // 範例為求方便，只簡單做例外處理
            throw new RuntimeException(e);
        }
    }

}
