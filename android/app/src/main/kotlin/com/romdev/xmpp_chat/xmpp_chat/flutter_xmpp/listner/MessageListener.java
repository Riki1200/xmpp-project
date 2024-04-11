package com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.listner;

import android.content.Context;

import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Utils.Utils;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;


public class MessageListener implements StanzaListener {

    private static Context mApplicationContext;

    public MessageListener(Context context) {
        mApplicationContext = context;
    }

    @Override
    public void processStanza(Stanza packet) {

        Message message = (Message) packet;
        Utils.broadcastMessageToFlutter(mApplicationContext, message);
    }
}
