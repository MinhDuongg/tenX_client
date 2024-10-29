package com.example.spring_boot.model;


import com.example.spring_boot.net.TenXResponseGetter;

/**
 * Represents a {@code TenXObject} that has properties or method that can make HTTP requests.
 * Examples: - list that can retrieve next page - event that can fetch associated object
 */
public interface TenXActiveObject {

    void setResponseGetter(TenXResponseGetter responseGetter);

    /**
     * Calls {@code setResponseGetter} on the {@code object} if it's supported.
     *
     * @param object the object to set the {@code TenXResponseGetter} for.
     * @param responseGetter the {@code TenXResponseGetter} instance to use for making further
     *     requests.
     */
    default void trySetResponseGetter(Object object, TenXResponseGetter responseGetter) {
        if (object instanceof TenXActiveObject) {
            ((TenXActiveObject) object).setResponseGetter(responseGetter);
        }
    }
}
