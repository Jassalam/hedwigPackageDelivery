package net.sharksystem.hedwig;

import net.sharksystem.SharkException;
import net.sharksystem.SharkNotSupportedException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.sharksystem.hedwig.AppConstants.*;

public class HedwigComponentImpl extends HedwigMessageReceivedListenerManager implements HedwigComponent, ASAPMessageReceivedListener, ASAPEnvironmentChangesListener {

    private final SharkPKIComponent sharkPKIComponent;
    private ASAPPeer asapPeer;
    public Set<CharSequence> onlinePeers = new HashSet<>();
    public Set<CharSequence> allPeers = new HashSet<>();
    /*
     *Package Information filled only when its a Hedwig
     *
     */
    private String packageReciever = "";

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
                                 boolean encrypt) throws IOException, HedwigMessangerException{
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

    @Override
    public void setChannelBehaviour(CharSequence uri, String behaviour) throws SharkUnknownBehaviourException, HedwigMessangerException {
        Log.writeLog(this, "setChannelBehaviour", "not yet implemented");
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
    public void sendMessageHedwigRegistrationIfOwnerAHedwig(CharSequence ownerId) throws IOException, HedwigMessangerException {
        if (ownerId.toString().startsWith(HEDWIG_PREFIX)) {
            Log.writeLog(this, "sending Hedwig Registration");
            this.sendHedwigMessage(String.valueOf(ownerId).getBytes(StandardCharsets.UTF_8), URI_HEDWIG_HEDWIG_REGISTRATION, false, false);
        }
    }

    @Override
    public void makeOfferToSendSomePackageToPeer(String peerId, String message) throws HedwigMessangerException, IOException  {
        Log.writeLog(this, "making an Offer to send some Package");
        this.sendHedwigMessage((peerId + ":" + message).getBytes(StandardCharsets.UTF_8), URI_MAKE_OFFER, Collections.singleton(peerId), true, false);
    }

    @Override
    public void sendUserLocation(String longitude, String latitude) throws IOException, HedwigMessangerException {
        Log.writeLog(this, "Sharing User Location");
        this.sendHedwigMessage(String.valueOf(longitude + ", " + latitude).getBytes(StandardCharsets.UTF_8), URI_USER_GPS, false, false);
    }

    @Override
    public void callHedwig(CharSequence hedwigID, String longitude, String latitude) throws IOException, HedwigMessangerException {
        this.sendHedwigMessage((hedwigID + ":" + longitude + ":" + latitude).getBytes(StandardCharsets.UTF_8), URI_CALL_HEDWIG, false, false);
    }

    @Override
    public void sendPackageToUserLocation(String message, CharSequence receiver) throws IOException, HedwigMessangerException {
        if (HedwigApp.OWNER.startsWith(HEDWIG_PREFIX)) packageReciever = receiver.toString();
        this.sendHedwigMessage(message.getBytes(StandardCharsets.UTF_8), URI_SEND_DELIVERY_PACKAGE, receiver, true, true);
    }

    @Override
    public void sendRecieverConfirmationAndDropPackage(String reciever) throws IOException, HedwigMessangerException {
        this.sendHedwigMessage((HedwigApp.OWNER).getBytes(StandardCharsets.UTF_8), URI_PACKAGE_RECIEVED_CONFIRMATION, reciever, true, true);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                                     act on received messages                            //
    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages,
                                     String senderE2E, // E2E part
                                     List<ASAPHop> asapHops) throws IOException {
        CharSequence uri = asapMessages.getURI();
        if (uri.toString().equals(URI_MAKE_OFFER)) {
            Log.writeLog(this, "Checking Message in Offer Channel");
            asapMessages.getMessages().forEachRemaining(message -> {
                String[] peerIdAndMeesage = message.toString().split(":");
                if (HedwigApp.OWNER.equals(peerIdAndMeesage[0])) {
                    List<String> messages = HedwigComponent.messages.get(senderE2E);
                    if (messages != null && !messages.isEmpty()) {
                        messages.add(peerIdAndMeesage[1]);
                        HedwigComponent.messages.put(senderE2E, messages);
                    } else {
                        HedwigComponent.messages.put(senderE2E, Collections.singletonList(peerIdAndMeesage[1]));
                    }
                }

            });
        }
        if (uri.toString().equals(URI_USER_GPS)) {
            //add GPS and User to Peerslist in App
            Log.writeLog(this, "Got GPS Message");
            asapMessages.getMessages().forEachRemaining(message ->
            {
                String[] split = message.toString().split(",");
                Optional<User> userPresent = users.stream()
                    .filter(user -> user.peerId.equals(senderE2E))
                    .findFirst();
                if (userPresent.isPresent()) {
                    users.remove(userPresent); //update user location anyway
                }
                users.add(new User(senderE2E, split[0], split[1]));
            });

        }

        if (uri.toString().equals(URI_CALL_HEDWIG)) {
            // fly to user IOT Call
            Log.writeLog(this, "Got Call Hedwig Message");

            //If message Reciever is a Hedwig then it goes to location and fly there
            for (Iterator<byte[]> it = asapMessages.getMessages(); it.hasNext(); ) {
                byte[] asapMessage = it.next();
                try {
                    InMemoHedwigMessage inMemoHedwigMessage = InMemoHedwigMessage.parseMessage(asapMessage, asapHops, this.sharkPKIComponent);
                    byte[] content = inMemoHedwigMessage.getContent();
                    String[] split = content.toString().split(":");
                    if (split[0].equals(HedwigApp.OWNER)) {
                        Log.writeLog(this, "flying to longitude: " + split[1] + " and latitude: " + split[2]);
                    }
                } catch (ASAPException asapException) {
                    asapException.printStackTrace();
                }
            }

        }

        if (uri.toString().equals(URI_SEND_DELIVERY_PACKAGE)) {
            //If message Reciever is a Hedwig then it checks user location and fly there
            for (Iterator<byte[]> it = asapMessages.getMessages(); it.hasNext(); ) {
                byte[] asapMessage = it.next();
                try {
                    InMemoHedwigMessage inMemoHedwigMessage = InMemoHedwigMessage.parseMessage(asapMessage, asapHops, this.sharkPKIComponent);
                    if (inMemoHedwigMessage.couldBeDecrypted()) {
                        // its end user send confirmation message in confirmation uri
                        this.sendRecieverConfirmationAndDropPackage(senderE2E);

                        byte[] content = inMemoHedwigMessage.getContent();
                        List<String> strings = messages.get(senderE2E);
                        if (strings != null && !strings.isEmpty()) {
                            strings.add(content.toString());
                            messages.put(senderE2E, strings);
                        } else {
                            messages.put(senderE2E, Collections.singletonList(content.toString()));
                        }

                    } else {
                        // its intermediatiaries hedwigs or users for which message is not meant, ideally add exchange protocol in same uri
                        this.sendHedwigMessage("Package Intermediary Hedwig has recieved Package".getBytes(StandardCharsets.UTF_8), uri, true, false);
                    }
                } catch (ASAPException | HedwigMessangerException e) {
                    e.printStackTrace();
                }
            }
            // else if its a Android User then he tries to open if it opens then it send Confirmation message
            Log.writeLog(this, "Got Send delivery Message");
        }

        if (uri.toString().equals(URI_PACKAGE_RECIEVED_CONFIRMATION)) {
            //add GPS and User to Peerslist in App
            Log.writeLog(this, "Got GPS Message");
            asapMessages.getMessages().forEachRemaining(message -> {
                String senderIdOnlyWhenMessageDecrypted = message.toString();
                if (HedwigApp.OWNER.startsWith(HEDWIG_PREFIX) && packageReciever.equals(senderIdOnlyWhenMessageDecrypted)) {
                    Log.writeLog(this, "Dropping package");
                }
                if (HedwigApp.OWNER.equals(senderIdOnlyWhenMessageDecrypted)) {
                    List<String> strings = messages.get(senderE2E);
                    String messageToAdd = "Package Recieved Confirmation from Reciever: " + senderE2E;
                    if (strings != null && !strings.isEmpty()) {
                        strings.add(messageToAdd);
                        messages.put(senderE2E, strings);
                    }
                    messages.put(senderE2E, Collections.singletonList(messageToAdd));

                }
            });

        }
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)");

        this.notifyHedwigMessageReceivedListener(uri);
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
}
