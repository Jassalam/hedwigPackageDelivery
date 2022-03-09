package net.sharksystem.hedwig;

import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.HashMap;

public class HedwigMessageChannelImpl implements HedwigMessageChannel {

    private static final String KEY_NAME_HEDWIG_MESSENGER_CHANNEL_NAME = "hedwigMessengerChannelName";
    private static final String KEY_AGE_HEDWIG_MESSENGER_CHANNEL_NAME = "hedwigMessengerAge";

    private final ASAPChannel asapChannel;
    private final ASAPPeer asapPeer;
    private final SharkPKIComponent pkiComponent;
    boolean readNameFromExtraData = false;
    private CharSequence channelName;

    public HedwigMessageChannelImpl(ASAPPeer asapPeer, SharkPKIComponent pkiComponent, ASAPChannel asapChannel) {
        this.asapPeer = asapPeer;
        this.pkiComponent = pkiComponent;
        this.asapChannel = asapChannel;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          settings                                             //
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call this constructor to set up a new channel - set a name
     *
     * @param asapPeer
     * @param pkiComponent
     * @param asapChannel
     * @param channelName
     */
    public HedwigMessageChannelImpl(ASAPPeer asapPeer,
                                     SharkPKIComponent pkiComponent,
                                     ASAPChannel asapChannel,
                                     CharSequence channelName) throws IOException {

        this(asapPeer, pkiComponent, asapChannel);

        if (channelName != null) {
            asapChannel.putExtraData(KEY_NAME_HEDWIG_MESSENGER_CHANNEL_NAME, channelName.toString());
        } else {
            asapChannel.putExtraData(KEY_NAME_HEDWIG_MESSENGER_CHANNEL_NAME,
                HedwigComponent.CHANNEL_DEFAULT_NAME);
        }
    }

    @Override
    public HedwigCommunicationAge getAge() {
        return HedwigCommunicationAge.UNDEFINED;
    }

    @Override
    public void setAge(HedwigCommunicationAge channelAge) {
        Log.writeLog(this, "not yet implemented");
    }

    @Override
    public CharSequence getURI() throws IOException {
        return this.asapChannel.getUri();
    }

    public CharSequence getName() throws IOException {
        if (!readNameFromExtraData) {
            this.channelName = HedwigComponent.CHANNEL_DEFAULT_NAME; // default
            this.readNameFromExtraData = true; // remember

            // find a name
            HashMap<String, String> extraData = asapChannel.getExtraData();
            if (extraData != null) {
                String name = extraData.get(KEY_NAME_HEDWIG_MESSENGER_CHANNEL_NAME);
                if (name != null) this.channelName = name;
            }
        }

        return this.channelName;
    }

    @Override
    public boolean isStoneAge() {
        Log.writeLog(this, "not yet implemented");
        return false;
    }

    @Override
    public boolean isBronzeAge() {
        Log.writeLog(this, "not yet implemented");
        return false;
    }

    @Override
    public boolean isInternetAge() {
        Log.writeLog(this, "not yet implemented");
        return false;
    }

    @Override
    public HedwigMessageList getMessages(boolean sentMessagesOnly, boolean ordered)
        throws HedwigMessangerException, IOException {

        try {
            return new HedwigMessageListImpl(this.pkiComponent, this.asapChannel, sentMessagesOnly, ordered);
        } catch (ASAPException e) {
            throw new HedwigMessangerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public HedwigMessageList getMessages() throws HedwigMessangerException, IOException {
        return this.getMessages(false, true);
    }
}
