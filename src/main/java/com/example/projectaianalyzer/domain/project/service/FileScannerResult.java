package com.example.projectaianalyzer.domain.project.service;

import com.example.projectaianalyzer.domain.project.model.FileInfo;

import java.util.List;

public record FileScannerResult(List<FileInfo> fileInfoList, List<String> fileStructureSummaries) {
}
