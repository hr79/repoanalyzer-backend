package com.example.projectaianalyzer.domain.analysis.service;

import lombok.Getter;

@Getter
public enum FileRole {
    CONTROLLER("controller"),
    SERVICE("service"),
    REPOSITORY("repository"),
    CONFIGURATION("configuration"),
    FILTER("filter"),
    INTERCEPTOR("interceptor"),
    EXCEPTION("exception"),
    ENTITY("entity"),
    UTIL("util"),
    DTO("dto"),
    OTHER("other");

    private final String name;

    FileRole(String name){
        this.name = name;
    }

    public static FileRole from(String role){
        return switch (role){
            case "controller" -> CONTROLLER;
            case "service" -> SERVICE;
            case "repository" -> REPOSITORY;
            case "configuration" -> CONFIGURATION;
            case "filter" -> FILTER;
            case "interceptor" -> INTERCEPTOR;
            case "exception" -> EXCEPTION;
            case "entity" -> ENTITY;
            case "dto" -> DTO;
            case "util" -> UTIL;
            default -> OTHER;
        };
    }
}
