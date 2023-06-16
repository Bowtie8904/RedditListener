package bt.redditlistener.reddit;

import bt.redditlistener.config.Configuration;
import bt.redditlistener.event.AuthCodeReceived;
import bt.redditlistener.reddit.observ.RedditObservable;
import bt.redditlistener.web.util.RestUtils;
import bt.scheduler.Threads;
import com.google.common.net.HttpHeaders;
import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author &#8904
 */
@Component
@Slf4j
public class RedditConnector
{
    public static final String TOKEN_REQUEST_URL = "https://www.reddit.com/api/v1/access_token";
    public static final String CODE_REQUEST_URL = "https://www.reddit.com/api/v1/authorize?duration=permanent&response_type=code&scope=read,privatemessages,history";
    private DoubleProperty remainingRateLimit;
    private ObservableManager observableManager;
    private Configuration config;
    private HostServices hostServices;
    private RedditToken token;
    private volatile boolean codeRequested = false;
    private volatile boolean canRequest = true;

    public RedditConnector(Configuration config, ObservableManager observableManager)
    {
        this.config = config;
        this.observableManager = observableManager;
        this.remainingRateLimit = new SimpleDoubleProperty();
    }

    public DoubleProperty remainingRateLimitProperty()
    {
        return this.remainingRateLimit;
    }

    public void startScheduler()
    {
        Threads.get().scheduleWithFixedDelayDaemon(this::requestNewThreads,
                                                   1000,
                                                   this.config.getRequestInterval(),
                                                   TimeUnit.MILLISECONDS);
    }

    protected boolean tokenValid()
    {
        if (this.token == null)
        {
            if (!this.codeRequested)
            {
                requestAuthCode();
            }

            return false;
        }
        else if (!this.token.isValid())
        {
            return refreshToken();
        }

        return true;
    }

    public void requestAuthCode()
    {
        log.info("Requesting authorization code.");
        this.codeRequested = true;
        String url = CODE_REQUEST_URL
                + "&client_id=" + this.config.getClientId()
                + "&state=" + UUID.randomUUID()
                + "&redirect_uri=" + this.config.getRedirectUri();
        this.hostServices.showDocument(url);
    }

    @EventListener
    public void onAuthCodeReceived(AuthCodeReceived event)
    {
        if (event.getCode() != null)
        {
            requestToken(event.getCode());
        }
    }

    public boolean refreshToken()
    {
        String auth = this.config.getClientId() + ":" + this.config.getClientSecret();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        Map<String, String> headers = new HashMap<>();

        headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        headers.put(HttpHeaders.USER_AGENT, this.config.getUserAgent());

        JSONObject json = null;
        try
        {
            String refreshToken = this.token.getRefreshToken();

            json = RestUtils.post(TOKEN_REQUEST_URL,
                                  headers,
                                  RestUtils.formParam("grant_type", "refresh_token"),
                                  RestUtils.formParam("refresh_token", refreshToken));

            this.token = new RedditToken(json);
            this.token.setRefreshToken(refreshToken);

            log.info("Refreshed token which will now expire at " + new Timestamp(this.token.getExpiresAt()) + ". " + this.token);

            return true;
        }
        catch (IOException e)
        {
            log.error("Failed to refresh token", e);
            return false;
        }
    }

    public void requestToken(String code)
    {
        String auth = this.config.getClientId() + ":" + this.config.getClientSecret();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        Map<String, String> headers = new HashMap<>();

        headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        headers.put(HttpHeaders.USER_AGENT, this.config.getUserAgent());

        try
        {
            JSONObject json = RestUtils.post(TOKEN_REQUEST_URL,
                                  headers,
                                  RestUtils.formParam("grant_type", "authorization_code"),
                                  RestUtils.formParam("code", code),
                                  RestUtils.formParam("redirect_uri", this.config.getRedirectUri()));

            this.token = new RedditToken(json);

            log.info("Aquired new token which will expire at " + new Timestamp(this.token.getExpiresAt()) + ". " + this.token);
        }
        catch (IOException e)
        {
            log.error("Failed to request a token. " + e.getMessage());
        }

        this.codeRequested = false;
    }

    private Map<String, String> createRequestHeaders()
    {
        String authHeader = "Bearer " + this.token.getAccessToken();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        headers.put(HttpHeaders.USER_AGENT, this.config.getUserAgent());

        return headers;
    }

    private void evaluateRateLimit(double used, double remaining, double reset)
    {
        this.remainingRateLimit.set(remaining);

        if (remaining == 0)
        {
            log.warn("Rate limit reached. Next request allowed in " + reset + " seconds.");
            this.canRequest = false;

            Threads.get().scheduleDaemon(() ->
                                         {
                                             this.canRequest = true;
                                             log.debug("Rate limit reset.");
                                         }, (int)reset + 5, TimeUnit.SECONDS);
        }
    }

    public synchronized void requestNewThreads()
    {
        if (tokenValid())
        {
            List<RedditObservable> observables = new ArrayList<>(this.observableManager.observables());
            observables.sort(Comparator.comparing(RedditObservable::getNextRequest));
            requestNewThreads(observables);

            observables = new ArrayList<>(this.observableManager.hiddenObservables());
            observables.sort(Comparator.comparing(RedditObservable::getNextRequest));
            requestNewThreads(observables);
        }
        else
        {
            log.warn("Token is invalid. Cant request new threads yet.");
        }
    }

    public synchronized void requestNewThreads(List<RedditObservable> obs)
    {
        boolean canContinue = true;

        for (var ob : obs)
        {
            if (ob.getDeleteTimestamp() > -1 && System.currentTimeMillis() >= ob.getDeleteTimestamp())
            {
                log.debug("Deleting " + ob + " because its end of life was reached.");
                this.observableManager.removeHiddenObservable(ob);
                ob.onDelete();
            }
            else
            {
                canContinue = requestNewThreads(ob);

                if (!canContinue)
                {
                    break;
                }
            }
        }
    }

    public synchronized boolean requestNewThreads(RedditObservable obs)
    {
        boolean canContinue = true;

        if (this.canRequest)
        {
            if (System.currentTimeMillis() >= obs.getNextRequest())
            {
                var headers = createRequestHeaders();

                var params = obs.createRequestParameters();

                try
                {
                    log.debug("Requesting new content for " + obs.getName() + " from " + obs.getRequestUrl() + " with params " + Arrays.toString(params));
                    var json = RestUtils.get(obs.getRequestUrl(), headers, params);

                    obs.parseNewNotifications(json);
                    obs.setNextRequest(System.currentTimeMillis() + (obs.isHidden() ? this.config.getHiddenRequestInterval() : this.config.getRequestInterval()));

                    evaluateRateLimit(Double.parseDouble(headers.get("x-ratelimit-used")),
                                      Double.parseDouble(headers.get("x-ratelimit-remaining")),
                                      Double.parseDouble(headers.get("x-ratelimit-reset")));
                }
                catch (IOException e)
                {
                    log.error("Failed to request new content for " + obs.getName() + " from " + obs.getRequestUrl() + " with params " + Arrays.toString(params) + ". " + e.getMessage());
                }
            }
        }
        else
        {
            canContinue = false;
        }

        return canContinue;
    }

    public HostServices getHostServices()
    {
        return this.hostServices;
    }

    public void setHostServices(HostServices hostServices)
    {
        this.hostServices = hostServices;
    }
}