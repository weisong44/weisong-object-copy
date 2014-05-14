package com.weisong.common.data;

import org.springframework.data.jpa.repository.JpaRepository;

public class ModelMetadata {
    public <DO> JpaRepository<DO, Long> getRepository(Class<DO> clazz) {
        return null;
    }
}
