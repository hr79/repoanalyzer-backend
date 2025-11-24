package com.example.projectaianalyzer.domain.analysis.controller;

import com.example.projectaianalyzer.domain.analysis.dto.FinalAnalysisDto;
import com.example.projectaianalyzer.domain.analysis.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    @PostMapping("/analyze")
    public ResponseEntity<FinalAnalysisDto> analyze(@RequestParam String repoUrl) {
        FinalAnalysisDto analysisDto = analyzeService.analyze(repoUrl);
        return ResponseEntity.ok(analysisDto);
    }
}
