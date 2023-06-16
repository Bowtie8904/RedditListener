package bt.redditlistener.web;

import bt.redditlistener.event.AuthCodeReceived;
import bt.scheduler.Threads;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@RestController
@Slf4j
public class AuthController
{
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @RequestMapping("/auth")
    public String authorizationCode(@RequestParam(required = false) String code, @RequestParam(required = false) String error)
    {
        if (code == null)
        {
            log.warn("Authorization failed. error=" + error);

            Threads.get().scheduleDaemon(() -> System.exit(0), 3, TimeUnit.SECONDS);

            return "Authorization failed. The application will shut down.";
        }
        else
        {
            log.info("Authorization successful. code=" + code.substring(0, 5) + "...");
            this.applicationEventPublisher.publishEvent(new AuthCodeReceived(code));
            return "Authorization successful. You can close this window now.";
        }
    }
}