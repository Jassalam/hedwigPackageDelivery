package net.sharksystem.hedwig;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

@ASAPFormats(formats = {HedwigComponent.APP_FORMAT})
public interface HedwigComponent extends SharkComponent {
    String CHANNEL_DEFAULT_NAME = "channel has no name";

    String APP_FORMAT = "application/x-hedwigTransport";
    String URI = "hedwig://transporter";

    // behaviour flags
    String HEDWIG_MESSENGER_STONE_AGE_MODE = "net.sharksystem.messenger_stone_age";
    String HEDWIG_MESSENGER_BRONZE_AGE_MODE = "net.sharksystem.messenger_bronze_age";
    String HEDWIG_MESSENGER_INTERNET_AGE_MODE = "net.sharksystem.messenger_internet_age";
    String DEFAULT_AGE = HEDWIG_MESSENGER_BRONZE_AGE_MODE;

    /**
     * List of all users online and their data
     */
    Set<User> users = new HashSet<>();

    Hashtable<String, List<String>> messages = new Hashtable();

    /**
     * List of all Hedwigs
     */
    Set<String> hedwigs = new HashSet();

    /**
     * Send a shark message. Recipients can be empty (null). This message is sent to anybody.
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
     *                                 encrypted flag set
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
     * Remove a new channel.
     *
     * @param uri Channel identifier
     * @throws IOException
     * @throws HedwigMessangerException unknown channel uri
     * @since 1.1
     */
    void removeChannel(CharSequence uri) throws IOException, HedwigMessangerException;

    /**
     * Remove all channels - be careful.
     *
     * @throws IOException
     * @since 1.1
     */
    void removeAllChannels() throws IOException;

    /**
     * Set communication behaviour (stone, bronze, internet age) for a channel.
     *
     * @param uri       channel uri
     * @param behaviour behaviour
     * @throws SharkUnknownBehaviourException unknown communication behaviour
     * @throws HedwigMessangerException        unknown channel uri
     * @since 1.1
     */
    void setChannelBehaviour(CharSequence uri, String behaviour)
        throws SharkUnknownBehaviourException, HedwigMessangerException;

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

    /**
     * Get a collection of messages of a channel.
     * @param uri
     * @return
     * @throws SharkMessengerException no such channel
     * @throws IOException problems when reading
     */
//    Collection<SharkMessage> getSharkMessages(CharSequence uri) throws SharkMessengerException, IOException;

    /**
     * @param listener
     * @since 1.0
     */
    void addHedwigMessagesReceivedListener(HedwigMessageReceivedListener listener);

    /**
     * @param listener
     * @since 1.0
     */
    void removeHedwigMessagesReceivedListener(HedwigMessageReceivedListener listener);

    SharkPKIComponent getSharkPKI();

    /**
     * connection of Bluetooth
     * on startup Bluetooth will be set On and try to make connection
     * on success, connection through a message  which show that Devices are connected
     */

    void startBluetooth();
    /**
     * !! should be done automatically now  !!
     * connect Peers and exchange ,store Certificate
     * connecting two Peers exchange certificate with basic information like Name, ID, and signature
     * and store in ASAP storage
     */


    /**
     * net.sharksystem.hedwig.Hedwig registration
     * on startup Peer type net.sharksystem.hedwig.Hedwig will send a message to all peers to inform his identity as net.sharksystem.hedwig.Hedwig
     * this will add net.sharksystem.hedwig.Hedwig to Hedwigs list in app,
     * this way we can select net.sharksystem.hedwig.Hedwig and give him commands later on.
     * net.sharksystem.hedwig.Hedwig registration message will be received by HedwigRegistrationListener.
     * Message listener will add hedwig to HedwigPeer list.
     */
    void sendMessageHedwigRegistrationIfOwnerAHedwig(CharSequence ownerId) throws IOException, HedwigMessangerException;

    /**
     * User plans to send some Package to another User
     * makes an Offer to send Package
     * User responds with his Location
     *
     * @param peerId  id of the user whom he is sending message
     * @param message a custom message from User
     */
    void makeOfferToSendSomePackageToPeer(String peerId, String message) throws HedwigMessangerException, IOException;

    /**
     * location exchange of app users
     * GPS location of Sender and Receiver is exchanged and store with sender
     * on startup automatically message is send with location as soon as user start app.
     * A specific message listener will be implemented which update the location
     * of specific peer which will be shown in app
     */
    void sendUserLocation(String longitude, String latitude) throws IOException, HedwigMessangerException;

    /**
     * call net.sharksystem.hedwig.Hedwig
     * when the user want to send a package he will call net.sharksystem.hedwig.Hedwig to his location,
     * This call will be implemented with IOT.
     */
    void callHedwig(CharSequence hedwigID, String longitude, String latitude) throws IOException, HedwigMessangerException;

    /**
     * send hedwig with package to receiver location
     * Sender provide package, package details which are encrypted and signed, and message for receiver
     * location of receiver,package weight and certificate to net.sharksystem.hedwig.Hedwig
     * SendPackageMessageListener in hedwig will receive and will fly to GPS location of Receiver.
     */
    void sendPackageToUserLocation(String message, CharSequence receiver) throws IOException, HedwigMessangerException;

    /**
     * Confirmation Message
     * After receiving the package Receiver will send a confirmation message to sender
     * by confirmationMessageListener which inculde the message and stop the thread there.
     * when receiver decrypted the message
     * successfully hedwig will drop the package with Receiver
     */
    void sendRecieverConfirmationAndDropPackage(String reciever) throws IOException, HedwigMessangerException;
}