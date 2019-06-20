package api.omdb;

import api.Constants;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Map;


public class OMDbApiClient {

    public JsonPath getRequest(Map<String,String> params){
        RestAssured.baseURI = Constants.OMDB_URL;

        final JsonPath obj = RestAssured
                .given()
                .contentType("application/json")
                .queryParam("apikey", Constants.OMDB_API_KEY)
                .queryParams(params)

                .when()
                .get()

                .then()
                .statusCode(200)

                .extract()
                .jsonPath();
        return obj;
    }
}
