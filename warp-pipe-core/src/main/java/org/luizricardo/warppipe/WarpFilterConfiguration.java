package org.luizricardo.warppipe;


import java.nio.charset.Charset;

public class WarpFilterConfiguration {

    private final Charset charset;
    private final boolean flushAfterHead;
    private final boolean autoExecuteBeforeClosingBody;
    private final boolean autoDetectPlaceholders;

    public WarpFilterConfiguration(final Charset charset,
                                   final boolean flushAfterHead,
                                   final boolean autoExecuteBeforeClosingBody,
                                   final boolean autoDetectPlaceholders) {
        this.charset = charset;
        this.flushAfterHead = flushAfterHead;
        this.autoExecuteBeforeClosingBody = autoExecuteBeforeClosingBody;
        this.autoDetectPlaceholders = autoDetectPlaceholders;
    }

    public Charset charset() {
        return charset;
    }

    public boolean flushAfterHead() {
        return flushAfterHead;
    }

    public boolean autoExecuteBeforeClosingBody() {
        return autoExecuteBeforeClosingBody;
    }

    public boolean autoDetectPlaceholders() {
        return autoDetectPlaceholders;
    }
}
