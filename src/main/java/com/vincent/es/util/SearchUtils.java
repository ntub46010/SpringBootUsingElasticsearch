package com.vincent.es.util;

import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchUtils {
    private SearchUtils() {}

    /**
     * <pre>
     *     {
     *         "term": {
     *             "{field}": {value}
     *         }
     *     }
     * </pre>
     */
    public static Query createTermQuery(String field, Object value) {
        var builder = new TermQuery.Builder();
        if (value instanceof Integer) {
            builder
                    .field(field)
                    .value((int) value); // 此方法接受 long 型態
        } else if (value instanceof String){
            builder
                    .field(field + ".keyword")
                    .value((String) value);
        } else {
            throw new UnsupportedOperationException("Please implement for other type additionally.");
        }

        return builder.build()._toQuery();
    }

    /**
     * <pre>
     *     {
     *         "terms": {
     *             "{field}": [{values}[0], {values}[1], ...]
     *         }
     *     }
     * </pre>
     */
    public static Query createTermsQuery(String field, Collection<?> values) {
        var elem = values.stream().findAny().orElseThrow();

        Stream<FieldValue> fieldValueStream;
        if (elem instanceof Integer) {
            fieldValueStream = values.stream()
                    .map(value -> FieldValue.of(b -> b.longValue((int) value)));
        } else if (elem instanceof String) {
            field = field + ".keyword";
            fieldValueStream = values.stream()
                    .map(value -> FieldValue.of(b -> b.stringValue((String) value)));
        } else {
            throw new UnsupportedOperationException("Please implement for other type additionally.");
        }

        var fieldValues = fieldValueStream.collect(Collectors.toList());
        var termsQueryField = TermsQueryField.of(b -> b.value(fieldValues));

        return new TermsQuery.Builder()
                .field(field)
                .terms(termsQueryField)
                .build()
                ._toQuery();
    }

    /**
     * <pre>
     *     {
     *         "range": {
     *             "{field}": {
     *                 "gte": "{gte}",
     *                 "lte": "{lte}"
     *             }
     *         }
     *     }
     * </pre>
     */
    public static Query createRangeQuery(String field, Number gte, Number lte) {
        var builder = new RangeQuery.Builder().field(field);

        if (gte != null) {
            builder.gte(JsonData.of(gte));
        }

        if (lte != null) {
            builder.lte(JsonData.of(lte));
        }

        return builder.build()._toQuery();
    }

    public static Query createRangeQuery(String field, Date gte, Date lte) {
        var builder = new RangeQuery.Builder().field(field);

        if (gte != null) {
            builder.gte(JsonData.of(gte));
        }

        if (lte != null) {
            builder.lte(JsonData.of(lte));
        }

        return builder.build()._toQuery();
    }

    /**
     * <pre>
     *     {
     *         "bool": {
     *             "should": [
     *                 {
     *                     "match": { "{fields}[0]": "{searchText}" }
     *                 },
     *                 {
     *                     "match": { "{fields}[1]": "{searchText}" }
     *                 }
     *             ]
     *         }
     *     }
     * </pre>
     */
    public static Query createMatchQuery(Set<String> fields, String searchText) {
        var bool = new BoolQuery.Builder();
        fields.stream()
                .map(field -> {
                    var matchQuery = new MatchQuery.Builder()
                            .field(field)
                            .query(searchText)
                            .build();
                    return matchQuery._toQuery();
                })
                .forEach(bool::should);

        return bool.build()._toQuery();
    }

    public static SortOptions createSortOption(String field, SortOrder order, SortMode mode) {
        var fieldSort = new FieldSort.Builder()
                .field(field)
                .order(order)
                .mode(mode)
                .build();
        return SortOptions.of(b -> b.field(fieldSort));
    }

    public static SortOptions createSortOption(String field, SortOrder order) {
        return createSortOption(field, order, null);
    }
}