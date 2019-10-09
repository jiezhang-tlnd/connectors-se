/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.rest.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.talend.components.rest.configuration.HttpMethod;
import org.talend.components.rest.configuration.Param;
import org.talend.components.rest.configuration.RequestConfig;
import org.talend.components.rest.configuration.auth.Authentication;
import org.talend.components.rest.configuration.auth.Authorization;
import org.talend.components.rest.configuration.auth.Basic;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.BaseComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@WithComponents(value = "org.talend.components.rest")
class ClientTest {

    private final static String HTTP_BIN_BASE = "http://tal-rd169.talend.lan:8085";

    private final static String DONT_CHECK = "%DONT_CHECK%";

    private final static int CONNECT_TIMEOUT = 5000;

    private final static int READ_TIMEOUT = 5000;

    @Service
    RestService service;

    @Service
    JsonReaderFactory jsonReaderFactory;

    @Injected
    private BaseComponentsHandler handler;

    private RequestConfig config;

    @BeforeEach
    void buildConfig() {
        // Inject needed services
        handler.injectServices(this);

        Client client = service.getClient();

        config = RequestConfigBuilder.getEmptyRequestConfig();

        config.getDataset().getDatastore().setBase(HTTP_BIN_BASE);
        config.getDataset().setConnectionTimeout(CONNECT_TIMEOUT);
        config.getDataset().setReadTimeout(READ_TIMEOUT);
    }

    private String getEncoding(Record rec) {
        String encoding = "UTF-8";

        Map<String, String> headers = rec.getArray(Record.class, "headers").stream()
                .collect(Collectors.toMap(r -> r.getString("key"), r -> r.getString("value")));
        String contentTypeStr = Optional.ofNullable(headers.get(ContentType.HEADER_KEY)).orElse("");
        if (contentTypeStr.indexOf(';') > 1) {
            String[] split = contentTypeStr.split(";");
            contentTypeStr = split[0];
            encoding = split[1];
        }

        return encoding;
    }

    private String getBodyAsString(Record rec) {
        String enc = getEncoding(rec);
        byte[] body = rec.getBytes("body");
        return new String(body, 0, body.length, Charset.forName(enc));
    }

    @Test
    void httpbinGet() throws Exception {
        config.getDataset().setResource("get");
        config.getDataset().setMethodType(HttpMethod.GET);

        Record resp = service.execute(config);

        assertEquals(200, resp.getInt("status"));

        List<Record> headers = Collections.unmodifiableList(new ArrayList<>(resp.getArray(Record.class, "headers")));

        Map<String, String> headerToCheck = new HashMap<>();
        headerToCheck.put("Server", DONT_CHECK);
        headerToCheck.put("Access-Control-Allow-Origin", "*");
        headerToCheck.put("Access-Control-Allow-Credentials", "true");
        headerToCheck.put("Connection", "keep-alive");
        headerToCheck.put("Content-Length", DONT_CHECK);
        headerToCheck.put("X-Powered-By", "Flask");
        headerToCheck.put("Content-Type", "application/json");
        headerToCheck.put("Date", DONT_CHECK);
        headerToCheck.put("X-Processed-Time", DONT_CHECK);

        headers.forEach(e -> {
            String expected = headerToCheck.get(e.getString("key"));
            if (!DONT_CHECK.equals(expected)) {
                assertEquals(expected, e.getString("value"));
            }
            headerToCheck.remove(e.getString("key"));
        });
        assertTrue(headerToCheck.size() > 0); // it may have more header returned by the server

        String bodyS = getBodyAsString(resp);
        JsonObject bodyJson = jsonReaderFactory.createReader(new ByteArrayInputStream((bodyS == null ? "" : bodyS).getBytes()))
                .readObject();

        assertEquals(JsonValue.ValueType.OBJECT, bodyJson.getJsonObject("args").getValueType());

        Map<String, String> headersValid = new HashMap<>();
        headersValid.put("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        headersValid.put("Connection", "keep-alive");
        headersValid.put("Host", HTTP_BIN_BASE.substring(7));
        headersValid.put("User-Agent", DONT_CHECK);

        JsonObject headersJson = bodyJson.getJsonObject("headers");
        headersJson.keySet().stream().forEach(k -> {
            if (!DONT_CHECK.equals(headersValid.get(k))) {
                assertEquals(headersValid.get(k), headersJson.getString(k));
            }
            headersValid.remove(k);
        });
        assertTrue(headersValid.isEmpty());

        assertEquals(HTTP_BIN_BASE + "/get", bodyJson.getString("url"));
    }

    /**
     * If there are some parameters set, if false is given to setHasXxxx those parameters should not be passed.
     *
     * @throws Exception
     */
    @Test
    void testParamsDisabled() throws Exception {
        HttpMethod[] verbs = { HttpMethod.DELETE, HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT };
        config.getDataset().setResource("get");
        config.getDataset().setMethodType(HttpMethod.GET);

        List<Param> queryParams = new ArrayList<>();
        queryParams.add(new Param("params1", "value1"));
        config.getDataset().setHasQueryParams(false);
        config.getDataset().setQueryParams(queryParams);

        List<Param> headerParams = new ArrayList<>();
        headerParams.add(new Param("Header1", "simple value"));
        config.getDataset().setHasHeaders(false);
        config.getDataset().setHeaders(headerParams);

        Record resp = service.execute(config);

        assertEquals(200, resp.getInt("status"));

        JsonReader payloadReader = jsonReaderFactory.createReader(new StringReader(getBodyAsString(resp)));
        JsonObject payload = payloadReader.readObject();

        assertEquals(0, payload.getJsonObject("args").size());
        assertEquals(4, payload.getJsonObject("headers").size());

    }

    @Test
    void testQueryAndHeaderParams() throws Exception {
        HttpMethod[] verbs = { HttpMethod.DELETE, HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT };
        for (HttpMethod m : verbs) {
            config.getDataset().setResource(m.name().toLowerCase());
            config.getDataset().setMethodType(m);

            List<Param> queryParams = new ArrayList<>();
            queryParams.add(new Param("params1", "value1"));
            queryParams.add(new Param("params2", "<name>Dupont & Dupond</name>"));
            config.getDataset().setHasQueryParams(true);
            config.getDataset().setQueryParams(queryParams);

            List<Param> headerParams = new ArrayList<>();
            headerParams.add(new Param("Header1", "simple value"));
            headerParams.add(new Param("Header2", "<name>header Dupont & Dupond</name>"));
            config.getDataset().setHasHeaders(true);
            config.getDataset().setHeaders(headerParams);

            Record resp = service.execute(config);

            assertEquals(200, resp.getInt("status"));

            JsonReader payloadReader = jsonReaderFactory.createReader(new StringReader(getBodyAsString(resp)));
            JsonObject payload = payloadReader.readObject();

            assertEquals("value1", payload.getJsonObject("args").getString("params1"));
            assertEquals("<name>Dupont & Dupond</name>", payload.getJsonObject("args").getString("params2"));
            assertEquals("simple value", payload.getJsonObject("headers").getString("Header1"));
            assertEquals("<name>header Dupont & Dupond</name>", payload.getJsonObject("headers").getString("Header2"));
        }
    }

    @Test
    void testBasicAuth() throws Exception {
        String user = "my_user";
        String pwd = "my_password";

        Basic basic = new Basic();
        basic.setUsername(user);
        basic.setPassword(pwd);

        Authentication auth = new Authentication();
        auth.setType(Authorization.AuthorizationType.Basic);
        auth.setBasic(basic);

        config.getDataset().setAuthentication(auth);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setResource("/basic-auth/{user}/{pwd}");
        config.setStopIfNotOk(false);

        // httpbin expects login/pwd given in header Basic as path param
        config.getDataset().setResource("/basic-auth/" + user + "/wrong_" + pwd);
        Record respForbidden = service.execute(config);
        assertEquals(401, respForbidden.getInt("status"));

        config.getDataset().setResource("/basic-auth/" + user + "/" + pwd);
        Record respOk = service.execute(config);
        assertEquals(200, respOk.getInt("status"));
    }

    @Test
    void testBearerAuth() throws Exception {
        Authentication auth = new Authentication();
        auth.setType(Authorization.AuthorizationType.Bearer);

        config.getDataset().getDatastore().setBase(HTTP_BIN_BASE);
        config.getDataset().setAuthentication(auth);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setResource("/bearer");
        config.setStopIfNotOk(false);

        auth.setBearerToken("");
        Record respKo = service.execute(config);
        assertEquals(401, respKo.getInt("status"));

        auth.setBearerToken("token-123456789");
        Record respOk = service.execute(config);
        assertEquals(200, respOk.getInt("status"));
    }

    @Test
    void testRedirect() throws Exception {
        boolean followRedirects_backup = HttpURLConnection.getFollowRedirects();
        HttpURLConnection.setFollowRedirects(false);

        String redirect_url = HTTP_BIN_BASE + "/get?redirect=ok";
        config.getDataset().setResource("redirect-to?url=" + redirect_url);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setRedirect(true);
        config.getDataset().setMaxRedirect(1);


        Record resp = service.execute(config);
        assertEquals(200, resp.getInt("status"));

        JsonReader payloadReader = jsonReaderFactory.createReader(new StringReader(getBodyAsString(resp)));
        JsonObject payload = payloadReader.readObject();

        assertEquals("ok", payload.getJsonObject("args").getString("redirect"));
        HttpURLConnection.setFollowRedirects(followRedirects_backup);
    }

    @ParameterizedTest
    @CsvSource(value = { "6,-1", "3,3", "3,5"})
    void testRedirectNOk(final int nbRedirect, final int maxRedict) throws Exception {
        boolean followRedirects_backup = HttpURLConnection.getFollowRedirects();
        HttpURLConnection.setFollowRedirects(false);

        config.getDataset().setResource("redirect/"+nbRedirect);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setRedirect(true);
        config.getDataset().setMaxRedirect(maxRedict);


        Record resp = service.execute(config);
        assertEquals(200, resp.getInt("status"));

        HttpURLConnection.setFollowRedirects(followRedirects_backup);
    }

    @ParameterizedTest
    @CsvSource(value = { "3,0", "3,1", "3,2", "5,4"})
    void testRedirectNko(final int nbRedirect, final int maxRedict) throws Exception {
        boolean followRedirects_backup = HttpURLConnection.getFollowRedirects();
        HttpURLConnection.setFollowRedirects(false);

        config.getDataset().setResource("redirect/"+nbRedirect);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setRedirect(true);
        config.getDataset().setMaxRedirect(maxRedict);


        Exception e = assertThrows(IllegalArgumentException.class, () -> service.execute(config));
        log.info(e.getMessage());

        HttpURLConnection.setFollowRedirects(followRedirects_backup);
    }

    @ParameterizedTest
    @CsvSource(value = { "auth-int,MD5", "auth,MD5", "auth-int,MD5-sess", "auth,MD5-sess", "auth-int,SHA-256", "auth,SHA-256",
            "auth-int,SHA-512", "auth,SHA-512" })
    void testDisgestAuth(final String qop, final String algo) {
        String user = "my_user";
        String pwd = "my_password";

        Basic basic = new Basic();
        basic.setUsername(user);
        basic.setPassword(pwd);

        Authentication auth = new Authentication();
        auth.setType(Authorization.AuthorizationType.Digest);
        auth.setBasic(basic);

        testDigestAuthWithQop(200, user, pwd, auth, qop);
        testDigestAuthWithQop(401, user, pwd + "x", auth, qop);

        testDigestAuthWithQopAlgo(200, user, pwd, auth, qop, algo);
        testDigestAuthWithQopAlgo(401, user, pwd + "x", auth, qop, algo);
    }

    private void testDigestAuthWithQop(final int expected, final String user, final String pwd, final Authentication auth,
            final String qop) {
        config.getDataset().setAuthentication(auth);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setResource("digest-auth/" + qop + "/" + user + "/" + pwd);

        Record resp = service.execute(config);
        assertEquals(expected, resp.getInt("status"));
    }

    private void testDigestAuthWithQopAlgo(final int expected, final String user, final String pwd, final Authentication auth,
            final String qop, final String algo) {
        config.getDataset().setAuthentication(auth);
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setResource("digest-auth/" + qop + "/" + user + "/" + pwd + "/" + algo);

        Record resp = service.execute(config);
        assertEquals(expected, resp.getInt("status"));
    }

    @ParameterizedTest
    @CsvSource(value = { "json", "xml", "html" })
    void testformats(final String type) {
        config.getDataset().setMethodType(HttpMethod.GET);
        config.getDataset().setResource(type);

        Record resp = service.execute(config);

        assertEquals(200, resp.getInt("status"));
    }

    static IntStream threeSixNine() {
        return IntStream.range(1, 10).filter(l -> l % 3 == 0);
    }

    /*
     * @Test
     * void testReadStreaming() throws Exception {
     * config.getDataset().setMethodType(HttpMethod.GET);
     * config.getDataset().setResource("/stream/10");
     *
     * Record resp = service.execute(config);
     * assertEquals(200, resp.getInt("status"));
     * }
     *
     * @Test
     * void testReadBytes() throws Exception {
     * config.getDataset().setMethodType(HttpMethod.GET);
     * config.getDataset().setResource("/bytes/100");
     *
     * Record resp = service.execute(config);
     * assertEquals(200, resp.getInt("status"));
     * }
     *
     * @Test
     * void testReadHml() throws Exception {
     * config.getDataset().setMethodType(HttpMethod.GET);
     * config.getDataset().setResource("links/10/0");
     *
     * Record resp = service.execute(config);
     * assertEquals(200, resp.getInt("status"));
     * }
     */

}