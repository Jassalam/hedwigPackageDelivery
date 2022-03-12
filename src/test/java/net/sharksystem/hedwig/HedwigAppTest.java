package net.sharksystem.hedwig;

import net.sharksystem.SharkException;
import net.sharksystem.SharkTestPeerFS;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HedwigAppTest extends HedwigTestHelper {

    public HedwigAppTest() {
        super(HedwigAppTest.class.getSimpleName());
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
    public void test1_1() throws SharkException, IOException, InterruptedException {
        this.setUpScenario_1();
        this.runTest_1();
    }

    public void runTest_1() throws SharkException, IOException, InterruptedException {

        // Harry broadcast message in channel URI - not signed, not encrypted
        harryMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, false, false);

        ///////////////////////////////// Encounter Harry - Herminoe   ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.herminoePeer, true);

        // test results
        ASAPStorage herminoeAsapStorage = herminoeMessengerImpl.getASAPStorage();
        List<CharSequence> senderList = herminoeAsapStorage.getSender();
        Assert.assertNotNull(senderList);
        Assert.assertFalse(senderList.isEmpty());
        CharSequence senderID = senderList.get(0);
        Assert.assertTrue(harryPeer.samePeer(senderID));
        ASAPStorage senderIncomingStorage = herminoeAsapStorage.getExistingIncomingStorage(senderID);
        ASAPChannel channel = senderIncomingStorage.getChannel(URI);
        byte[] message = channel.getMessages().getMessage(0, true);
        Assert.assertNotNull(message);

        HedwigMessageChannel herminoeChannel = herminoeMessenger.getChannel(URI);
        HedwigMessageI hedwigMessage = herminoeChannel.getMessages().getHedwigMessage(0, true);

        // message received by Hermione from Harry
        Assert.assertTrue(harryPeer.samePeer(hedwigMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(hedwigMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(hedwigMessage.encrypted());
        Assert.assertFalse(hedwigMessage.verified());

    }

    @Test
    public void test1_2() throws SharkException, IOException, InterruptedException {
        this.setUpScenario_1();

        // Alice broadcast message in channel URI - signed, not encrypted
        harryMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, herminoePeer.getPeerID(), false, true);

        ///////////////////////////////// Encounter Alice - Bob ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.herminoePeer, true);

        // test results
        HedwigMessageChannel herminoeChannel = herminoeMessenger.getChannel(URI);
        HedwigMessageI hedwigMessage = herminoeChannel.getMessages().getHedwigMessage(0, true);
        Assert.assertTrue(hedwigMessage.couldBeDecrypted());
        // message received by Bob from Alice?
        Assert.assertTrue(harryPeer.samePeer(hedwigMessage
            .getSender()));
        Assert.assertTrue(Utils.compareArrays(hedwigMessage.getContent(), MESSAGE_BYTE));
        Assert.assertTrue(hedwigMessage.encrypted());
        Assert.assertFalse(hedwigMessage.verified());

        ///////////////////////////////// Encounter Alice - Clara ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.hedwigPeer, true);

        // test results
        HedwigMessageChannel hedwigChannel = hedwigMessenger.getChannel(URI);
        hedwigMessage = hedwigChannel.getMessages().getHedwigMessage(0, true);
        // message received by Bob from Alice?
        Assert.assertFalse(hedwigMessage.couldBeDecrypted());
    }

    /*
     * v) Clara sends two messages (unsigned and encrypted) to B and than A. Both can decrypt.
     * Bob is sure of Clara's identity. Alice is not.
     */

    private void oneEncryptableOneIsNot(HedwigMessageList msgList) throws HedwigMessangerException {
        HedwigMessageI hedwigMessage0 = msgList.getHedwigMessage(0, true);
        HedwigMessageI hedwigMessage1 = msgList.getHedwigMessage(1, true);

        HedwigMessageI decryptedMsg = hedwigMessage0.couldBeDecrypted() ? hedwigMessage0 : hedwigMessage1;
        HedwigMessageI undecryptedMsg = hedwigMessage0.couldBeDecrypted() ? hedwigMessage1 : hedwigMessage0;

        // the other cannot be decrypted - it was not meant for this receiver
        Assert.assertFalse(undecryptedMsg.couldBeDecrypted());
    }
}
