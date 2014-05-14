package com.weisong.common.vodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyVoHelper extends DefaultValueObjectHelper<Dummy, DummyVo> {

    static private Logger logger = LoggerFactory.getLogger(DummyVoHelper.class);
    
    @Override
    public Dummy toDo(DummyVo vo, Dummy o) throws Exception {
    	o.setName("Changed by helper");
        logger.debug("Dummy.name set to new value");
        return o;
    }

    @Override
    public DummyVo toVo(DummyVo vo, Object... dos) throws Exception {
    	vo.setName("Changed by helper");
        logger.debug("DummyVo.name set to new value");
        return vo;
    }
}
