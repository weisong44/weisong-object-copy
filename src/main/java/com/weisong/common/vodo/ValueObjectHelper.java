package com.weisong.common.vodo;

/**
 * This class is meant to override default behavior of VoDoUtil,
 * simply define a class called XyzVOHelper in exactly the same package
 * as the VO class with "Helper" appended to the VO class name.
 * At the end of processing, VoDoUtil will instantiate this class
 * and invoke one of the methods accordingly.
 */
public interface ValueObjectHelper<DO, VO> {
    DO toDo(VO vo, DO o) throws Exception;
    VO toVo(VO vo, Object... dos) throws Exception;
}
