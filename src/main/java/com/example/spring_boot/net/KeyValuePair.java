package com.example.spring_boot.net;

import java.util.AbstractMap;

public class KeyValuePair<K, V> extends AbstractMap.SimpleEntry<K, V>  {
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new instance of the {@link KeyValuePair} class using the specified key and value.
     *
     * @param key the key
     * @param value the value
     */
    public KeyValuePair(K key, V value) {
        super(key, value);
    }
}
