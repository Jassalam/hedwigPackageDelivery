package net.sharksystem.hedwig;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@ASAPFormats(formats = {HedwigComponent.APP_FORMAT})
public interface HedwigComponent extends SharkComponent {
    String CHANNEL_DEFAULT_NAME = "channel has no name";

    String APP_FORMAT = "application/x-hedwigTransport";
    String URI = "hedwig://transporter";


    /**
     * Send a hedwig message. Recipients can be empty (null). This message is sent to anybody.
     * End-to-end security is supported. This message is encrypted for any recipient in a non-empty
     * recipient list if flag <i>encrypted</i> is set. Message to with an empty recipient list cannot be
     * encrypted. This message would throw an exception.
     * <br/>
     * <br/>
     * This message is signed if the signed flag is set.
     *
     * @param content  Arbitrary content
     * @param uri      channel uri
     * @param receiver recipient list - can be null
     * @param sign     message will be signed yes / no
     * @param encrypt  message will be encrypted for recipient(s) yes / no. A message with multiple
     *                 receiver is sent a multiple copies, each encrypted with receiver' public key.
     * @throws HedwigMessangerException no all certificates available to encrypt. Empty receiver list but
     *                                  encrypted flag set
     * @since 1.0
     */
    void sendHedwigMessage(byte[] content, CharSequence uri, Set<CharSequence> receiver,
                           boolean sign, boolean encrypt) throws HedwigMessangerException, IOException;

    /**
     * Variant. Just a single receiver
     *
     * @see #sendHedwigMessage(byte[], CharSequence, CharSequence, boolean, boolean)
     * @since 1.0
     */
    void sendHedwigMessage(byte[] content, CharSequence uri, CharSequence receiver,
                           boolean sign, boolean encrypt) throws HedwigMessangerException, IOException;

    /**
     * Variant. No receiver specified - send to anybody
     *
     * @see #sendHedwigMessage(byte[], CharSequence, CharSequence, boolean, boolean)
     * @since 1.0
     */
    void sendHedwigMessage(byte[] content, CharSequence uri, boolean sign, boolean encrypt)
        throws HedwigMessangerException, IOException;

    /**
     * Create a new channel.
     *
     * @param uri  Channel identifier
     * @param name Channel (human readable) name
     * @throws IOException
     * @throws HedwigMessangerException channel already exists
     * @since 1.1
     */
    HedwigMessageClosedChannel createClosedChannel(CharSequence uri, CharSequence name)
        throws IOException, HedwigMessangerException;

    /**
     * Produces an object reference to a messenger channel with specified uri - throws an exception otherwise
     *
     * @param uri
     * @return
     * @throws HedwigMessangerException channel does not exist
     */
    HedwigMessageChannel getChannel(CharSequence uri) throws HedwigMessangerException, IOException;

    /**
     * Android support - give channels a number starting with 0
     *
     * @param position
     * @return
     * @throws HedwigMessangerException
     * @throws IOException
     */
    HedwigMessageChannel getChannel(int position) throws HedwigMessangerException, IOException;

    /**
     * Create a new channel.
     *
     * @param uri          channel uri
     * @param name         user friendly name
     * @param mustNotExist if true - an exception is thrown if a channel with this uri already exists
     * @return
     * @throws HedwigMessangerException
     * @throws IOException
     */
    HedwigMessageChannel createChannel(CharSequence uri, CharSequence name, boolean mustNotExist)
        throws HedwigMessangerException, IOException;

    /**
     * Create a new channel. No other channel with this uri already exists
     *
     * @param uri
     * @param name
     * @return
     * @throws HedwigMessangerException
     * @throws IOException
     */
    HedwigMessageChannel createChannel(CharSequence uri, CharSequence name)
        throws HedwigMessangerException, IOException;

    /**
     * Produces a list of active channel uris
     *
     * @return
     * @throws IOException
     * @since 1.1
     */
    List<CharSequence> getChannelUris() throws IOException, HedwigMessangerException;


    SharkPKIComponent getSharkPKI();

    /**
     * connection of Bluetooth
     * on startup Bluetooth will be set On and try to make connection
     * on success, connection through a message  which show that Devices are connected
     */

    void startBluetooth();

    /**
     * all Peers whether online or offline.
     */
    Set<CharSequence> getAllPeers();


    /**
     * User plans to send some Package to another User
     * makes an Offer to send Package
     * User responds with his Location
     *
     * @param peerId  id of the user whom he is sending message
     * @param message a custom message from User
     */
    void makeOfferToSendSomePackageToPeer(String peerId, String message) throws HedwigMessangerException, IOException;

    /*
     * accept the offer of Package delivery
     * send credential message back to the sender
     */
    void acceptOfferFromPeer(CharSequence peerId, String offerId) throws HedwigMessangerException, IOException, ASAPException;
    
    /*
     * Set of all Subjects where acceptance of Credential Messages is Pending.
     */
    Set<String> getAllPendingCredentialAcceptanceSubjects();

    /**
     * Accept credential message from Subject
     */
    void acceptCredentialMessageBySubject(String subject) throws ASAPSecurityException, IOException;

    /**
     * send hedwig with package to receiver location
     * Sender provide message for receiver signed and encrypted
     * Hedwigs in between sender and reciever could not decrypt message
     * as soon as reciever recieves message and decrypt it he will send a confirmation message automatically
     */
    void sendPackageToUser(String message, CharSequence receiver) throws IOException, HedwigMessangerException;
}
