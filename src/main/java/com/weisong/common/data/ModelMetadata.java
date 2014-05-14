package com.weisong.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class ModelMetadata {
    public <DO> JpaRepository<DO, Long> getRepository(Class<DO> clazz) {
        return null;
    }
}
