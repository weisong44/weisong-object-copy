package com.weisong.common.vodo.converter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class HandlerList extends AbstractCollectionHandler<List> {
    @Override
    protected List newInstance() {
        return new ArrayList(5);
    }
}
