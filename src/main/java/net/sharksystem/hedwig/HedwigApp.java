package net.sharksystem.hedwig;

import net.sharksystem.SharkPeer;
import net.sharksystem.SharkPeerFS;
import net.sharksystem.asap.ASAPPeerFS;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static net.sharksystem.hedwig.AppConstants.*;
import static net.sharksystem.hedwig.HedwigComponent.APP_FORMAT;

public class HedwigApp {
    public static final String OWNER = HEDWIG_PREFIX + UUID.randomUUID();


    public static void main(String[] args) {
        Collection<CharSequence> formats = new ArrayList<>();
        formats.add(APP_FORMAT);

        try {
            ASAPPeerFS asapPeer = new ASAPPeerFS(OWNER, ROOT_FOLDER, formats);
            SharkPeer sharkPeer = new SharkPeerFS(OWNER, ROOT_FOLDER);

            /**
             * process to intialize hedwig component
             */

            // create a component factory
            SharkPKIComponentFactory certificateComponentFactory = new SharkPKIComponentFactory();

            // register this component with shark peer - note: we use interface SharkPeer
            sharkPeer.addComponent(certificateComponentFactory, SharkPKIComponent.class);

            HedwigComponentFactory hedwigComponentFactory =
                new HedwigComponentFactory(
                    (SharkPKIComponent) sharkPeer.getComponent(SharkPKIComponent.class)
                );

            sharkPeer.addComponent(hedwigComponentFactory, HedwigComponent.class);

            HedwigComponent hedwigComponent = (HedwigComponent) sharkPeer.getComponent(HedwigComponent.class);

            // start Sharkpeer
            sharkPeer.start(asapPeer);

            System.out.println("Peer started");

            // create channel and send message to register Hedwigs only, send to all Peers
            hedwigComponent.createChannel(URI_HEDWIG_HEDWIG_REGISTRATION, CHANNEL_NAME_HEDWIG_REGISTRATION);
            hedwigComponent.sendMessageHedwigRegistrationIfOwnerAHedwig(OWNER);

            // Its a Channel for making Offer to a User to send him a package
            hedwigComponent.createChannel(URI_MAKE_OFFER, CHANNEL_MAKE_OFFER);

            //  should be created in InitialActivity. Its Channel for users GPS. Send to all Peers
            hedwigComponent.createChannel(URI_USER_GPS, CHANNEL_NAME_USER_GPS);


            // create channel to send Package to particular location as soon as Hedwig get it, it fly to that location.
            hedwigComponent.createChannel(URI_SEND_DELIVERY_PACKAGE, CHANNEL_NAME_SEND_DELIVERY_PACKAGE);
            //hedwigComponent.sendPackageToUserLocation("test message", "HARRY_ID");

            // Only to check
            HedwigMessageList messages = hedwigComponent.getChannel(URI_HEDWIG_HEDWIG_REGISTRATION).getMessages();
            System.out.println("messages size: " + messages.size() +
                ", first sender: " + messages.getHedwigMessage(0, true).getSender() +
                ", first reciever: " + messages.getHedwigMessage(0, true).getRecipients().toString());

            messages = hedwigComponent.getChannel(URI_USER_GPS).getMessages();
            System.out.println("messages size: " + messages.size() +
                ", first sender: " + messages.getHedwigMessage(0, true).getSender() +
                ", first reciever: " + messages.getHedwigMessage(0, true).getRecipients().toString());


            Thread.sleep(360000);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
