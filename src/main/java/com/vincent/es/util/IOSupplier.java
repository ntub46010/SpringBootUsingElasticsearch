package com.vincent.es.util;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier<V> {
    V get() throws IOException;
}
