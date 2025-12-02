package com.example.projectaianalyzer.infra.util;

public interface FileStorage {
    void writeJson(Object data, String outputFilePath);
    void delete(String path);
}
