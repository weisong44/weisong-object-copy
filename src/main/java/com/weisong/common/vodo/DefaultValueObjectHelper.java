package com.weisong.common.vodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValueObjectHelper<DO, VO> implements ValueObjectHelper<DO, VO> {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public DO toDo(VO vo, DO o) throws Exception {
        return o;
    }

    @Override
    public VO toVo(VO vo, Object... dos) throws Exception {
        return vo;
    }

}
