package com.vincent.es.util;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SearchInfo {
    private BoolQuery boolQuery;                            // 查詢條件
    private List<FunctionScore> functionScores = List.of(); // 計分函數
    private List<SortOptions> sortOptions;                  // 排序方式
    private Integer from;                                   // 資料的跳過數量
    private Integer size;                                   // 資料的擷取數量

    public SearchInfo() {
        var matchAll = MatchAllQuery.of(b -> b)._toQuery();
        this.boolQuery = BoolQuery.of(b -> b.filter(matchAll));
    }

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

    public List<FunctionScore> getFunctionScores() {
        return functionScores;
    }

    public void setFunctionScores(List<FunctionScore> functionScores) {
        this.functionScores = functionScores;
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

    // library 使用 Query 類別當作條件的傳遞介面
    public Query toQuery() {
        if (CollectionUtils.isEmpty(functionScores)) {
            return boolQuery._toQuery();
        }

        return new FunctionScoreQuery.Builder()
                .query(boolQuery._toQuery())
                .functions(functionScores)
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Replace)
                .maxBoost(30.0)
                .build()
                ._toQuery();
    }
}
