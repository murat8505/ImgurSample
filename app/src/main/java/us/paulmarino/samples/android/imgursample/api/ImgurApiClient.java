package us.paulmarino.samples.android.imgursample.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import us.paulmarino.samples.android.imgursample.Config;
import us.paulmarino.samples.android.imgursample.model.GalleryDataResponse;
import us.paulmarino.samples.android.imgursample.model.GalleryItem;

import static us.paulmarino.samples.android.imgursample.util.LogUtils.LOGD;
import static us.paulmarino.samples.android.imgursample.util.LogUtils.makeLogTag;

/**
 * Client for the Imgur API
 */
public class ImgurApiClient {
    private static final String TAG = makeLogTag(ImgurApiClient.class);

    private static final String GET_GALLERY_SUBREDDIT = "/gallery/r/{subreddit}";

    private static ImgurApiClient sApiClient;
    private final VolleyContainer mVolley;

    /**
     * Callback for when the gallery items are loaded
     */
    public interface GalleryLoadedListener {
        void onGalleryLoaded(ArrayList<GalleryItem> data);
        void onGalleryLoadError();
    }

    /**
     * Private constructor (Singleton)
     */
    private ImgurApiClient(Context context) {
        mVolley = VolleyContainer.get(context);
    }

    /**
     * Return instance of ImgurApiClient or make a new one
     */
    public static ImgurApiClient get(Context context) {
        if (sApiClient == null)
            sApiClient = new ImgurApiClient(context);
        return sApiClient;
    }

    /**
     * Retrieve a list of Imgur gallery items based on a Subreddit category
     *
     * @param category      Subreddit category name
     * @param listener      to be called when complete
     */
    public void loadGallery(final String category, final GalleryLoadedListener listener) {
        RequestQueue requestQueue = mVolley.getRequestQueue();

        final String path = GET_GALLERY_SUBREDDIT.replace("{subreddit}", category);
        ImgurApiRequest<GalleryDataResponse> galleryRequest = new ImgurApiRequest<>(
                (new TypeToken<GalleryDataResponse>() {}),
                Request.Method.GET,
                Config.IMGUR_ENDPOINT_URL + path,
                new Response.Listener<GalleryDataResponse>() {
                    @Override
                    public void onResponse(GalleryDataResponse response) {
                        if (response.getData() != null) {
                            ArrayList<GalleryItem> items = response.getData();
                            listener.onGalleryLoaded(items);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LOGD(TAG, "Error retrieving gallery items");
                        if (listener != null) {
                            listener.onGalleryLoadError();
                        }
                    }
                },
                true
        );

        galleryRequest.setTag(GET_GALLERY_SUBREDDIT);
        requestQueue.add(galleryRequest);
    }
}
