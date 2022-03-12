package net.sharksystem.hedwig;

import net.sharksystem.SharkException;
import net.sharksystem.SharkTestPeerFS;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.pki.CredentialMessage;
import net.sharksystem.pki.SharkPKIComponent;
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

        if (stop) {
            System.out.println(">>>>>>>>>>>>>>>>>  stop encounter: "
                + leftPeer.getASAPPeer().getPeerID() + " <--> " + rightPeer.getASAPPeer().getPeerID());
            leftPeer.getASAPTestPeerFS().stopEncounter(this.hedwigPeer.getASAPTestPeerFS());
            Thread.sleep(100);
        }
    }

    @Test
    public void whenSomeoneInRangeThenAddToList() throws SharkException, IOException, InterruptedException {

        this.setUpScenario_0();


        // add keys to peers
        SharkPKIComponent harryPKI = (SharkPKIComponent) this.harryPeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent herminoePKI = (SharkPKIComponent) this.herminoePeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent hedwigPKI = (SharkPKIComponent) this.hedwigPeer.getComponent(SharkPKIComponent.class);

        //create credential sendDeliveryMessages
        CredentialMessage harryCredentialMessage = harryPKI.createCredentialMessage();
        CredentialMessage herminoeCredentialMessage = herminoePKI.createCredentialMessage();

        // Harry and Herminoe exchange and accept credential message and issue certificates
        ASAPCertificate harryIssuedHerminoeCert = harryPKI.acceptAndSignCredential(herminoeCredentialMessage);
        ASAPCertificate herminoeIssuedHarryCert = herminoePKI.acceptAndSignCredential(harryCredentialMessage);

        // Harry broadcast message in channel URI - not signed, not encrypted
        harryMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, false, false);

        // Act run encounter
        this.runEncounter(this.harryPeer, this.herminoePeer, true);
        herminoeMessenger.sendHedwigMessage(MESSAGE_1_BYTE, URI, false, false);

        this.runEncounter(this.herminoePeer, this.harryPeer, true);
        harryMessenger.sendHedwigMessage(MESSAGE_2_BYTE, URI, this.herminoePeer.getPeerID(), false, false);
        harryMessenger.sendHedwigMessage(MESSAGE_3_BYTE, URI_MAKE_OFFER, this.herminoePeer.getPeerID(), false, false);
        this.runEncounter(this.harryPeer, this.herminoePeer, true);


        // assert peers list has one user
        ASAPStorage herminoeMessengerImplASAPStorage = herminoeMessengerImpl.getASAPStorage();
        List<CharSequence> sender = herminoeMessengerImplASAPStorage.getSender();
        Assert.assertEquals(sender.get(0), this.harryPeer.getPeerID());

        ASAPStorage harryMessengerImplASAPStorage = harryMessengerImpl.getASAPStorage();
        List<CharSequence> sender1 = harryMessengerImplASAPStorage.getSender();
        Assert.assertEquals(sender1.get(0), this.herminoePeer.getPeerID());

        /*
         *herminoe sends credential msg to harry
         * harry receive msg and sign it
         * harry send his credential msg to herminoe
         * herminoe sign it
         *
         *Assert everybody has credential msg of each
         */

        this.runEncounter(this.herminoePeer, this.harryPeer, false);
        herminoeMessenger.acceptOfferFromPeer(this.harryPeer.getPeerID(), URI_MAKE_OFFER);
        this.runEncounter(this.herminoePeer, this.harryPeer, false);

        HedwigMessageList makeOfferHarryMessages = this.harryMessengerImpl.getChannel(URI_MAKE_OFFER).getMessages();
        Assert.assertEquals(2, makeOfferHarryMessages.size());
        Assert.assertEquals(makeOfferHarryMessages.getHedwigMessage(0, true).getSender(), harryPeer.getPeerID());
        Assert.assertEquals(makeOfferHarryMessages.getHedwigMessage(1, true).getSender(), herminoePeer.getPeerID());

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
        harryMessenger.sendPackageToUser(URI_SEND_DELIVERY_PACKAGE, this.herminoePeer.getPeerID());
        runEncounter(this.harryPeer, this.hedwigPeer, false);
        runEncounter(this.hedwigPeer, this.herminoePeer, false);

        HedwigMessageList sendDeliveryMessages = herminoeMessengerImpl.getChannel(URI_SEND_DELIVERY_PACKAGE).getMessages();
        Assert.assertEquals(1, sendDeliveryMessages.size());
        Assert.assertEquals(this.harryPeer.getPeerID(), sendDeliveryMessages.getHedwigMessage(0, true).getSender());
        Assert.assertEquals(1, sendDeliveryMessages.getHedwigMessage(0, true).getASAPHopsList().size());
        Assert.assertEquals(this.harryPeer.getPeerID(), sendDeliveryMessages.getHedwigMessage(0, true).getASAPHopsList().get(0).sender());

        Assert.assertTrue(sendDeliveryMessages.getHedwigMessage(0, true).verified());
        Assert.assertTrue(sendDeliveryMessages.getHedwigMessage(0, true).couldBeDecrypted());

        /**
         *  on next encounter Harry recieves confirmation message sent automatically from Herminoe
         */
        runEncounter(this.hedwigPeer, this.harryPeer, false);

        HedwigMessageList confirmationMessages = harryMessengerImpl.getChannel(URI_PACKAGE_RECIEVED_CONFIRMATION).getMessages();
        Assert.assertEquals(1, confirmationMessages.size());
        Assert.assertEquals(confirmationMessages.getHedwigMessage(0, true).getSender(), herminoePeer.getPeerID());

    }

}
