package dev.bebomny.beaverdam.watchpost.processors;

public interface ContentProcessor<T> {
    boolean supports(String sourceType);
    void process(T rawData, Integer sourceId);
}
