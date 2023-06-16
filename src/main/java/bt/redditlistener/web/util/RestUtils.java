package bt.redditlistener.web.util;

import bt.io.json.JSON;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author &#8904
 */
@Slf4j
public final class RestUtils
{
    /**
     * Performs a POST request to the given endpoint and transmits the given JSON.
     *
     * @param action   The action that should be performed. The value will be added to the json with the key 'Task'.
     * @param endpoint The endpoint for the request.
     * @param json     The JSON that should be sent to the endpoint.
     *
     * @return The JSON response from the endpoint.
     *
     * @throws IOException
     */
    public static synchronized JSONObject post(String endpoint, Map<String, String> headers, JSONObject json) throws IOException
    {
        JSONObject returnJson = null;

        String url = endpoint;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type",
                               "application/json; charset=UTF-8");
        con.setRequestProperty("User-Agent",
                               "Mozilla/5.0");
        con.setRequestProperty("Accept",
                               "application/json");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        if (headers != null)
        {
            for (String key : headers.keySet())
            {
                con.setRequestProperty(key,
                                       headers.get(key));
            }
        }

        try (OutputStream os = con.getOutputStream())
        {
            os.write(json.toString().getBytes("UTF-8"));
        }
        catch (IOException e)
        {
            headers.put("responsecode", con.getResponseCode() + "");
            throw e;
        }

        StringBuffer response = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
        {

            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
        }
        catch (IOException e)
        {
            headers.put("responsecode", con.getResponseCode() + "");
            throw e;
        }

        for (var headerName : con.getHeaderFields().keySet())
        {
            if (headerName != null)
            {
                headers.put(headerName.toLowerCase(), con.getHeaderField(headerName));
            }
        }

        returnJson = JSON.parse(response != null ? response.toString() : null);

        return returnJson;
    }

    public static synchronized JSONObject post(String endpoint, Map<String, String> headers, String... params) throws IOException
    {
        JSONObject returnJson = null;

        String url = endpoint;

        String urlParams = "";

        for (String param : params)
        {
            urlParams += param;
        }

        if (urlParams.endsWith("&"))
        {
            urlParams = urlParams.substring(0,
                                            urlParams.length() - 1);
        }

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type",
                               "application/x-www-form-urlencoded");
        con.setRequestProperty("User-Agent",
                               "Mozilla/5.0");
        con.setRequestProperty("Accept",
                               "application/json");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        if (headers != null)
        {
            for (String key : headers.keySet())
            {
                con.setRequestProperty(key,
                                       headers.get(key));
            }
        }

        try (OutputStream os = con.getOutputStream())
        {
            os.write(urlParams.getBytes("UTF-8"));
        }
        catch (IOException e)
        {
            headers.put("responsecode", con.getResponseCode() + "");
            throw e;
        }

        StringBuffer response = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
        {
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
        }
        catch (IOException e)
        {
            headers.put("responsecode", con.getResponseCode() + "");
            throw e;
        }

        for (var headerName : con.getHeaderFields().keySet())
        {
            if (headerName != null)
            {
                headers.put(headerName.toLowerCase(), con.getHeaderField(headerName));
            }
        }

        returnJson = JSON.parse(response != null ? response.toString() : null);

        return returnJson;
    }

    /**
     * Performs a GET request to the given endpoint with the given parameters.
     *
     * <p>
     * Use {@link #formParam(String, String)} to correctly format parameter pairs of name and value.
     * </p>
     *
     * @param action   The action that should be performed. The value will be added as the parameter 'task'
     * @param endpoint The endpoint for the request.
     * @param params   URL parameters for the endpoint. Format: ?key=value&
     *
     * @return The JSON response from the endpoint.
     *
     * @throws IOException
     */
    public static synchronized JSONObject get(String endpoint, Map<String, String> headers, String... params) throws IOException
    {
        JSONObject json = null;

        String url = endpoint;

        if (params.length != 0)
        {
            url += "?";
        }

        for (String param : params)
        {
            url += param;
        }

        if (url.endsWith("&"))
        {
            url = url.substring(0,
                                url.length() - 1);
        }

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent",
                               "Mozilla/5.0");
        con.setRequestProperty("Accept",
                               "application/json");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        if (headers != null)
        {
            for (String key : headers.keySet())
            {
                con.setRequestProperty(key,
                                       headers.get(key));
            }
        }

        StringBuffer response = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
        {

            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
        }
        catch (IOException e)
        {
            headers.put("responsecode", con.getResponseCode() + "");
            throw e;
        }

        for (var headerName : con.getHeaderFields().keySet())
        {
            if (headerName != null)
            {
                headers.put(headerName.toLowerCase(), con.getHeaderField(headerName));
            }
        }

        json = JSON.parse(response != null ? response.toString() : null);

        return json;
    }

    /**
     * Formats the given Strings in a corect url parameter format.
     *
     * @param key
     * @param value
     *
     * @return The formatted String
     *
     * <pre>
     * key=value&
     *         </pre>
     */
    public static String formParam(String key, String value)
    {
        StringBuilder result = new StringBuilder();

        try
        {
            result.append(URLEncoder.encode(key,
                                            "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value,
                                            "UTF-8"));
            result.append("&");
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("Used encoding is not supported", e);
        }

        return result.toString();
    }
}