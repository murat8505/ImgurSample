package us.paulmarino.samples.android.imgursample.model;

import java.util.ArrayList;

/**
 * Response used by {@link .api.ImgurApiClient}
 */
public class GalleryDataResponse {
    private ArrayList<GalleryItem> data;

    public ArrayList<GalleryItem> getData() {
        return data;
    }
}
