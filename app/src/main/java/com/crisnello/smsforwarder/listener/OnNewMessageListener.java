package com.crisnello.smsforwarder.listener;

public interface OnNewMessageListener {
    void onNewMessageReceived(String from, String msg);
}
