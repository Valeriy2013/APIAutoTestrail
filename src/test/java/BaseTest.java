import api.Constants;
import api.TestStatus;
import api.omdb.OMDbApiClient;
import api.testrail.APIException;
import api.testrail.TestrailApiClient;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    private static TestrailApiClient client;
    private static TestStatus testStatus;
    private static int testProjectId = Constants.PROJECT_ID;
    private static int testRunId;
    protected static int testCaseId;
    protected static RequestSpecification request;
    protected static OMDbApiClient omDbApiClient = new OMDbApiClient();


    @BeforeAll
    static void beforeAll() throws Exception {
        logger.info("Before all test methods");
        client = TestrailApiClient.testrailApiClient();
        testRunId = client.addRun(client, testProjectId);
    }

    @BeforeEach
    void beforeEach() {
        logger.info("Before each test method");
    }

    @AfterEach
    void afterEach() throws IOException, APIException {
        logger.info("After each test method");
        client.addResultForTestCase(client, testCaseId, testRunId, testStatus.getValue(), "");
    }

    @AfterAll
    static void afterAll() {
        logger.info("After all test methods");
    }

    public class StatusExtension implements AfterTestExecutionCallback {

        StatusExtension() {
        }

        @Override
        public void afterTestExecution(ExtensionContext context) {
            boolean isFailed = context.getExecutionException().isPresent();
            testStatus = isFailed ? TestStatus.FAILED : TestStatus.PASSED;
        }
    }

    @RegisterExtension
    BaseTest.StatusExtension statusExtension = new BaseTest.StatusExtension();
}