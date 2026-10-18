package org.klyx.flux.importer.snowstorm.reading;

@FunctionalInterface
public interface Reader<T> {
    T read(ReadingContext context);
}
