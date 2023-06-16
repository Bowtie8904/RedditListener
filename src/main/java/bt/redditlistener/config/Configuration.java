package bt.redditlistener.config;

import bt.io.xml.XML;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author &#8904
 */
@Component
@Slf4j
public class Configuration
{
    private String userAgent;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private int threadsPerRequest;
    private int requestInterval;
    private int hiddenRequestInterval;
    private int hiddenThreadLifetimeHours;
    private boolean trackThreadComments;
    private int x;
    private int y;

    public Configuration()
    {
        load(new File("./config.xml"));
    }

    public void load(File configFile)
    {
        try
        {
            var doc = XML.parse(configFile);
            this.clientId = doc.selectSingleNode("//" + XML.lowerNode("clientId")).getText();
            this.clientSecret = doc.selectSingleNode("//" + XML.lowerNode("clientSecret")).getText();
            this.redirectUri = doc.selectSingleNode("//" + XML.lowerNode("redirectUri")).getText();
            this.userAgent = doc.selectSingleNode("//" + XML.lowerNode("userAgent")).getText();
            this.threadsPerRequest = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("threadsPerRequest")).getText());
            this.requestInterval = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("requestInterval")).getText());
            this.hiddenRequestInterval = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("hiddenRequestInterval")).getText());
            this.hiddenThreadLifetimeHours = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("hiddenThreadLifetimeHours")).getText());
            this.x = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("popUpX")).getText());
            this.y = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("popUpY")).getText());
            this.trackThreadComments = Boolean.parseBoolean(doc.selectSingleNode("//" + XML.lowerNode("trackThreadComments")).getText());
        }
        catch (DocumentException e)
        {
            log.error("Failed to read configuration", e);
        }
    }

    public String getRedirectUri()
    {
        return this.redirectUri;
    }

    public void setRedirectUri(String redirectUri)
    {
        this.redirectUri = redirectUri;
    }

    public boolean isTrackThreadComments()
    {
        return this.trackThreadComments;
    }

    public void setTrackThreadComments(boolean trackThreadComments)
    {
        this.trackThreadComments = trackThreadComments;
    }

    /**
     * @return the x
     */
    public int getX()
    {
        return this.x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY()
    {
        return this.y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * @return the requestInterval
     */
    public int getRequestInterval()
    {
        return this.requestInterval;
    }

    /**
     * @param requestInterval the requestInterval to set
     */
    public void setRequestInterval(int requestInterval)
    {
        this.requestInterval = requestInterval;
    }

    /**
     * @return the threadsPerRequest
     */
    public int getThreadsPerRequest()
    {
        return this.threadsPerRequest;
    }

    /**
     * @param threadsPerRequest the threadsPerRequest to set
     */
    public void setThreadsPerRequest(int threadsPerRequest)
    {
        this.threadsPerRequest = threadsPerRequest;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent()
    {
        return this.userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * @return the clientId
     */
    public String getClientId()
    {
        return this.clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret()
    {
        return this.clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    public int getHiddenRequestInterval()
    {
        return this.hiddenRequestInterval;
    }

    public void setHiddenRequestInterval(int hiddenRequestInterval)
    {
        this.hiddenRequestInterval = hiddenRequestInterval;
    }

    public int getHiddenThreadLifetimeHours()
    {
        return this.hiddenThreadLifetimeHours;
    }

    public void setHiddenThreadLifetimeHours(int hiddenThreadLifetimeHours)
    {
        this.hiddenThreadLifetimeHours = hiddenThreadLifetimeHours;
    }
}