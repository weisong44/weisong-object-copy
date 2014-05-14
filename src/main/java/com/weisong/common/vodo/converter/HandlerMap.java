package com.weisong.common.vodo.converter;

import java.util.Map;

import org.springframework.util.Assert;

public class HandlerMap extends AbstractAttrHandler {

    @Override
    boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {

        Assert.notNull(ctx.doFieldValue);

        if(ctx.voField.getType().isAssignableFrom(ctx.doFieldValue.getClass()) == false ||
           ctx.doFieldValue instanceof Map == false) {
            return false;
        }
        
        throw NotImplementedException;

    }

    @Override
    boolean voToDo(VoToDoContext ctx) throws Exception {
        
        if(ctx.converterVoToDo.hasVocoField(ctx.voField)) {
            return false;
        }
        
        Assert.notNull(ctx.voFieldValue);
        
        if(ctx.doField.getType().isAssignableFrom(ctx.voFieldValue.getClass()) == false ||
           ctx.voFieldValue instanceof Map == false) {
             return false;
        }
        
        throw NotImplementedException;

    }

}
