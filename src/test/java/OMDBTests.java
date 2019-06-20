import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


class OMDBTests extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(OMDBTests.class);


    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that exist at least 30 items ")
    void searchByTextAndCheckThatExistMoreThan30Items() {
        testCaseId = 4;

        logger.info("Getting total items count from 'totalResults' field");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put("s", "stem");
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        assertThat(Integer.parseInt(response.get("totalResults")), greaterThan(30));
    }

    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that the result contains items titled \"The STEM Journals\" and \"Activision: STEM - in the Videogame Industry\"  ")
    void searchByTextAndCheckThatResultContainsItemsWithTitles() {
        testCaseId = 5;

        logger.info("Getting total items count from 'totalResults' field");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put("s", "stem");
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        int foundResults = Integer.parseInt(response.get("totalResults"));
        int pagesCount = foundResults / 10 + 1;

        logger.info("Since response contains only 10 items per page, getting all the results based on 'totalResults' field");
        ArrayList<HashMap<String, ?>> allResults = new ArrayList<>();

        for (int i = 1; i <= pagesCount; i++) {
            searchParams.clear();
            searchParams.put("s", "stem");
            searchParams.put("page", String.valueOf(i));
            JsonPath results = omDbApiClient.getRequest(searchParams);
            ArrayList<HashMap<String, ?>> searchResults = results.get("Search.findAll { item -> item.Title == 'The STEM Journals' || item.Title == 'Activision: STEM - in the Videogame Industry'}");
            allResults.addAll(searchResults);
        }
        assertThat(allResults.size(), equalTo(2));
    }

    @Test
    @DisplayName("Get movie details by ID and check that the movie was released on 23 Nov 2010")
    void searchByIdAndCheckReleaseDate() {
        testCaseId = 6;
        logger.info("Getting movie by ID");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put("i", "tt1810525");
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        assertThat(response.get("Released"), equalTo("23 Nov 2010"));
    }

    private static Stream<Arguments> Movies() {
        return Stream.of(
                Arguments.arguments("i", "tt1810525", "Director", "Mike Feurstein"),
                Arguments.arguments("i", "tt1846527", "Director", "Ph Carli"));
    }

    @ParameterizedTest
    @MethodSource("Movies")
    @DisplayName("Get movie details by ID and check that the movie was directed by Mike Feurstein")
    void searchByIdAndDirectedBy(String endpoint, String id, String field, String expectedValue) {
        testCaseId = 9;

        logger.info("Getting movie by ID");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put(endpoint, id);
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        assertThat(response.get(field), equalTo(expectedValue));
    }

    @Test
    @DisplayName("Get item by title \"The STEM Journals\" and check that the plot contains the string \"Science, Technology, Engineering and Math\" ")
    void searchByTitleAndCheckPlot() {
        testCaseId = 7;

        logger.info("Getting movie by Title");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put("t", "The STEM Journals");
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        assertThat(response.get("Plot"), endsWith("Science, Technology, Engineering and Math."));
    }

    @Test
    @DisplayName("Get item by title \"The STEM Journals\" and check that the item has a runtime of 22 minutes")
    void searchByTitleAndCheckRuntime() {
        testCaseId = 8;

        logger.info("Getting movie by Title");
        Map<String, String> searchParams = new HashMap<String, String>() {
            {
                put("t", "The STEM Journals");
            }
        };
        JsonPath response = omDbApiClient.getRequest(searchParams);
        assertThat(response.get("Runtime"), equalTo("22 min"));
    }
}
