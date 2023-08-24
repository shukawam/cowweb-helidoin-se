package me.shukawam;

import io.helidon.webserver.Routing.Rules;

import com.github.ricksbrown.cowsay.Cowsay;

import io.helidon.config.Config;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class CowwebService implements Service {

    private final String defaultMessage;
    private final String defaultCowfile;

    public CowwebService(Config config) {
        defaultMessage = config.get("cowweb.message").asString().orElse("Moo!");
        defaultCowfile = config.get("cowweb.cowfile").asString().orElse("default");
    }

    @Override
    public void update(Rules rules) {
        rules.get("/say", this::say);
    }

    private void say(ServerRequest req, ServerResponse res) {
        var params = new String[] { "-f", req.queryParams().first("cowfile").orElse(defaultCowfile),
                req.queryParams().first("say").orElse(defaultMessage) };
        res.send(Cowsay.say(params));
    }

}
