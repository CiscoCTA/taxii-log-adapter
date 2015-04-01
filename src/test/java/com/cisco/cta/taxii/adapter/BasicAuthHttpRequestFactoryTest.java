package com.cisco.cta.taxii.adapter;

import ch.qos.logback.core.Appender;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.RequestAuthCache;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import static com.cisco.cta.taxii.adapter.IsEventContaining.verifyLog;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


@SuppressWarnings("all")
public class BasicAuthHttpRequestFactoryTest {

    private ClientHttpRequestFactory factory;
    private HttpClient httpClient;
    private TaxiiServiceSettings connSettings;
    private ProxySettings proxySettings;
    private CredentialsProvider credentialsProvider;

    @Mock
    private Appender mockAppender;

    @Before
    public void setUp() throws Exception {
        httpClient = HttpClients.createDefault();
        credentialsProvider = Mockito.mock(CredentialsProvider.class);
        connSettings = TaxiiServiceSettingsFactory.createDefaults();
        proxySettings = ProxySettingsFactory.createDefaults();
        credentialsProvider = Mockito.mock(CredentialsProvider.class);
        factory = new BasicAuthHttpRequestFactory(httpClient, connSettings, proxySettings, credentialsProvider);
        MockitoAnnotations.initMocks(this);
        when(mockAppender.getName()).thenReturn("MOCK");
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestAuthCache.class.getCanonicalName())).addAppender(mockAppender);
    }

    @Test
    public void tryConnectWithBasicAuthentication() throws Exception {
        try {
            ClientHttpRequest req = factory.createRequest(connSettings.getPollEndpoint().toURI(), HttpMethod.POST);
            req.execute();
            fail("connection must fail");
        } catch (Throwable e) {
            assertThat(e, instanceOf(HttpHostConnectException.class));
        }
        verifyLog(mockAppender, "Re-using cached 'basic' auth scheme for http://localhost:80");
    }

    @Test(expected=SocketTimeoutException.class)
    public void timeoutConnection() throws Exception {
        BasicAuthHttpRequestFactory factoryImpl = (BasicAuthHttpRequestFactory) factory;
        factoryImpl.setReadTimeout(10);
        Thread server = null;
        try (ServerRunnable serverRunnable = new ServerRunnable()) {
            server = new Thread(serverRunnable);
            ClientHttpRequest req = factory.createRequest(connSettings.getPollEndpoint().toURI(), HttpMethod.POST);
            req.execute();
        } finally {
            server.join(10);
        }
    }


    private static class ServerRunnable implements Runnable, AutoCloseable {
        
        private final ServerSocket serverSocket;
        private Exception runException;

        public ServerRunnable() throws Exception {
            InetAddress localhost = InetAddress.getByName("localhost"); 
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(8080, 1, localhost);
        }

        @Override
        public void run() {
            try {
                serverSocket.accept();
            } catch (Exception e) {
                runException = e;
            }
        }
        
        @Override
        public void close() throws Exception {
            serverSocket.close();
        }

    }
}
