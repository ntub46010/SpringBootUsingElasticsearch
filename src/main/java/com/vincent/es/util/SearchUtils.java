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
     *             "{@param field}": {@param value}
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
                    .field(field)
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
     *             "{@param field}": [
     *                 {@param values[0]},
     *                 {@param values[1]}, ...
     *             ]
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
     *             "{@param field}": {
     *                 "gte": "{@param gte}",
     *                 "lte": "{@param lte}"
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
     *                     "match": {
     *                         "{@param fields[0]}": "{@param searchText}"
     *                     }
     *                 },
     *                 {
     *                     "match": {
     *                         "{@param fields[1]}": "{@param searchText}"
     *                     }
     *                 }, ...
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

    /**
     * <pre>
     *     {
     *         "exists": {
     *             "field": {@param field}
     *         }
     *     }
     * </pre>
     */
    public static Query createFieldExistsQuery(String field) {
        return new ExistsQuery.Builder()
                .field(field)
                .build()
                ._toQuery();
    }

    /**
     * <pre>
     *     {
     *         "{@param field}": {
     *             "order": {@param order},
     *             "mode": {@param mode}
     *         }
     *     }
     * </pre>
     */
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

    /**
     * <pre>
     *     {
     *         "field_value_factor": {
     *             "field": {@param field},
     *             "factor": {@param factor},
     *             "modifier": {@param modifier},
     *             "missing": {@param missing}
     *         }
     *     }
     * </pre>
     */
    public static FunctionScore createFieldValueFactor(
            String field, Double factor, FieldValueFactorModifier modifier, Double missing) {

        return new FieldValueFactorScoreFunction.Builder()
                .field(field)
                .factor(factor)
                .modifier(modifier)
                .missing(missing)
                .build()
                ._toFunctionScore();
    }

    /**
     * <pre>
     *     {
     *         "filter": {@param query},
     *         "weight": {@param weight}
     *     }
     * </pre>
     */
    public static FunctionScore createConditionalWeightFunctionScore(Query query, Double weight) {
        return new FunctionScore.Builder()
                .filter(query)
                .weight(weight)
                .build();
    }

    /**
     * <pre>
     *     {
     *         "field_value_factor": {@param function},
     *         "weight": {@param weight}
     *     }
     * </pre>
     */
    public static FunctionScore createWeightedFieldValueFactor(
            FieldValueFactorScoreFunction function, Double weight) {

        return new FunctionScore.Builder()
                .fieldValueFactor(function)
                .weight(weight)
                .build();
    }

    /**
     * <pre>
     *     {
     *         "gauss": {@param placement}
     *     }
     * </pre>
     */
    public static FunctionScore createGaussFunction(String field, DecayPlacement placement) {
        var decayFunction = new DecayFunction.Builder()
                .field(field)
                .placement(placement)
                .build();
        return new FunctionScore.Builder()
                .gauss(decayFunction)
                .build();
    }

    /**
     * <pre>
     *     {
     *         "origin": {@param origin},
     *         "offset": {@param offset},
     *         "scale": {@param scale},
     *         "decay": {@param decay}
     *     }
     * </pre>
     */
    public static DecayPlacement createDecayPlacement(
            Number origin, Number offset, Number scale, Double decay) {

        return new DecayPlacement.Builder()
                .origin(JsonData.of(origin))
                .offset(JsonData.of(offset))
                .scale(JsonData.of(scale))
                .decay(decay)
                .build();
    }

    public static DecayPlacement createDecayPlacement(
            String originExp, String offsetExp, String scaleExp, Double decay) {

        return new DecayPlacement.Builder()
                .origin(JsonData.of(originExp))
                .offset(JsonData.of(offsetExp))
                .scale(JsonData.of(scaleExp))
                .decay(decay)
                .build();
    }
}