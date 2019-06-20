import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


class OMDBTests extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(OMDBTests.class);

    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that exist at least 30 items ")
    void searchByTextAndCheckThatExistMoreThan30Items() {
        testCaseId = 4;

        logger.info("Getting total items count from 'totalResults' field");
        final String foundResults = request
                .given()
                .pathParam("search_criteria", "stem")

                .when()
                .get("/?s={search_criteria}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("totalResults");

        assertThat(Integer.parseInt(foundResults), greaterThan(30));
    }

    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that the result contains items titled \"The STEM Journals\" and \"Activision: STEM - in the Videogame Industry\"  ")
    void searchByTextAndCheckThatResultContainsItemsWithTitles() {
        testCaseId = 5;

        logger.info("Getting total items count from 'totalResults' field");
        final String foundResults = request
                .given()
                .pathParam("search_criteria", "stem")

                .when()
                .get("/?s={search_criteria}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("totalResults");


        logger.info("Since response contains only 10 items per page, getting all the results based on 'totalResults' field");
        ArrayList<HashMap<String, ?>> allResults = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(foundResults) / 10 + 1; i++) {
            initRequest();
            ArrayList<HashMap<String, ?>> searchResults = request
                    .given()
                    .pathParam("search_criteria", "stem")
                    .pathParam("page", String.valueOf(i))

                    .when()
                    .get("/?s={search_criteria}&page={page}")

                    .then()
                    .statusCode(200)

                    .extract()
                    .jsonPath()
                    .get("Search.findAll { item -> item.Title == 'The STEM Journals' || item.Title == 'Activision: STEM - in the Videogame Industry'}");
            allResults.addAll(searchResults);
        }
        assertThat(allResults.size(), equalTo(2));
    }

    @Test
    @DisplayName("Get movie details by ID and check that the movie was released on 23 Nov 2010")
    void searchByIdAndCheckReleaseDate() {
        testCaseId = 6;
        String movieId = "tt1810525";
        logger.info("Getting movie by ID");
        final String released = request
                .given()
                .pathParam("id", movieId)

                .when()
                .get("/?i={id}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("Released");

        assertThat(released, equalTo("23 Nov 2010"));
    }

    @Test
    @DisplayName("Get movie details by ID and check that the movie was directed by Mike Feurstein")
    void searchByIdAndDirectedBy() {
        testCaseId = 9;
        String movieId = "tt1810525";
        logger.info("Getting movie by ID");
        final String released = request
                .given()
                .pathParam("id", movieId)

                .when()
                .get("/?i={id}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("Director");

        assertThat(released, equalTo("Mike Feurstein"));
    }

    @Test
    @DisplayName("Get item by title \"The STEM Journals\" and check that the plot contains the string \"Science, Technology, Engineering and Math\" ")
    void searchByTitleAndCheckPlot() {
        testCaseId = 7;
        String movieTitle = "The STEM Journals";
        logger.info("Getting movie by Title");
        final String plot = request
                .given()
                .pathParam("title", movieTitle)

                .when()
                .get("/?t={title}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("Plot");
        assertThat(plot, endsWith("Science, Technology, Engineering and Math."));
    }

    @Test
    @DisplayName("Get item by title \"The STEM Journals\" and check that the item has a runtime of 22 minutes")
    void searchByTitleAndCheckRuntime() {
        testCaseId = 8;
        String movieTitle = "The STEM Journals";
        logger.info("Getting movie by Title");
        final String runtime = request
                .given()
                .pathParam("title", movieTitle)

                .when()
                .get("/?t={title}")

                .then()
                .statusCode(200)

                .extract()
                .jsonPath()
                .get("Runtime");
        assertThat(runtime, equalTo("22 min"));
    }

}
