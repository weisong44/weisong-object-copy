package com.weisong.common.vodo;


abstract public class WithVocoLevel {
    
    final protected VoDoUtil voDoUtil;
    final protected int vocoLevel;
    
    abstract public void doExecute() throws Exception;
    
    public WithVocoLevel(VoDoUtil voDoUtil, int vocoLevel) throws Exception {
        this.voDoUtil = voDoUtil;
        this.vocoLevel = vocoLevel;
        execute();
    }
    
    private void execute() throws Exception {
        VoDoUtil.Settings settings = voDoUtil.getPerThreadSettings();
        int oldValue = settings.getVocoLevel();
        try {
            settings.setVocoLevel(vocoLevel);
            doExecute();
        }
        finally {
            settings.setVocoLevel(oldValue);
        }
    }
}
