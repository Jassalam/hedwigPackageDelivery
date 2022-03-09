package net.sharksystem.hedwig;

import java.io.IOException;

public interface HedwigMessageChannel {
   HedwigCommunicationAge getAge();

    void setAge(HedwigCommunicationAge channelAge);

    /**
     * Return the URI of this channel.
     *
     * @return
     */
    CharSequence getURI() throws IOException;

    CharSequence getName() throws IOException;

    boolean isStoneAge();

    boolean isBronzeAge();

    boolean isInternetAge();

    /**
     * Produce a list of messages in this channel.
     *
     * @param sentMessagesOnly true: only messages sent by this peer; false: also received messages
     * @param ordered          true: messages are sorted by a timestamp (note1: sorting is useless when using
     *                         sentMessagesOnly == true. They are already ordered. note2: timestamps are produced from
     *                         distributed  and not synchronized clocks. It is not safe)
     * @return
     */
    HedwigMessageList getMessages(boolean sentMessagesOnly, boolean ordered) throws HedwigMessangerException, IOException;

    /**
     * Return a list of all messages (sent and received) ordered by timestamp. For comments of time stamp
     * accuracy, see comment in order parameter in the full variant of this method
     *
     * @return
     * @see #getMessages(boolean, boolean)
     */
    HedwigMessageList getMessages() throws HedwigMessangerException, IOException;
}
