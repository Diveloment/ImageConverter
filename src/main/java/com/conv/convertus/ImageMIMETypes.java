package com.conv.convertus;

import java.util.Arrays;
import java.util.Objects;

public enum ImageMIMETypes {
    GIF("gif", "image/gif"),
    JPEG("jpeg", "image/jpeg"),
    JPG("jpg", "image/jpg"),
    PJPEG("jpeg", "image/pjpeg"),
    PNG("png", "image/png"),
    TIFF("tiff", "image/tiff"),
    WEBP("webp", "image/webp"),
    BMP("bmp", "image/bmp"),
    DEFAULT(null, null),
    NONE("", "");

    private String extension;

    private String mimeType;

    ImageMIMETypes(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static String toExtension(String mimeType) {
        ImageMIMETypes imageMIMETypes = Arrays
                .stream(ImageMIMETypes.values())
                .filter(types -> Objects.equals(types.mimeType,mimeType))
                .findFirst()
                .orElse(DEFAULT);
        return imageMIMETypes.extension;
    }

    public static String toExtensionDot(String mimeType) {
        String imageMIMEType = Arrays
                .stream(ImageMIMETypes.values())
                .filter(types -> Objects.equals(types.mimeType,mimeType))
                .findFirst().map(e -> "." + e.getExtension())
                .orElse(NONE.getExtension());
        return imageMIMEType;
    }
}

