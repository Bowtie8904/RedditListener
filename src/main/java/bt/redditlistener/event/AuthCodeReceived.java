package bt.redditlistener.event;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
public class AuthCodeReceived
{
    private String code;

    public AuthCodeReceived(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return this.code;
    }
}