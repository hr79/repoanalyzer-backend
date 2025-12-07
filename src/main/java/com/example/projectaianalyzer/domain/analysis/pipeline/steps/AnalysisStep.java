package com.example.projectaianalyzer.domain.analysis.pipeline.steps;

public interface AnalysisStep<I, O> {
    O execute(I input);
}
