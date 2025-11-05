package com.team_5_back_repository.project.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RsData(
        String resultCode,
        @JsonIgnore
        int statusCode,
        String msg) {
    public RsData(String resultCode, String msg) {
        this(resultCode, Integer.parseInt(resultCode.split("-",2)[0]), msg);
    }
}