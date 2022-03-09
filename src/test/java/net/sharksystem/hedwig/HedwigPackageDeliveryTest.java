package net.sharksystem.hedwig;


import net.sharksystem.SharkException;
import net.sharksystem.SharkTestPeerFS;

import net.sharksystem.asap.ASAPMessages;
import net.sharksystem.asap.ASAPStorage;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

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




         /*
         * Heminoe sends encrypted and signed confirmation msg to harry
         *
         * Assert harry receive the confirmation msg and could decrypt it.
          */
     }



}
