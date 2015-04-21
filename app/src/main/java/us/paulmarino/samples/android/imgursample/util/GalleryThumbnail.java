package us.paulmarino.samples.android.imgursample.util;

import us.paulmarino.samples.android.imgursample.Config;

/**
 * Utility to simply generate specific Imgur thumbnail urls so we don't have to load
 * potentially massive images and eat up extra bandwidth.
 *
 * https://api.imgur.com/models/gallery_image
 */
public enum GalleryThumbnail {
    SIZE_MEDIUM("m"),    // 320x320 Thumbnail suffix
    SIZE_LARGE("l");     // 640x640 Thumbnail suffix

    private static final String THUMBNAIL_EXTENSION = ".jpg";

    private final String thumbnail;

    GalleryThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public static String buildThumbnailRequestUrl(final String imageId,
                                                  final GalleryThumbnail imageItem) {
        StringBuilder sb = new StringBuilder();
        sb.append(Config.IMGUR_IMAGE_ENDPOINT)
                .append(imageId)
                .append(imageItem.thumbnail)
                .append(THUMBNAIL_EXTENSION);

        return sb.toString();
    }
}
