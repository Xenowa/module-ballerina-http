/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.http.transport.http2.frameleveltests;

import io.ballerina.stdlib.http.transport.contract.HttpClientConnector;
import io.ballerina.stdlib.http.transport.contract.HttpResponseFuture;
import io.ballerina.stdlib.http.transport.message.HttpCarbonMessage;
import io.ballerina.stdlib.http.transport.util.DefaultHttpConnectorListener;
import io.ballerina.stdlib.http.transport.util.TestUtil;
import io.ballerina.stdlib.http.transport.util.client.http2.MessageGenerator;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.DATA_FRAME_STREAM_03;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.DATA_FRAME_STREAM_03_DIFFERENT_DATA;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.END_SLEEP_TIME;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.GO_AWAY_FRAME_MAX_STREAM_03;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.HEADER_FRAME_STREAM_03;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.SETTINGS_FRAME;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.SETTINGS_FRAME_WITH_ACK;
import static io.ballerina.stdlib.http.transport.http2.frameleveltests.TestUtils.SLEEP_TIME;
import static org.testng.Assert.assertEquals;

/**
 * This contains a test case where the client sends a request after receiving a GoAway.
 * This tests whether there is a new connection opened from the client.
 */
public class Http2TcpServerSendRequestAfterGoAwayScenarioTest {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Http2TcpServerSendRequestAfterGoAwayScenarioTest.class);

    private HttpClientConnector h2ClientWithPriorKnowledge;
    private ServerSocket serverSocket;
    Semaphore semaphore = new Semaphore(0);

    @BeforeClass
    public void setup() throws InterruptedException {
        runTcpServer(TestUtil.HTTP_SERVER_PORT);
        h2ClientWithPriorKnowledge = TestUtils.setupHttp2PriorKnowledgeClient();
    }

    @Test
    private void testGoAwayForAllStreamsScenario() {
        HttpCarbonMessage httpCarbonMessage1 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        HttpCarbonMessage httpCarbonMessage2 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        HttpCarbonMessage httpCarbonMessage3 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        HttpCarbonMessage httpCarbonMessage4 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        HttpCarbonMessage httpCarbonMessage5 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        HttpCarbonMessage httpCarbonMessage6 = MessageGenerator.generateRequest(HttpMethod.POST, "Test Http2 Message");
        try {
            DefaultHttpConnectorListener msgListener1 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture1 = h2ClientWithPriorKnowledge.send(httpCarbonMessage1);
            responseFuture1.setHttpConnectorListener(msgListener1);
            semaphore.acquire();
            responseFuture1.sync();
            DefaultHttpConnectorListener msgListener2 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture2 = h2ClientWithPriorKnowledge.send(httpCarbonMessage2);
            responseFuture2.setHttpConnectorListener(msgListener2);
            semaphore.acquire();
            responseFuture2.sync();
            DefaultHttpConnectorListener msgListener3 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture3 = h2ClientWithPriorKnowledge.send(httpCarbonMessage3);
            responseFuture3.setHttpConnectorListener(msgListener3);
            semaphore.acquire();
            responseFuture3.sync();
            DefaultHttpConnectorListener msgListener4 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture4 = h2ClientWithPriorKnowledge.send(httpCarbonMessage4);
            responseFuture4.setHttpConnectorListener(msgListener4);
            semaphore.acquire();
            responseFuture4.sync();
            DefaultHttpConnectorListener msgListener5 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture5 = h2ClientWithPriorKnowledge.send(httpCarbonMessage5);
            responseFuture5.setHttpConnectorListener(msgListener5);
            semaphore.acquire();
            responseFuture5.sync();
            DefaultHttpConnectorListener msgListener6 = new DefaultHttpConnectorListener();
            HttpResponseFuture responseFuture6 = h2ClientWithPriorKnowledge.send(httpCarbonMessage6);
            responseFuture6.setHttpConnectorListener(msgListener6);
            semaphore.acquire();
            responseFuture6.sync();
            HttpCarbonMessage response1 = msgListener1.getHttpResponseMessage();
            assertEquals(response1.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world3");
            HttpCarbonMessage response2 = msgListener2.getHttpResponseMessage();
            assertEquals(response2.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world5");
            HttpCarbonMessage response3 = msgListener3.getHttpResponseMessage();
            assertEquals(response3.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world3");
            HttpCarbonMessage response4 = msgListener4.getHttpResponseMessage();
            assertEquals(response4.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world5");
            HttpCarbonMessage response5 = msgListener5.getHttpResponseMessage();
            assertEquals(response5.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world3");
            HttpCarbonMessage response6 = msgListener6.getHttpResponseMessage();
            assertEquals(response6.getHttpContent().content().toString(CharsetUtil.UTF_8), "hello world5");
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception occurred");
        }
    }

    private void runTcpServer(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                int numberOfConnections = 0;
                LOGGER.info("HTTP/2 TCP Server listening on port " + port);
                while (numberOfConnections < 6) {
                    Socket clientSocket = serverSocket.accept();
                    LOGGER.info("Accepted connection from: " + clientSocket.getInetAddress());
                    try (OutputStream outputStream = clientSocket.getOutputStream()) {
                        if (numberOfConnections % 2 == 0) {
                            sendGoAwayBeforeSendingHeaders(outputStream);
                        } else {
                            sendGoAwayAfterSendingHeaders(outputStream);
                        }
                        numberOfConnections += 1;
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }).start();
    }

    private void sendGoAwayBeforeSendingHeaders(OutputStream outputStream) throws IOException, InterruptedException {
        outputStream.write(SETTINGS_FRAME);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(SETTINGS_FRAME_WITH_ACK);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(GO_AWAY_FRAME_MAX_STREAM_03);
        outputStream.write(HEADER_FRAME_STREAM_03);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(DATA_FRAME_STREAM_03);
        semaphore.release();
        Thread.sleep(END_SLEEP_TIME);
    }

    private void sendGoAwayAfterSendingHeaders(OutputStream outputStream) throws IOException, InterruptedException {
        outputStream.write(SETTINGS_FRAME);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(SETTINGS_FRAME_WITH_ACK);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(HEADER_FRAME_STREAM_03);
        outputStream.write(GO_AWAY_FRAME_MAX_STREAM_03);
        Thread.sleep(SLEEP_TIME);
        outputStream.write(DATA_FRAME_STREAM_03_DIFFERENT_DATA);
        semaphore.release();
        Thread.sleep(END_SLEEP_TIME);
    }

    @AfterMethod
    public void cleanUp() throws IOException {
        h2ClientWithPriorKnowledge.close();
        serverSocket.close();
    }
}
