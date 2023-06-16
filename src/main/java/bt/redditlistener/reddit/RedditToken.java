package bt.redditlistener.reddit;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/**
 * @author &#8904
 */
@ToString
@Slf4j
public class RedditToken
{
    @ToString.Exclude
    private String accessToken;
    private long expiresAt;
    private String scope;
    private String type;
    @ToString.Exclude
    private String refreshToken;

    public RedditToken(JSONObject json)
    {
        parse(json);
    }

    public void parse(JSONObject json)
    {
        this.accessToken = json.getString("access_token");

        if (json.has("refresh_token"))
        {
            this.refreshToken = json.getString("refresh_token");
        }

        this.scope = json.getString("scope");
        this.type = json.getString("token_type");
        long expiresIn = json.getLong("expires_in");
        this.expiresAt = System.currentTimeMillis() + ((expiresIn - 10) * 1000);
    }

    public boolean isValid()
    {
        return System.currentTimeMillis() < this.expiresAt;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken()
    {
        return this.accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    /**
     * @return the expiresAt
     */
    public long getExpiresAt()
    {
        return this.expiresAt;
    }

    /**
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(long expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    /**
     * @return the scope
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    public String getRefreshToken()
    {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
    }
}