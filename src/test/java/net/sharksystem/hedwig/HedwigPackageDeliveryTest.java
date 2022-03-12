package net.sharksystem.hedwig;


import net.sharksystem.SharkException;
import net.sharksystem.SharkTestPeerFS;

import net.sharksystem.asap.ASAPHop;
import net.sharksystem.asap.ASAPMessageReceivedListener;
import net.sharksystem.asap.ASAPMessages;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.pki.CredentialMessage;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static net.sharksystem.hedwig.AppConstants.*;

public class HedwigPackageDeliveryTest extends HedwigTestHelper {

    public HedwigPackageDeliveryTest() {
        super(HedwigPackageDeliveryTest.class.getSimpleName());
    }

    public void runEncounter(SharkTestPeerFS leftPeer, SharkTestPeerFS rightPeer, boolean stop)
        throws SharkException, IOException, InterruptedException {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("                       start encounter: "
            + leftPeer.getASAPPeer().getPeerID() + " <--> " + rightPeer.getASAPPeer().getPeerID());
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        leftPeer.getASAPTestPeerFS().startEncounter(HedwigTestHelper.getPortNumber(), rightPeer.getASAPTestPeerFS());
        // give them moment to exchange data
        Thread.sleep(1000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        if(stop) {
            System.out.println(">>>>>>>>>>>>>>>>>  stop encounter: "
                + leftPeer.getASAPPeer().getPeerID() + " <--> " + rightPeer.getASAPPeer().getPeerID());
            leftPeer.getASAPTestPeerFS().stopEncounter(this.hedwigPeer.getASAPTestPeerFS());
            Thread.sleep(100);
        }
    }

     @Test
     public void whenSomeoneInRangeThenAddToList() throws SharkException, IOException, InterruptedException {

         this.setUpScenario_0();

         // Harry broadcast message in channel URI - not signed, not encrypted
         harryMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, false, false);

         // Act run encounter
         this.runEncounter(this.harryPeer, this.herminoePeer, true);
         herminoeMessenger.sendHedwigMessage(MESSAGE_1_BYTE, URI,false,false);

         this.runEncounter(this.herminoePeer, this.harryPeer, true);
         harryMessenger.sendHedwigMessage(MESSAGE_2_BYTE, URI, this.herminoePeer.getPeerID(), false, false);
         harryMessenger.sendHedwigMessage(MESSAGE_3_BYTE, URI, this.herminoePeer.getPeerID(), false, false);
         this.runEncounter(this.harryPeer, this.herminoePeer, true);
         herminoeMessenger.sendHedwigMessage(MESSAGE_4_BYTE, URI, this.harryPeer.getPeerID(), false, false);
         this.runEncounter(this.herminoePeer, this.harryPeer, true);

         // assert peers list has one user
         ASAPStorage herminoeMessengerImplASAPStorage = herminoeMessengerImpl.getASAPStorage();
         List<CharSequence> sender = herminoeMessengerImplASAPStorage.getSender();
         Assert.assertEquals(sender.get(0), this.harryPeer.getPeerID());
         ASAPStorage harryMessengerImplASAPStorage = harryMessengerImpl.getASAPStorage();
         List<CharSequence> sender1 = harryMessengerImplASAPStorage.getSender();
         Assert.assertEquals(sender1.get(0), this.herminoePeer.getPeerID());

         ASAPMessages asapMessages = herminoeMessengerImplASAPStorage.getChannel(URI).getMessages();

         Assert.assertEquals(2, asapMessages.size());


         ASAPMessages messagesHarry = harryMessengerImplASAPStorage.getChannel(URI).getMessages();
         Assert.assertEquals(3, messagesHarry.size());

         /*
         *herminoe sends credential msg to harry
         * harry receive msg and sign it
         * harry send his credential msg to herminoe
         * herminoe sign it
         *
         *Assert everybody has credential msg of each
          */
         // add keys to peers
         SharkPKIComponent harryPKI = (SharkPKIComponent) this.harryPeer.getComponent(SharkPKIComponent.class);
         SharkPKIComponent herminoePKI = (SharkPKIComponent) this.herminoePeer.getComponent(SharkPKIComponent.class);
         SharkPKIComponent hedwigPKI = (SharkPKIComponent) this.hedwigPeer.getComponent(SharkPKIComponent.class);

         //create credential messages
         CredentialMessage harryCredentialMessage = harryPKI.createCredentialMessage();
         CredentialMessage herminoeCredentialMessage = herminoePKI.createCredentialMessage();

         // Harry and Herminoe exchange and accept credential message and issue certificates
         ASAPCertificate harryIssuedHerminoeCert = harryPKI.acceptAndSignCredential(herminoeCredentialMessage);
         ASAPCertificate herminoeIssuedHarryCert = herminoePKI.acceptAndSignCredential(harryCredentialMessage);

         // hedwig gets credential message from Harry and sign it

         harryPKI.addCertificate(herminoeIssuedHarryCert);

         herminoePKI.addCertificate(harryIssuedHerminoeCert);

         Assert.assertEquals(1, harryPKI.getCertificatesByIssuer(herminoePeer.getPeerID()).size());
         Assert.assertEquals(1, herminoePKI.getCertificatesByIssuer(harryPeer.getPeerID()).size());

         /*
         *harry sends encrypted signed msg to heminoe through hedwig
         *
         *
         * Assert hedwig and heminoe receive the msg
         * hedwig was not able to decrypt it
         * heminoe could decrypt the msg
         *
         * heminoe could see all intermeditories of the msg(e.g hedwig)
         *
          */
         this.harryMessengerImpl.createChannel(URI_MAKE_OFFER, CHANNEL_MAKE_OFFER );
         this.hedwigMessengerImpl.createChannel(URI_MAKE_OFFER, CHANNEL_MAKE_OFFER );
         this.herminoeMessengerImpl.createChannel(URI_MAKE_OFFER, CHANNEL_MAKE_OFFER );

        harryMessenger.sendHedwigMessage("OFFER".getBytes(), URI_MAKE_OFFER, herminoePeer.getPeerID(), false, false );

         this.runEncounter(this.harryPeer, this.hedwigPeer, true);

         HedwigMessageList messages = hedwigMessengerImpl.getChannel(URI_MAKE_OFFER).getMessages();
         Assert.assertEquals(1, messages.size());

         /*
          * Hedwig sends message to herminoe
          */
         this.runEncounter(this.hedwigPeer, this.herminoePeer, true);

         this.runEncounter(this.herminoePeer, this.hedwigPeer, true);
         this.runEncounter(this.hedwigPeer, this.herminoePeer, true);


         messages = hedwigMessengerImpl.getChannel(URI_MAKE_OFFER).getMessages();
         Assert.assertEquals(1, messages.size());

         messages = herminoeMessengerImpl.getChannel(URI_MAKE_OFFER).getMessages();
         Assert.assertEquals(1, messages.size());
         Assert.assertEquals(harryPeer.getPeerID(), messages.getHedwigMessage(0, true).getSender());
         Assert.assertTrue(messages.getHedwigMessage(0, false).couldBeDecrypted());
         Assert.assertTrue(messages.getHedwigMessage(0, false).verified());

         /*
          * Heminoe sends encrypted and signed confirmation msg to harry
         *
         * Assert harry receive the confirmation msg and could decrypt it.
          */
     }

}
