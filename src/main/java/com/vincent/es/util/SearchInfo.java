package com.vincent.es.util;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.List;

public class SearchInfo {
    private BoolQuery boolQuery;
    private List<SortOptions> sortOptions;
    private Integer from;
    private Integer size;

    public static SearchInfo of(BoolQuery bool) {
        var info = new SearchInfo();
        info.boolQuery = bool;

        return info;
    }

    public static SearchInfo of(Query query) {
        var bool = BoolQuery.of(b -> b.filter(query));
        return of(bool);
    }

    public BoolQuery getBoolQuery() {
        return boolQuery;
    }

    public void setBoolQuery(BoolQuery boolQuery) {
        this.boolQuery = boolQuery;
    }

    public List<SortOptions> getSortOptions() {
        return sortOptions;
    }

    public void setSortOptions(List<SortOptions> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
