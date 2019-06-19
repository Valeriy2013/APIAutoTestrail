import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class OMDBTests extends BaseTest {

    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that at least 30 items are shown  ")
    void searchByTextAndCheckThatMoreThan30ItemsAreShown() throws MalformedURLException {
        testCaseId = 4;
        final HashMap items = new JsonPath(new URL("http://www.omdbapi.com/?s=stem&apikey=d60ddbe8")).get();
        assertThat(Integer.parseInt(items.get("totalResults").toString()), greaterThan(30));
    }

    @Test
    @DisplayName("Search for all items that match the search string \"stem\" and check that the result contains items titled \"The STEM Journals\" and \"Activision: STEM - in the Videogame Industry\"  ")
    void searchByTextAndCheckThatResultContainsItems() throws MalformedURLException {
        testCaseId = 5;
        final List<Map<String, ?>> items = new JsonPath(new URL("http://www.omdbapi.com/?s=stem&apikey=d60ddbe8&page=100")).get("search.findAll { item -> item.title = 'The STEM Journals' }");
        assertThat(items.size(), equalTo(2));
    }
}
