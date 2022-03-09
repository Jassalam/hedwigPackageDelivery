package net.sharksystem.hedwig;

import java.io.IOException;
import java.util.Set;

public interface HedwigMessageClosedChannel extends HedwigMessageChannel {

    /**
     * @param peerID
     * @throws HedwigMessangerException messengers would not be allowed on Stone age.
     */
    void addTrustedMessenger(CharSequence peerID) throws HedwigMessangerException;

    void removeTrustedMessenger(CharSequence peerID) throws HedwigMessangerException;

    Set<CharSequence> getTrustedMessengers(CharSequence peerID) throws HedwigMessangerException;

    /**
     * That send message method variant with all options. Other variants are just convenience versions of this
     * one.
     *
     * @param content  Content
     * @param receiver End-to-End receiver (can be null)
     * @param sign     message is to be signed or not
     * @param encrypt  message is to be encrypted - a receiver must be specified if set true
     * @throws HedwigMessangerException some logical problems like: no receiver but encryption wanted...
     * @throws IOException             problems with sending message. It is mainly caused by problems with memory, e.g. SDCard.
     *                                 It does <i>not</i> indicate any network problems, though. ASAP is an opportunistic protocol. It will look and
     *                                 wait for chances to transmit that message - as soon as possible.
     */
    void sendSharkMessage(byte[] content, CharSequence receiver, boolean sign, boolean encrypt)
        throws HedwigMessangerException, IOException;

    /**
     * Sends an unencrypted and unsigned message into this channel.
     *
     * @param content
     * @throws HedwigMessangerException
     * @throws IOException
     */
    void sendSharkMessage(byte[] content) throws HedwigMessangerException, IOException;
}
