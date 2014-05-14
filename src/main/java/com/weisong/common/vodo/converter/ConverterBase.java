package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.vodo.DefaultValueObjectHelper;
import com.weisong.common.vodo.ValueObjectHelper;
import com.weisong.common.vodo.VoDoUtil;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.ContentOptimization;

class ConverterBase {

    final static protected Exception HandlerNotAvailableException = new Exception("handler not available");

    final static private DefaultValueObjectHelper<?, ?> noHelper = new DefaultValueObjectHelper<>();

    final static private Field noVocoField = ReflectionUtil.getField(ConverterDoToVo.class, "vocoFieldMap");
    final static private Map<Field, Field> vocoFieldMap = new HashMap<>(10);

    final static private boolean strictErrorChecking = System.getProperty("vodo.strict.error.checking") != null;

    final protected Logger logger;
    final protected VoDoUtil voDoUtil;
    final protected ApplicationContext ctx;
    final protected AbstractAttrHandler[] handlers;

    /** Local cache for known value object helpers */
    private final Map<Class<?>, ValueObjectHelper<?, ?>> helperMap = new ConcurrentHashMap<>(100);

    ConverterBase(VoDoUtil vodoUtil, ApplicationContext ctx) {
        this.ctx = ctx;
        this.voDoUtil = vodoUtil;
        this.logger = LoggerFactory.getLogger(getClass());
        this.handlers = new AbstractAttrHandler[] { new HandlerNullValue(), new HandlerList(), new HandlerSet(),
                new HandlerMap(), new HandlerSingleElementCompatibleType(), new HandlerHiveOrValueObject(),
                new HandlerEnum(), new HandlerByteArray(), new HandlerSpecial() };
    }

    @SuppressWarnings("unchecked")
    protected <DO, VO> ValueObjectHelper<DO, VO> getVoHelper(Class<VO> voClass) {
        ValueObjectHelper<DO, VO> helper = (ValueObjectHelper<DO, VO>) helperMap.get(voClass);
        if (helper != null) {
            return helper != noHelper ? helper : null;
        }

        String helperClassName = voClass.getName() + "Helper";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> helperClass = loader.loadClass(helperClassName);
            helper = (ValueObjectHelper<DO, VO>) ctx.getAutowireCapableBeanFactory().autowire(helperClass,
                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
            helperMap.put(voClass, helper);
            return helper;
        }
        catch (Exception e) {
            helperMap.put(voClass, noHelper);
            return null;
        }
    }

    public boolean hasVocoField(Field field) {
        return getVocoField(field) != null;
    }

    synchronized public Field getVocoField(Field voField) {
        Field vocoField = vocoFieldMap.get(voField);
        if (vocoField == null) {
            Class<?> voClass = voField.getDeclaringClass();
            for (Field f : voClass.getDeclaredFields()) {
                ContentOptimization anno = f.getAnnotation(ContentOptimization.class);
                if (anno == null) {
                    continue;
                }

                String doFieldName = voField.getName();
                BindToField bf = voField.getAnnotation(BindToField.class);
                if (bf != null) {
                    if (bf.clazz().isEmpty() == false) {
                        continue; // TODO: Can't handle this case yet
                    }
                    doFieldName = bf.value();
                    if (doFieldName.contains(".")) {
                        continue; // TODO: Can't handle this case yet
                    }
                }
                if (anno.value().equals(doFieldName) == false) {
                    continue;
                }
                vocoField = f;
                break;
            }

            if (vocoField == null) {
                vocoField = noVocoField;
            }
            vocoFieldMap.put(voField, vocoField);
            if (logger.isDebugEnabled()) {
                String targetFieldName = vocoField == noVocoField ? "None" : vocoField.getName();
                logger.debug(String.format("Added VOCO field mapping: %s.%s -> %s.%s", voField.getDeclaringClass()
                        .getSimpleName(), voField.getName(), vocoField.getDeclaringClass().getSimpleName(),
                        targetFieldName));
            }
        }
        return vocoField == noVocoField ? null : vocoField;
    }

    protected void debugAttrAction(Class<?> c, Field f, String action) {
        debugAttrAction(c, f.getName(), action);
    }

    protected void debugAttrAction(Class<?> c, String fieldName, String action) {
        logger.debug(String.format("  %s.%s: %s", c.getSimpleName(), fieldName, action));
    }

    protected void warnAttrAction(Class<?> c, Field f, String action) {
        warnAttrAction(c, f.getName(), action);
    }

    protected void warnAttrAction(Class<?> c, String fieldName, String action) {
        logger.warn(String.format("  %s.%s: %s", c.getSimpleName(), fieldName, action));
    }

    protected void handleAttrConversionError(Exception ex) {
        if (strictErrorChecking) {
            throw new RuntimeException(ex);
        }
    }
}
