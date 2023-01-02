package com.vincent.es;

import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
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

    @Test
    public void testFunctionScore_FieldValueFactor() {
        var fieldValueFactorScore = SearchUtils
                .createFieldValueFactor("grade", 0.5, FieldValueFactorModifier.Square, 0.0);

        var searchInfo = new SearchInfo();
        searchInfo.setFunctionScores(List.of(fieldValueFactorScore));

        var students = repository.find(searchInfo);

        // Dora (4.0) -> Mario (2.25) -> Vincent (1.0) -> Winnie (0.25)
        assertDocumentIds(students, "101", "102", "103", "104");
    }

    @Test
    public void testFunctionScore_ConditionalWeight() {
        var departmentQuery = SearchUtils
                .createTermQuery("departments.keyword", "財務金融");
        var departmentScore = SearchUtils
                .createConditionalWeightFunctionScore(departmentQuery, 3.0);

        var courseQuery = SearchUtils
                .createTermQuery("courses.name.keyword", "程式設計");
        var courseScore = SearchUtils
                .createConditionalWeightFunctionScore(courseQuery, 1.5);

        var fieldValueFactorScore = SearchUtils
                .createFieldValueFactor("grade", 1.0, FieldValueFactorModifier.None, 0.0);
        var gradeScore = SearchUtils
                .createWeightedFieldValueFactor(fieldValueFactorScore.fieldValueFactor(), 0.5);

        var searchInfo = new SearchInfo();
        searchInfo.setFunctionScores(List.of(
                departmentScore,
                courseScore,
                gradeScore
        ));

        var students = repository.find(searchInfo);

        // Vincent (5.5) -> Dora (5.0) -> Mario (1.5) -> Winnie (0.5)
        assertDocumentIds(students, "103", "101", "102", "104");
    }

    @Test
    public void testFunctionScore_DecayFunction_Number() {
        var placement = SearchUtils
                .createDecayPlacement(100, 15, 10, 0.5);
        var decayFunctionScore = SearchUtils
                .createGaussFunction("conductScore", placement);

        var searchInfo = new SearchInfo();
        searchInfo.setFunctionScores(List.of(decayFunctionScore));

        var students = repository.find(searchInfo);

        // Vincent (1.0) -> Mario (0.9726) -> Dora (0.4322) -> Winnie (0.2570)
        assertDocumentIds(students, "103", "102", "101", "104");
    }

    @Test
    public void testFunctionScore_DecayFunction_Date() {
        var placement = SearchUtils
                .createDecayPlacement("now", "90d", "270d", 0.5);
        var decayFunctionScore = SearchUtils
                .createGaussFunction("englishIssuedDate", placement);

        var searchInfo = new SearchInfo();
        searchInfo.setFunctionScores(List.of(decayFunctionScore));

        var students = repository.find(searchInfo);

        // Mario -> Winnie -> Dora -> Vincent
        assertDocumentIds(students, "102", "104", "101", "103");
    }

    private void assertDocumentIds(boolean ignoreOrder, List<Student> actualDocs, String... expectedIdArray) {
        if (!ignoreOrder) {
            assertDocumentIds(actualDocs, expectedIdArray);
            return;
        }

        var expectedIds = List.of(expectedIdArray);
        var actualIds = actualDocs.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        assertTrue(expectedIds.containsAll(actualIds));
        assertTrue(actualIds.containsAll(expectedIds));
    }

    private void assertDocumentIds(List<Student> actualDocs, String... expectedIdArray) {
        var expectedIds = List.of(expectedIdArray);
        var actualIds = actualDocs.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        assertEquals(expectedIds, actualIds);
    }

}
