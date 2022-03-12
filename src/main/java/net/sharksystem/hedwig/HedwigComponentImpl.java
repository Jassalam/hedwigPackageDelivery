package net.sharksystem.hedwig;

import net.sharksystem.SharkException;
import net.sharksystem.SharkNotSupportedException;
import net.sharksystem.asap.*;
import net.sharksystem.pki.CredentialMessage;
import net.sharksystem.pki.SharkCredentialReceivedListener;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.sharksystem.hedwig.AppConstants.*;

public class HedwigComponentImpl extends HedwigMessageReceivedListenerManager implements HedwigComponent,
    ASAPMessageReceivedListener, ASAPEnvironmentChangesListener, SharkCredentialReceivedListener {

    private final SharkPKIComponent sharkPKIComponent;
    public Set<CharSequence> onlinePeers = new HashSet<>();
    public Set<CharSequence> allPeers = new HashSet<>();
    public Map<String, CredentialMessage> credentialMessages = new HashMap<>();
    private ASAPPeer asapPeer;
    Hashtable<String, List<InMemoHedwigMessage>> messagesByUser = new Hashtable();
    /*
     *Package Information filled only when its a Hedwig
     *
     */
    private final String packageReciever = "";

    public HedwigComponentImpl(SharkPKIComponent sharkPKIComponent) {
        this.sharkPKIComponent = sharkPKIComponent;
    }


    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)");

        this.asapPeer.addASAPMessageReceivedListener(
            HedwigComponent.APP_FORMAT,
            this);

    }


    private void checkComponentRunning() throws HedwigMessangerException {
        if (this.asapPeer == null || this.sharkPKIComponent == null)
            throw new HedwigMessangerException("peer not started an/or pki not initialized");
    }

    @Override
    public void sendHedwigMessage(byte[] content, CharSequence uri, boolean sign,
                                  boolean encrypt) throws IOException, HedwigMessangerException {
        HashSet<CharSequence> set = new HashSet();
        this.sendHedwigMessage(content, uri, set, sign, encrypt);
    }

    @Override
    public void sendHedwigMessage(byte[] content, CharSequence uri,
                                  CharSequence receiver, boolean sign,
                                  boolean encrypt) throws IOException, HedwigMessangerException {
        HashSet<CharSequence> set = new HashSet();
        set.add(receiver);
        this.sendHedwigMessage(content, uri, set, sign, encrypt);
    }

    @Override
    public void sendHedwigMessage(byte[] content, CharSequence uri,
                                  Set<CharSequence> selectedRecipients, boolean sign,
                                  boolean encrypt)
        throws HedwigMessangerException, IOException {

        this.checkComponentRunning();

        // lets serialize and send asap messages.
        try {
            if (encrypt && selectedRecipients != null && selectedRecipients.size() > 1) {
                // more that one receiver and encrypted. Send one message for each.
                for (CharSequence receiver : selectedRecipients) {
                    this.asapPeer.sendASAPMessage(APP_FORMAT, uri,
                        // we have at most one receiver - this method can handle all combinations
                        InMemoHedwigMessage.serializeMessage(
                            content,
                            this.asapPeer.getPeerID(),
                            receiver,
                            sign, encrypt,
                            this.sharkPKIComponent));
                }
            } else {
                this.asapPeer.sendASAPMessage(APP_FORMAT, uri,
                    // we have at most one receiver - this method can handle all combinations
                    InMemoHedwigMessage.serializeMessage(
                        content,
                        this.asapPeer.getPeerID(),
                        selectedRecipients,
                        sign, encrypt,
                        this.sharkPKIComponent));
            }
        } catch (ASAPException e) {
            throw new HedwigMessangerException("when serialising and sending message: " + e.getLocalizedMessage(), e);
        }
    }


    public HedwigMessageClosedChannel createClosedChannel(CharSequence uri, CharSequence name)
        throws IOException, HedwigMessangerException {

        this.checkComponentRunning();
        throw new SharkNotSupportedException("not yet implemented");
    }

    public HedwigMessageChannel getChannel(CharSequence uri) throws HedwigMessangerException, IOException {
        try {
            ASAPStorage asapStorage =
                this.asapPeer.getASAPStorage(APP_FORMAT);

            ASAPChannel channel = asapStorage.getChannel(uri);

            return new HedwigMessageChannelImpl(this.asapPeer, this.sharkPKIComponent, channel);
        } catch (ASAPException asapException) {
            throw new HedwigMessangerException(asapException);
        }
    }

    public HedwigMessageChannel getChannel(int position) throws HedwigMessangerException, IOException {
        CharSequence uri = this.getChannelUris().get(position);
        return this.getChannel(uri);
    }

    public HedwigMessageChannel createChannel(CharSequence uri, CharSequence name)
        throws HedwigMessangerException, IOException {
        return this.createChannel(uri, name, true);
    }

    public HedwigMessageChannel createChannel(CharSequence uri, CharSequence name, boolean mustNotExist)
        throws HedwigMessangerException, IOException {

        this.checkComponentRunning();

        try {
            this.getChannel(uri); // already exists ?
            // yes exists
            if (mustNotExist) throw new HedwigMessangerException("channel already exists");
        } catch (HedwigMessangerException asapException) {
            // does not exist yet - or it is okay
        }

        // create
        try {
            ASAPStorage asapStorage =
                this.asapPeer.getASAPStorage(HedwigComponent.APP_FORMAT);

            asapStorage.createChannel(uri);
            ASAPChannel channel = asapStorage.getChannel(uri);

            return new HedwigMessageChannelImpl(this.asapPeer, this.sharkPKIComponent, channel, name);
        } catch (ASAPException asapException) {
            throw new HedwigMessangerException(asapException);
        }
    }


    @Override
    public List<CharSequence> getChannelUris() throws IOException, HedwigMessangerException {
        try {
            return this.asapPeer.getASAPStorage(APP_FORMAT).getChannelURIs();
        } catch (ASAPException asapException) {
            throw new HedwigMessangerException(asapException);
        }
    }


    @Override
    public void removeChannel(CharSequence uri) throws IOException, HedwigMessangerException {
        Log.writeLog(this, "removeChannel", "not yet implemented");
        throw new SharkNotSupportedException("not yet implemented");
    }

    @Override
    public void removeAllChannels() throws IOException {
        Log.writeLog(this, "removeAllChannels", "not yet implemented");
        throw new SharkNotSupportedException("not yet implemented");
    }

    public int size() throws IOException, HedwigMessangerException {
        try {
            ASAPStorage asapStorage =
                this.asapPeer.getASAPStorage(APP_FORMAT);

            return asapStorage.getChannelURIs().size();
        } catch (ASAPException asapException) {
            throw new HedwigMessangerException(asapException);
        }
    }

    /**
     * @Override public SharkMessage getSharkMessage(CharSequence uri, int position, boolean chronologically)
     * throws SharkMessengerException, IOException {
     * <p>
     * try {
     * ASAPStorage asapStorage =
     * this.asapPeer.getASAPStorage(HedwigComponent.APP_FORMAT);
     * <p>
     * byte[] asapMessage =
     * asapStorage.getChannel(uri).getMessages(false).getMessage(position, chronologically);
     * <p>
     * return InMemoSharkMessage.parseMessage(asapMessage, this.sharkPKIComponent);
     * <p>
     * } catch (ASAPException asapException) {
     * throw new SharkMessengerException(asapException);
     * }
     * }
     */

    @Override
    public SharkPKIComponent getSharkPKI() {
        return this.sharkPKIComponent;
    }

    @Override
    public void startBluetooth() {
        Log.writeLog(this, "Simulating Bluetooth connection started");
    }


    @Override
    public void makeOfferToSendSomePackageToPeer(String peerId, String message) throws HedwigMessangerException, IOException {
        Log.writeLog(this, "making an Offer to send some Package");
        this.sendHedwigMessage((peerId + ":" + message).getBytes(StandardCharsets.UTF_8), URI_MAKE_OFFER, Collections.singleton(peerId), false, false);
    }

    @Override
    public void sendUserLocation(String longitude, String latitude) throws IOException, HedwigMessangerException {
        Log.writeLog(this, "Sharing User Location");
        this.sendHedwigMessage((longitude + ", " + latitude).getBytes(StandardCharsets.UTF_8), URI_USER_GPS, false, false);
    }

    @Override
    public void sendPackageToUser(String message, CharSequence receiver) throws IOException, HedwigMessangerException {
        this.sendHedwigMessage(message.getBytes(StandardCharsets.UTF_8), URI_SEND_DELIVERY_PACKAGE, receiver, true, true);
    }

    @Override
    public Set<CharSequence> getAllPeers() {
        return this.allPeers;
    }

    @Override
    public void acceptOfferFromPeer(CharSequence peerId, String offerId) throws HedwigMessangerException, IOException, ASAPException {
        Log.writeLog(this, "sending credentialmessage and accepting offer: " + offerId + "from: " + peerId.toString());
        this.sharkPKIComponent.sendOnlineCredentialMessage();
        this.sendHedwigMessage(offerId.getBytes(StandardCharsets.UTF_8), URI_MAKE_OFFER, peerId, false, false);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                                     act on received messages                            //
    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages,
                                     String senderE2E, // E2E part
                                     List<ASAPHop> asapHops) throws IOException {
        CharSequence uri = asapMessages.getURI();
        boolean uriFound = false;
        if (uri.toString().equals(URI)) {
            parseMessageAndAddToUsersMessageList(URI, asapMessages, asapHops);
            uriFound = true;
        }

        if (uri.toString().equals(URI_MAKE_OFFER)) {
            Log.writeLog(this, "Checking Message in Offer Channel");

            parseMessageAndAddToUsersMessageList(URI_MAKE_OFFER, asapMessages, asapHops);
            uriFound = true;
        }

        if (uri.toString().equals(URI_SEND_DELIVERY_PACKAGE)) {
            parseMessageAndAddToUsersMessageList(URI_SEND_DELIVERY_PACKAGE, asapMessages, asapHops);

            // else if its a Android User then he tries to open if it opens then it send Confirmation message
            Log.writeLog(this, "Got Send delivery Message");
            uriFound = true;
        }

        if (uri.toString().equals(URI_PACKAGE_RECIEVED_CONFIRMATION)) {
            //add GPS and User to Peerslist in App
            parseMessageAndAddToUsersMessageList(URI_PACKAGE_RECIEVED_CONFIRMATION, asapMessages, asapHops);
            uriFound = true;
        }
        if (!uriFound) {
            parseMessageAndAddToUsersMessageList("NoUrI", asapMessages, asapHops);
        }
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)");
        this.notifyHedwigMessageReceivedListener(uri);
    }

    private void parseMessageAndAddToUsersMessageList(String uri, ASAPMessages asapMessages, List<ASAPHop> asapHops) throws IOException {
        asapMessages.getMessages().forEachRemaining(message -> {
            try {
                InMemoHedwigMessage inMemoHedwigMessage = InMemoHedwigMessage.parseMessage(message, asapHops, this.sharkPKIComponent);
                if (inMemoHedwigMessage.couldBeDecrypted()) {
                    Log.writeLog(this, "message could be decrypted");
                    if (URI_SEND_DELIVERY_PACKAGE.equals(uri)) {
                        Log.writeLog(this, HedwigApp.OWNER + " sending confirmation message to: " + inMemoHedwigMessage.getSender().toString());
                        this.sendHedwigMessage((HedwigApp.OWNER).getBytes(StandardCharsets.UTF_8), URI_PACKAGE_RECIEVED_CONFIRMATION, inMemoHedwigMessage.getSender().toString(), true, true);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ASAPException e) {
                e.printStackTrace();
            } catch (HedwigMessangerException e) {
                e.printStackTrace();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                       backdoor - remove it when finished implementing                   //
    /////////////////////////////////////////////////////////////////////////////////////////////

    public ASAPStorage getASAPStorage() throws IOException, ASAPException {
        return this.asapPeer.getASAPStorage(APP_FORMAT);
    }

    @Override
    public void onlinePeersChanged(Set<CharSequence> set) {
        this.onlinePeers = set;
        this.allPeers.addAll(set);
    }

    @Override
    public void credentialReceived(CredentialMessage credentialMessage) {
        credentialMessages.put(credentialMessage.getSubjectName().toString(), credentialMessage);
    }

    public void hedwigAcceptAndSignCredentialMessage(CredentialMessage credentialMessage) throws ASAPSecurityException, IOException {
        Log.writeLog(this, "going to issue a certificate");
        this.sharkPKIComponent.acceptAndSignCredential(credentialMessage);
        credentialMessages.remove(credentialMessage.getSubjectName().toString());
    }
}
