package com.weisong.common.vodo;


abstract public class WithVocoDisabled extends WithVocoLevel {
    
    public WithVocoDisabled(VoDoUtil voDoUtil) throws Exception {
        super(voDoUtil, Integer.MAX_VALUE);
    }

}
