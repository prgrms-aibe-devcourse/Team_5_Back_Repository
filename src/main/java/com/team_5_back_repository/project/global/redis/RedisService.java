package com.team_5_back_repository.project.global.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Integer> redisTemplate;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private int codeExpirationMillis;


    public void setCode(String email,Integer code){
        ValueOperations<String, Integer> valOperations = redisTemplate.opsForValue();
        valOperations.set(email, code, codeExpirationMillis, TimeUnit.MILLISECONDS);
    }

    public Integer getCode(String email){
        ValueOperations<String, Integer> valOperations = redisTemplate.opsForValue();
        Integer code = valOperations.get(email);
        if(code == null || code == 0){
            throw new UnAuthenticationException("400","인증코드가 만료되었습니다. 다시 시도해주세요.");
        }
        return code;
    }

    public void deleteCode(String email){
        redisTemplate.delete(email);
    }
}