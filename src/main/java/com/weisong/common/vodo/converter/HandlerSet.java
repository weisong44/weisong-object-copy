package com.weisong.common.vodo.converter;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class HandlerSet extends AbstractCollectionHandler<Set> {
    @Override
    protected Set newInstance() {
        return new HashSet(5);
    }
}
