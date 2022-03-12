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
import static net.sharksystem.hedwig.HedwigComponent.URI;

public class HedwigApp {
    public static final String OWNER = UUID.randomUUID().toString();


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

            // Its a Channel for making Offer to a User to send him a package
            hedwigComponent.createChannel(URI, "HEDWIG");
            // Its a Channel for making Offer to a User to send him a package
            hedwigComponent.createChannel(URI_MAKE_OFFER, CHANNEL_MAKE_OFFER);
            hedwigComponent.createChannel(URI_SEND_DELIVERY_PACKAGE, CHANNEL_NAME_SEND_DELIVERY_PACKAGE);
            hedwigComponent.createChannel(URI_PACKAGE_RECIEVED_CONFIRMATION, CHANNEL_PACKAGE_RECIEVED_CONFIRMATION);

            Thread.sleep(360000);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
