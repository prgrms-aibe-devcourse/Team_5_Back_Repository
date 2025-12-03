package com.team_5_back_repository.project.global.redis;

import com.team_5_back_repository.project.global.rsData.RsData;

public class UnAuthenticationException extends RuntimeException {
    private final String resultCode;
    private final String msg;

    public UnAuthenticationException(String resultCode, String msg) {
        super(resultCode + " : " + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public RsData<Void> getRsData() {
        return new RsData<>(resultCode, msg, null);
    }
}