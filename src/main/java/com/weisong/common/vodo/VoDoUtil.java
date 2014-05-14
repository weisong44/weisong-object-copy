package com.weisong.common.vodo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.converter.ConverterDoToVo;
import com.weisong.common.vodo.converter.ConverterVoToDo;

@Component
final public class VoDoUtil implements ApplicationContextAware, InitializingBean {

    final static public String VOCO_DISABLED = "vocoDisabled";
    final static public String VOCO_LEVEL = "vocoLevel";
    final static public int VOCO_DEFAULT_LEVEL = 2;
    final static private int VOCO_INITIAL_LEVEL = 1;
    final static private String DO_BASE_PACKAGE = "com.weisong.data";
    final static private String VO_BASE_PACKAGE = "com.weisong.value";

    private static final Logger logger = LoggerFactory.getLogger(VoDoUtil.class);
    
    static public Class<?> getDoClass(Class<?> voClass) throws ClassNotFoundException, IllegalArgumentException {
        BindToClass bc = voClass.getAnnotation(BindToClass.class);
        String doClassName = null;;
        
        try {
            if (bc != null) {
                doClassName = bc.value();
            }
            else {
                doClassName = voNameToDoName(voClass.getName());
                logger.trace("VO {} mapped to DO {}", voClass.getName(), doClassName);
            }
            
            Class<?> retval = Thread.currentThread().getContextClassLoader().loadClass(doClassName);
            return retval;
        }
        catch(ClassNotFoundException | IllegalArgumentException e) {
            logger.error("failed to find DO class for VO class {}: {}", voClass.getName(), e.getMessage());
            throw e;
        }
        catch (Exception e) {
            logger.error("failed to load DO class {} for VO class {}: {}",
                    doClassName, voClass.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to get VO class using relative package structure convention.
     * Note: keep in mind that a DO could be mapped to multiple VO's, and this method
     * only tries to get the VO that's mapped to the DO 1:1.
     */
    static public Class<?> getDefaultVoClass(Class<?> doClass) {
        String voClassName = doNameToVoName(doClass.getName());
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(voClassName);
            logger.trace("DO {} mapped to default VO {}", doClass.getName(), voClassName);
            return clazz;
        }
        catch (Exception e) {
            logger.warn("failed to load default VO class {} for DO class {}: {}",
                    voClassName, doClass.getName(), e.getMessage());
            return null;
        }
    }

    static private String voNameToDoName(String voName) {
        Assert.isTrue(voName.startsWith(VO_BASE_PACKAGE));

        String[] packages = voName.substring(VO_BASE_PACKAGE.length() + 1).split("\\.");

        Assert.isTrue(packages.length > 1);
        Assert.isTrue("vo".equals(packages[packages.length - 2]));

        StringBuilder doNameBuilder = new StringBuilder(DO_BASE_PACKAGE);

        for (int i = 0; i < packages.length - 2; i++) {
            doNameBuilder.append('.').append(packages[i]);
        }

        String voSimpleName = packages[packages.length - 1];
        doNameBuilder.append('.').append(voSimpleName.substring(0, voSimpleName.length() - 2));

        return doNameBuilder.toString();
    }

    static private String doNameToVoName(String doName) {
        if(doName.contains(VoDoUtil.class.getPackage().getName()) == false) {
            Assert.isTrue(doName.startsWith(DO_BASE_PACKAGE));
        }
        return doName.replace(DO_BASE_PACKAGE, VO_BASE_PACKAGE).replace(".model.", ".model.vo.") + "Vo";
    }

    @Getter @Setter @ToString
    final static public class Settings {
        private int vocoLevel = VOCO_DEFAULT_LEVEL;
    }
    
    final private ThreadLocal<Settings> perThreadSettings = new ThreadLocal<Settings>() {
        @Override
        protected Settings initialValue() {
            return new Settings();
        }
    };

    private ApplicationContext ctx;
    private ConverterVoToDo voToDo;
    private ConverterDoToVo doToVo;
    
    public Object toDo(Object vo) throws Exception {
        return voToDo.toDo(vo);
    }
    
    public <VO> List<VO> toDoList(List<?> voList) throws Exception {
        return voToDo.toDoList(voList);
    }
    
    public <VO> VO toVo(Class<VO> voClass, Object... dos) throws Exception {
        return doToVo.toVo(VOCO_INITIAL_LEVEL, voClass, dos);
    }

    public <VO> VO toVo(Map<Class<?>, Class<?>> voClassMapping, Object... dos) throws Exception {
        return doToVo.toVo(VOCO_INITIAL_LEVEL, voClassMapping, dos);
    }
    
    public <VO> ArrayList<VO> toVoList(Class<VO> voClass, List<?> doList) throws Exception {
        return doToVo.toVoList(VOCO_INITIAL_LEVEL, voClass, doList);
    }
    
    public <VO> Page<VO> toVoPage(Class<VO> voClass, Page<?> doPage) throws Exception {
        return doToVo.toVoPage(VOCO_INITIAL_LEVEL, voClass, doPage);
    }

    public <VO> Page<VO> toVoPage(Map<Class<?>, Class<?>> voClassMapping, Page<?> doPage) throws Exception {
        return doToVo.toVoPage(VOCO_INITIAL_LEVEL, voClassMapping, doPage);
    }
    
    public <VO> ArrayList<VO> toVoList(Map<Class<?>, Class<?>> voClassMapping, List<?> doList) 
            throws Exception {
        return doToVo.toVoList(VOCO_INITIAL_LEVEL, voClassMapping, doList);
    }

    public Settings getPerThreadSettings() {
        return perThreadSettings.get();
    }
    
    @SuppressWarnings("unchecked")
    public static boolean shouldSkipVoDoConversion(Field field, Class<? extends Annotation> ... annoClasses) {
        
        // Special handling
        if("jsonType".equals(field.getName())) {
            return true;
        }
        
        // Check modifier
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
            return true;
        }

        // Check annotation
        for(Class<? extends Annotation> annoClass : annoClasses) {
            Annotation anno = field.getAnnotation(annoClass);
            if (anno != null) {
                logger.debug(String.format("  %s.%s: %s", field.getDeclaringClass().getSimpleName(), field.getName(),
                        String.format("annotated with @%s, skip", annoClass.getSimpleName())));
                return true;
            }
        }

        return false;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        voToDo = new ConverterVoToDo(this, ctx);
        doToVo = new ConverterDoToVo(this, ctx);
    }

}
