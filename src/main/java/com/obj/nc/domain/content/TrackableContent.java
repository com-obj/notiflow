package com.obj.nc.domain.content;

public abstract class TrackableContent extends MessageContent {
    
    public abstract boolean hasHtmlText();
    
    public abstract String getHtmlText();
    
    public abstract void setHtmlText(String text);
    
}
