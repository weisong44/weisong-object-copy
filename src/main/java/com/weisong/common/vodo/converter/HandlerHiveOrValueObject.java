package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;

import com.weisong.common.data.DataObject;
import com.weisong.common.util.CommonUtil;
import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.value.ValueObject;
import com.weisong.common.vodo.VoDoUtil;

public class HandlerHiveOrValueObject extends AbstractAttrHandler {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        if(ValueObject.class.isAssignableFrom(ctx.voField.getType()) == false) {
            return false;
        }
        
        Assert.notNull(ctx.doFieldValue);
        
        boolean inVocoMode = ctx.converterDoToVo.isInVocoMode(curVocoLevel, ctx); 
        Field vocoField = ctx.converterDoToVo.getVocoField(ctx.voField);
        if(inVocoMode && vocoField != null) {
            debugAttrAction(ctx.vo.getClass(), ctx.voField, String.format(
                    "in voco mode (%s), retrieve and set id", vocoField.getName()));
            Long id = (Long) ReflectionUtil.getFieldValue(ctx.doFieldValue, "id");
            // Set VOCO field and clear out original field
            ReflectionUtil.setFieldValue(ctx.vo, vocoField, id);
            ReflectionUtil.setFieldValue(ctx.vo, ctx.voField, null);
        }
        else {
            debugAttrAction(ctx.vo.getClass(), ctx.voField, "set recursively");
            Class<?> voClass = VoDoUtil.getDefaultVoClass(ctx.doFieldValue.getClass());
            if(voClass == null) {
                voClass = ctx.voField.getType();
            }
            // Set original field and clear out VOCO field
            Object v = ctx.converterDoToVo.toVo(curVocoLevel + 1, voClass, (Map) ctx.voClassMapping, ctx.doFieldValue);
            ctx.voField.set(ctx.vo, v);
            if(vocoField != null) {
                ReflectionUtil.setFieldValue(ctx.vo, vocoField, null);
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    boolean voToDo(VoToDoContext ctx) throws Exception {
        if(DataObject.class.isAssignableFrom(ctx.doField.getType()) == false) {
            return false;
        }
        
        Field vocoField = ctx.converterVoToDo.getVocoField(ctx.voField);
        if(vocoField != null) {
            Long vocoFieldValue = (Long) ReflectionUtil.getFieldValue(ctx.vo, vocoField.getName());
            if(vocoFieldValue != null) {
                if(ctx.voFieldValue != null) {
                    throw new Exception(String.format("both VO field %s and VOCO field %s are not null", 
                            ctx.voField.getName(), vocoField.getName()));
                }
                debugAttrAction(ctx.o.getClass(), ctx.doField, String.format(
                        "VO field %s is null, retrieve using VOCO field %s",
                        ctx.voField.getName(), vocoField.getName()));
                
                JpaRepository repo = ctx.metadata.getRepository(ctx.doField.getType());
                Object o = CommonUtil.unboxProxy(repo.findOne(vocoFieldValue));
                ctx.doField.set(ctx.o, o);
                return true;
            }
            else {
                Assert.notNull(ctx.voFieldValue);
                debugAttrAction(ctx.o.getClass(), ctx.doField, String.format(
                        "VOCO field %s is null, set recursively using VO field %s",
                        vocoField.getName(), ctx.voField.getName()));
                ctx.doField.set(ctx.o, ctx.converterVoToDo.toDo(ctx.voFieldValue));
                return true;
            }
        }
        else {
            Assert.notNull(ctx.voFieldValue);
            debugAttrAction(ctx.o.getClass(), ctx.doField, "set recursively");
            ctx.doField.set(ctx.o, ctx.converterVoToDo.toDo(ctx.voFieldValue));
            return true;
        }
    }

}
