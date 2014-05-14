package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.StringUtils;

import com.weisong.common.data.DataObject;
import com.weisong.common.util.CommonUtil;
import com.weisong.common.util.ReflectionUtil;

public class HandlerFetchHiveObjectById extends AbstractAttrHandler {

    @Override
    boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        throw new RuntimeException("Should never reach here!");
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    boolean voToDo(VoToDoContext ctx) throws Exception {
        if(ctx.converterVoToDo.hasVocoField(ctx.voField) || ctx.voFieldValue == null) {
            return false;
        }
        if (Long.class != ctx.voField.getType()) {
            warnAttrAction(ctx.o.getClass(), ctx.doFieldName, "only Long supported, skip");
            return false;
        }
        if(ctx.doFieldName.endsWith(".id") == false || StringUtils.countOccurrencesOf(ctx.doFieldName, ".") != 1) {
            warnAttrAction(ctx.o.getClass(), ctx.doFieldName, "not supported, skip");
            return false;
        }
        String realDoFieldName = ctx.doFieldName.replace(".id", "");
        Field doField = ReflectionUtil.getField(ctx.o.getClass(), realDoFieldName);
        if (DataObject.class.isAssignableFrom(doField.getType()) == false) {
            warnAttrAction(ctx.o.getClass(), ctx.doFieldName, "target type is not HiveObject, skip");
            return false;
        }
        debugAttrAction(ctx.o.getClass(), ctx.doFieldName, "type is Long and mapped to DO attr, retrieve from database");
        JpaRepository repo = ctx.metadata.getRepository(doField.getType());
        Object realDoFieldValue = CommonUtil.unboxProxy(repo.findOne((Long) ctx.voFieldValue));
        ReflectionUtil.setFieldValue(ctx.o, doField, realDoFieldValue);
        return true;
    }

}
