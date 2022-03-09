package net.sharksystem.hedwig;

public interface HedwigMessageReceivedListener {

    /**
     * New messages arrived
     *
     * @param uri channel uri
     */
    void hedwigMessagesReceived(CharSequence uri);
}
