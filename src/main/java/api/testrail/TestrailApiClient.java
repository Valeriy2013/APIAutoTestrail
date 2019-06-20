/**
 * TestRail API binding for Java (API v2, available since TestRail 3.0)
 * Updated for TestRail 5.7
 * <p>
 * Learn more:
 * <p>
 * http://docs.gurock.com/testrail-api2/start
 * http://docs.gurock.com/testrail-api2/accessing
 * <p>
 * Copyright Gurock Software GmbH. See license.md for details.
 */

package api.testrail;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import api.Constants;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestrailApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TestrailApiClient.class);

    private static TestrailApiClient instance;
    private String m_user;
    private String m_password;
    private String m_url;

    public static TestrailApiClient testrailApiClient() {
        if (instance == null)
            instance = new TestrailApiClient(Constants.TESTRAIL_URL, Constants.TESTRAIL_USER, Constants.TESTRAIL_PASSWORD);
        return instance;
    }

    private TestrailApiClient(String base_url, String user, String pass) {
        if (!base_url.endsWith("/")) {
            base_url += "/";
        }
        m_url = base_url + "index.php?/api/v2/";
        m_user = user;
        m_password = pass;
    }

    /**
     * Get/Set User
     * <p>
     * Returns/sets the user used for authenticating the API requests.
     */
    public String getUser() {
        return m_user;
    }

    public void setUser(String user) {
        m_user = user;
    }

    /**
     * Get/Set Password
     * <p>
     * Returns/sets the password used for authenticating the API requests.
     */
    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * Send Get
     * <p>
     * Issues a GET request (read) against the API and returns the result
     * (as Object, see below).
     * <p>
     * Arguments:
     * <p>
     * uri                  The API method to call including parameters
     * (e.g. get_case/1)
     * <p>
     * Returns the parsed JSON response as standard object which can
     * either be an instance of JSONObject or JSONArray (depending on the
     * API method). In most cases, this returns a JSONObject instance which
     * is basically the same as java.util.Map.
     * <p>
     * If 'get_attachment/:attachment_id', returns a String
     */
    public Object sendGet(String uri, String data)
            throws IOException, APIException {
        return this.sendRequest("GET", uri, data);
    }

    public Object sendGet(String uri)
            throws IOException, APIException {
        return this.sendRequest("GET", uri, null);
    }

    /**
     * Send POST
     * <p>
     * Issues a POST request (write) against the API and returns the result
     * (as Object, see below).
     * <p>
     * Arguments:
     * <p>
     * uri                  The API method to call including parameters
     * (e.g. add_case/1)
     * data                 The data to submit as part of the request (e.g.,
     * a map)
     * If adding an attachment, must be the path
     * to the file
     * <p>
     * Returns the parsed JSON response as standard object which can
     * either be an instance of JSONObject or JSONArray (depending on the
     * API method). In most cases, this returns a JSONObject instance which
     * is basically the same as java.util.Map.
     */
    public Object sendPost(String uri, Object data)
            throws IOException, APIException {
        return this.sendRequest("POST", uri, data);
    }

    private Object sendRequest(String method, String uri, Object data)
            throws IOException, APIException {
        URL url = new URL(m_url + uri);
        // Create the connection object and set the required HTTP method
        // (GET/POST) and headers (content type and basic auth).
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String auth = getAuthorization(m_user, m_password);
        conn.addRequestProperty("Authorization", "Basic " + auth);

        if (method.equals("POST")) {
            conn.setRequestMethod("POST");
            // Add the POST arguments, if any. We just serialize the passed
            // data object (i.e. a dictionary) and then add it to the
            // request body.
            if (data != null) {
                if (uri.startsWith("add_attachment"))   // add_attachment API requests
                {
                    String boundary = "TestRailAPIAttachmentBoundary"; //Can be any random string
                    File uploadFile = new File((String) data);

                    conn.setDoOutput(true);
                    conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream ostreamBody = conn.getOutputStream();
                    BufferedWriter bodyWriter = new BufferedWriter(new OutputStreamWriter(ostreamBody));

                    bodyWriter.write("\n\n--" + boundary + "\r\n");
                    bodyWriter.write("Content-Disposition: form-data; name=\"attachment\"; filename=\""
                            + uploadFile.getName() + "\"");
                    bodyWriter.write("\r\n\r\n");
                    bodyWriter.flush();

                    //Read file into request
                    InputStream istreamFile = new FileInputStream(uploadFile);
                    int bytesRead;
                    byte[] dataBuffer = new byte[1024];
                    while ((bytesRead = istreamFile.read(dataBuffer)) != -1) {
                        ostreamBody.write(dataBuffer, 0, bytesRead);
                    }

                    ostreamBody.flush();

                    //end of attachment, add boundary
                    bodyWriter.write("\r\n--" + boundary + "--\r\n");
                    bodyWriter.flush();

                    //Close streams
                    istreamFile.close();
                    ostreamBody.close();
                    bodyWriter.close();
                } else    // Not an attachment
                {
                    conn.addRequestProperty("Content-Type", "application/json");
                    byte[] block = JSONValue.toJSONString(data).
                            getBytes(StandardCharsets.UTF_8);

                    conn.setDoOutput(true);
                    OutputStream ostream = conn.getOutputStream();
                    ostream.write(block);
                    ostream.close();
                }
            }
        } else    // GET request
        {
            conn.addRequestProperty("Content-Type", "application/json");
        }

        // Execute the actual web request (if it wasn't already initiated
        // by getOutputStream above) and record any occurred errors (we use
        // the error stream in this case).
        int status = conn.getResponseCode();

        InputStream istream;
        if (status != 200) {
            istream = conn.getErrorStream();
            if (istream == null) {
                throw new APIException(
                        "TestRail API return HTTP " + status +
                                " (No additional error message received)"
                );
            }
        } else {
            istream = conn.getInputStream();
        }

        // If 'get_attachment/' returned valid status code, save the file
        if ((istream != null) && (uri.startsWith("get_attachment/"))) {
            FileOutputStream outputStream = new FileOutputStream((String) data);

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = istream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            istream.close();
            return data;
        }

        // Not an attachment received
        // Read the response body, if any, and deserialize it from JSON.
        String text = "";
        if (istream != null) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            istream,
                            StandardCharsets.UTF_8
                    )
            );

            String line;
            while ((line = reader.readLine()) != null) {
                text += line;
                text += System.getProperty("line.separator");
            }

            reader.close();
        }

        Object result;
        if (!text.equals("")) {
            result = JSONValue.parse(text);
        } else {
            result = new JSONObject();
        }

        // Check for any occurred errors and add additional details to
        // the exception message, if any (e.g. the error message returned
        // by TestRail).
        if (status != 200) {
            String error = "No additional error message received";
            if (result != null && result instanceof JSONObject) {
                JSONObject obj = (JSONObject) result;
                if (obj.containsKey("error")) {
                    error = '"' + (String) obj.get("error") + '"';
                }
            }

            throw new APIException(
                    "TestRail API returned HTTP " + status +
                            "(" + error + ")"
            );
        }

        return result;
    }

    private static String getAuthorization(String user, String password) {
        try {
            return new String(Base64.getEncoder().encode((user + ":" + password).getBytes()));
        } catch (IllegalArgumentException e) {
            // Not thrown
        }

        return "";
    }

    private static String getBase64(byte[] buffer) {
        final char[] map = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '+', '/'
        };

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buffer.length; i++) {
            byte b0 = buffer[i++], b1 = 0, b2 = 0;

            int bytes = 3;
            if (i < buffer.length) {
                b1 = buffer[i++];
                if (i < buffer.length) {
                    b2 = buffer[i];
                } else {
                    bytes = 2;
                }
            } else {
                bytes = 1;
            }

            int total = (b0 << 16) | (b1 << 8) | b2;

            switch (bytes) {
                case 3:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append(map[(total >> 6) & 0x3f]);
                    sb.append(map[total & 0x3f]);
                    break;

                case 2:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append(map[(total >> 6) & 0x3f]);
                    sb.append('=');
                    break;

                case 1:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append('=');
                    sb.append('=');
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Add testcase results to existing test run
     * @param client
     * @param testCaseId
     * @param testRunId
     * @param testStatus
     * @param error
     * @throws IOException
     * @throws APIException
     */
    public void addResultForTestCase(TestrailApiClient client, int testCaseId, int testRunId, int testStatus, String error) throws IOException, APIException {
        logger.info("Adding run results for test case");
        Map<String, Serializable> data = new HashMap<>();
        data.put("status_id", testStatus);
        data.put("comment", "Test Executed - Status updated automatically from test automation.");
        client.sendPost("add_result_for_case/" + testRunId + "/" + testCaseId + "", data);
    }

    /**
     * Create test run in TestRail
     * @param client
     * @param testProjectId
     * @return
     * @throws IOException
     * @throws APIException
     */
    public int addRun(TestrailApiClient client, int testProjectId) throws IOException, APIException {
        logger.info("Creating test run");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        Map<String, Serializable> data = new HashMap<>();
        data.put("name", "Test run " + formatter.format(date));
        JSONObject obj = (JSONObject) client.sendPost("add_run/" + testProjectId, data);
        return Integer.parseInt(obj.get("id").toString());
    }

    /**
     * Return testcase title by testcase ID
     * @param client
     * @param testCaseId
     * @throws IOException
     * @throws APIException
     */
    public String getCaseName(TestrailApiClient client, int testCaseId) throws IOException, APIException {
        logger.info("Getting test case name");
        JSONObject obj = (JSONObject) client.sendGet("get_case/" + testCaseId);
        return obj.get("title").toString();
    }
}
