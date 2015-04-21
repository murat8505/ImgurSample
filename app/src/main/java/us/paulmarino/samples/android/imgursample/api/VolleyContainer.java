package us.paulmarino.samples.android.imgursample.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Basic Singleton to return our Volley instance
 */
public class VolleyContainer {
    private static VolleyContainer sVolley = null;
    private RequestQueue mRequestQueue;

    private VolleyContainer(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleyContainer get(Context context) {
        if (sVolley == null)
            sVolley = new VolleyContainer(context);
        return sVolley;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
