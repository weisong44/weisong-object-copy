package com.weisong.common.vodo.converter;

import java.sql.Timestamp;

public class HandlerSpecial extends AbstractAttrHandler {

    @Override
    public boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        if(Timestamp.class.isAssignableFrom(ctx.doFieldType) && ctx.voField.getType() == Long.class) {
            ctx.voField.set(ctx.vo, ((Timestamp) ctx.doFieldValue).getTime());
            return true;
        }
        return false;
    }

    @Override
    public boolean voToDo(VoToDoContext ctx) throws Exception {
        if(Timestamp.class.isAssignableFrom(ctx.doField.getType()) && ctx.voField.getType() == Long.class) {
            Timestamp ts = new Timestamp((Long) ctx.voFieldValue);
            ctx.doField.set(ctx.o, ts);
            return true;
        }
        return false;
    }
}
