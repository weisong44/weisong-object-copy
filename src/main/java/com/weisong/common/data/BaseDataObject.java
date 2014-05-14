package com.weisong.common.data;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BaseDataObject implements DataObject<Long> {
    private Long id;
    private Long createdAt, updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = System.currentTimeMillis();
        }
    }
}
