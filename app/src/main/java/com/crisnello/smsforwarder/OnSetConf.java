package com.crisnello.smsforwarder;

public interface OnSetConf {
    void onNewSignature(String sig);
    void onNewReply(String rep);
}
