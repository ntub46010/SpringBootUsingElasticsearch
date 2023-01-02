package com.vincent.es;

import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import com.vincent.es.entity.Student;
import com.vincent.es.repository.StudentEsRepository;
import com.vincent.es.util.SampleData;
import com.vincent.es.util.SearchInfo;
import com.vincent.es.util.SearchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTests {

    @Autowired
    private StudentEsRepository repository;

    @SuppressWarnings({"squid:S2925"})
    @Before
    public void setup() throws IOException, InterruptedException {
        repository.init();

        var documents = SampleData.get();
        repository.insert(documents);
        Thread.sleep(2000);
    }

    @Test
    public void testTerm_NumberField() {
        var query = SearchUtils.createTermQuery("grade", 3);
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Mario
        assertDocumentIds(true, students, "102");
    }

    @Test
    public void testTerms_TextField() {
        var values = List.of("資訊管理", "企業管理");
        var query = SearchUtils.createTermsQuery("departments.keyword", values);
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Vincent, Winnie
        assertDocumentIds(true, students, "103", "104");
    }

    @Test
    public void testNumberRange() {
        var query = SearchUtils.createRangeQuery("grade", 2, 4);
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Dora, Mario, Vincent
        assertDocumentIds(true, students, "101", "102", "103");
    }

    @Test
    public void testDateRange() throws ParseException {
        var sdf = new SimpleDateFormat("yyyy-MM-dd");
        var fromDate = sdf.parse("2021-07-01");
        var toDate = sdf.parse("2022-06-30");

        var query = SearchUtils.createRangeQuery("englishIssuedDate", fromDate, toDate);
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Winnie, Mario
        assertDocumentIds(true, students, "102", "104");
    }

    @Test
    public void testFullTextSearch() {
        var fields = Set.of("name", "introduction");
        var searchText = "vincent career";

        var query = SearchUtils.createMatchQuery(fields, searchText);
        var bool = BoolQuery.of(b -> b.must(query));
        var searchInfo = SearchInfo.of(bool);

        var students = repository.find(searchInfo);

        // Winnie, Vincent
        assertDocumentIds(true, students, "103", "104");
    }

    @Test
    public void testTextFieldExists() {
        var query = SearchUtils.createFieldExistsQuery("bloodType");
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Dora, Mario, Vincent
        assertDocumentIds(true, students, "101", "102", "103");
    }

    @Test
    public void testTextArrayFieldExists() {
        var query = SearchUtils.createFieldExistsQuery("phoneNumbers");
        var searchInfo = SearchInfo.of(query);

        var students = repository.find(searchInfo);

        // Dora, Vincent
        assertDocumentIds(true, students, "101", "103");
    }

    @Test
    public void testSortByMultipleFields() {
        var coursePointSort = SearchUtils.createSortOption("courses.point", SortOrder.Desc, SortMode.Max);
        var nameSort = SearchUtils.createSortOption("name.keyword", SortOrder.Asc);

        var query = MatchAllQuery.of(b -> b)._toQuery();

        var searchInfo = SearchInfo.of(query);
        searchInfo.setSortOptions(List.of(coursePointSort, nameSort));

        var students = repository.find(searchInfo);

        // Mario -> Vincent -> Dora -> Winnie
        assertDocumentIds(false, students, "102", "103", "101", "104");
    }

    @Test
    public void testPaging() {
        var gradeSort = SearchUtils.createSortOption("grade", SortOrder.Desc);

        var query = MatchAllQuery.of(b -> b)._toQuery();

        var searchInfo = SearchInfo.of(query);
        searchInfo.setSortOptions(List.of(gradeSort));
        searchInfo.setFrom(0);
        searchInfo.setSize(2);

        var students = repository.find(searchInfo);

        // Dora -> Mario
        assertDocumentIds(false, students, "101", "102");
    }

    private void assertDocumentIds(boolean ignoreOrder, List<Student> actualDocs, String... expectedIdArray) {
        var expectedIds = List.of(expectedIdArray);
        var actualIds = actualDocs.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        if (ignoreOrder) {
            assertTrue(expectedIds.containsAll(actualIds));
            assertTrue(actualIds.containsAll(expectedIds));
        } else {
            assertEquals(expectedIds, actualIds);
        }
    }
}
