package com.weisong.common.data;

public interface DataObject<ID extends Comparable<ID>> {
    ID getId();
    void setId(ID id);
}
