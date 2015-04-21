package us.paulmarino.samples.android.imgursample.api;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import us.paulmarino.samples.android.imgursample.Config;

import static us.paulmarino.samples.android.imgursample.util.LogUtils.LOGE;
import static us.paulmarino.samples.android.imgursample.util.LogUtils.makeLogTag;

/**
 * Custom Volley Request for Imgur Subreddit Gallery
 */
public class ImgurApiRequest<T> extends Request<T> {
    private static final String TAG = makeLogTag(ImgurApiRequest.class);

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_CLIENT_ID = "Client-ID " + Config.IMGUR_CLIENT_ID;

    private static final int TIMEOUT_MS = 10 * 1000;
    private static final int MAX_RETRIES = 3;
    private static final int NO_RETRIES = 0;
    private static final float BACKOFF_MULTIPLIER = 2.0f;

    private final Response.Listener<T> mListener;
    private Gson mGson;
    private TypeToken<T> mType;
    private byte[] mBody;

    public ImgurApiRequest(TypeToken<T> type, int method, String url,
                           Response.Listener<T> listener,
                           Response.ErrorListener errorListener, boolean backoff) {
        super(method, url, errorListener);
        mListener = listener;
        mGson = new Gson();
        mType = type;
        mBody = null;

        if (backoff)
            setRetryPolicy(new ImgurRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULTIPLIER));
        else
            setRetryPolicy(new ImgurRetryPolicy(TIMEOUT_MS, NO_RETRIES, BACKOFF_MULTIPLIER));
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {

        String json;
        try {
            json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            json = new String(response.data);
        }

        T parsed = null;
        if (!TextUtils.isEmpty(json)) {
            try {
                // T is parametrized in the constructor, so suppressing unchecked cast warning.
                @SuppressWarnings("unchecked")
                T tmpParsed = (T) mGson.fromJson(json, mType.getType());
                parsed = tmpParsed;
            } catch (JsonSyntaxException e) {
                LOGE(TAG, "Invalid JSON: " + json, e);
                return Response.error(new VolleyError("Error: could not parse JSON response"));
            }
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();

        // Add Authorization and Client ID to headers
        headers.put(HEADER_AUTH, HEADER_CLIENT_ID);

        return headers;
    }

    @Override
    public byte[] getBody() {
        return mBody;
    }

    /**
     * Custom Retry Policy
     */
    private static class ImgurRetryPolicy extends DefaultRetryPolicy {

        public ImgurRetryPolicy(int initialTimeoutMs, int maxNumRetries,
                                float backoffMultiplier) {
            super(initialTimeoutMs, maxNumRetries, backoffMultiplier);
        }

        @Override
        public void retry(VolleyError error) throws VolleyError {
            if (error.networkResponse.statusCode == 401)
                throw error;
            else
                super.retry(error);
        }
    }
}
