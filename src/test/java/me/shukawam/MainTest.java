
package me.shukawam;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.ricksbrown.cowsay.Cowsay;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    private static WebServer webServer;
    private static WebClient webClient;

    @BeforeAll
    static void startTheServer() {
        webServer = Main.startServer().await(Duration.ofSeconds(10));

        webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .addMediaSupport(JsonpSupport.create())
                .build();
    }

    @AfterAll
    static void stopServer() {
        if (webServer != null) {
            webServer.shutdown().await(10, TimeUnit.SECONDS);
        }
    }


    @Test
    void testMetrics() {
        WebClientResponse response = webClient.get()
                .path("/metrics")
                .request()
                .await(Duration.ofSeconds(5));
        assertThat(response.status().code(), is(200));
    }

    @Test
    void testHealth() {
        WebClientResponse response = webClient.get()
                .path("health")
                .request()
                .await(Duration.ofSeconds(5));
        assertThat(response.status().code(), is(200));
    }

    @Test
    public void testCowsayDefault() {
        var actual = webClient.get().path("/cowsay/say").request().await(Duration.ofSeconds(5));
        var expected = Cowsay.say(new String[] { "-f", "default", "Moo!" });
        assertEquals(expected, actual.content().as(String.class).await(), "HTTP Response of /cowsay/say");
    }

    @Test
    public void testCowsayWithMessage() {
        var actual = webClient.get().path("/cowsay/say").queryParam("say", "Hello").request().await(Duration.ofSeconds(5));
        var expected = Cowsay.say(new String[] { "-f", "default", "Hello" });
        assertEquals(expected, actual.content().as(String.class).await(), "HTTP Response of /cowsay/say?message=Hello");
    }

    @Test
    public void testCowsayWithMessageWithCowfile() {
        var actual = webClient.get().path("/cowsay/say").queryParam("say", "Hello").queryParam("cowfile", "www").request().await(Duration.ofSeconds(5));
        var expected = Cowsay.say(new String[] { "-f", "www", "Hello" });
        assertEquals(expected, actual.content().as(String.class).await(), "HTTP Response of /cowsay/say?message=Hello&cowfile=www");
    }

}
