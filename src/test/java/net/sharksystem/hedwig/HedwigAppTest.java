package net.sharksystem.hedwig;

import net.sharksystem.SharkException;
import net.sharksystem.SharkTestPeerFS;
import net.sharksystem.SortedMessage;
import net.sharksystem.SortedMessageFactory;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HedwigAppTest extends HedwigTestHelper{

    public HedwigAppTest() { super(HedwigAppTest.class.getSimpleName());}

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
    public void test1_1() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setUpScenario_1();
        this.runTest_1();
    }

    public void runTest_1() throws SharkException, IOException, InterruptedException, ASAPException {

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
    public void test1_2() throws SharkException, ASAPSecurityException, IOException, InterruptedException {
        this.setUpScenario_1();
        this.runTest_2();
    }

    public void runTest_2() throws SharkException, IOException, InterruptedException, ASAPSecurityException {

        // Harry broadcast message in channel URI - signed, not encrypted
        harryMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, true, false);

        ///////////////////////////////// Encounter Harry - Herminoe  ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.herminoePeer, true);

        // test results
        HedwigMessageChannel herminoeChannel = herminoeMessenger.getChannel(URI);
        HedwigMessageI hedwigMessage = herminoeChannel.getMessages().getHedwigMessage(0, true);

        // message received by Herminoe from Harry?
        Assert.assertTrue(harryPeer.samePeer(hedwigMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(hedwigMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(hedwigMessage.encrypted());
        Assert.assertTrue(hedwigMessage.verified());

        SharkPKIComponent herminoePKI = herminoeMessenger.getSharkPKI();
        int herminoeIdentityAssuranceOfIfHarry = herminoePKI.getIdentityAssurance(harryPeer.getPeerID());
        Assert.assertEquals(10, herminoeIdentityAssuranceOfIfHarry); // both met

        ///////////////////////////////// Encounter Harry - Hedwig ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.hedwigPeer, true);

        // test results
        HedwigMessageChannel hedwigChannel = hedwigMessenger.getChannel(URI);
        hedwigMessage = hedwigChannel.getMessages().getHedwigMessage(0, true);

        // message received by Herminoe from Harry
        Assert.assertTrue(harryPeer.samePeer(hedwigMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(hedwigMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(hedwigMessage.encrypted());



        Assert.assertTrue(hedwigMessage.verified());


        ///////////////////////////////// Encounter Herminoe - Hedwig ////////////////////////////////////////////////////
        this.runEncounter(this.herminoePeer, this.hedwigPeer, true);

        // test results
        herminoeChannel = herminoeMessenger.getChannel(URI);
        hedwigMessage = herminoeChannel.getMessages().getHedwigMessage(0, true);

        // message received by Herminoe from Harry?
        Assert.assertTrue(hedwigPeer.samePeer(hedwigMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(hedwigMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(hedwigMessage.encrypted());
        Assert.assertFalse(hedwigMessage.verified());

        SharkPKIComponent hedwigPKI = hedwigMessenger.getSharkPKI();
        int hedwigIdentityAssuranceOfIfHerminoe = hedwigPKI.getIdentityAssurance(herminoePeer.getPeerID());

        // Herminoe  has not got a certificate from Hedwig
        Assert.assertEquals(5, hedwigIdentityAssuranceOfIfHerminoe);

        herminoePKI = herminoeMessenger.getSharkPKI();
        int herminoeIdentityAssuranceOfIfHedwig = herminoePKI.getIdentityAssurance(hedwigPeer.getPeerID());

        // Alice never met Clara nor has she got a certificate
        Assert.assertEquals(0, herminoeIdentityAssuranceOfIfHedwig);
    }

    @Test
    public void test1_4() throws SharkException, ASAPSecurityException, IOException, InterruptedException {
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
        HedwigMessageI undecryptedMsg = hedwigMessage0.couldBeDecrypted() ? hedwigMessage1 :  hedwigMessage0;

        // the other cannot be decrypted - it was not meant for this receiver
        Assert.assertFalse(undecryptedMsg.couldBeDecrypted());
    }

    @Test
    public void test1_5() throws SharkException, ASAPSecurityException, IOException, InterruptedException {
        this.setUpScenario_1();

        // send two encrypted message - one for Alice, another for Bob.
        hedwigMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, harryPeer.getPeerID(), false, true);
        hedwigMessenger.sendHedwigMessage(MESSAGE_BYTE, URI, herminoePeer.getPeerID(), false, true);

        ///////////////////////////////// Encounter Clara - Bob ////////////////////////////////////////////////////
        this.runEncounter(this.hedwigPeer, this.herminoePeer, true);

        // test results
        HedwigMessageChannel herminoeChannel = herminoeMessenger.getChannel(URI);
        HedwigMessageList herminoeChannelMessages = herminoeChannel.getMessages();
        Assert.assertEquals(2, herminoeChannelMessages.size());
        this.oneEncryptableOneIsNot(herminoeChannelMessages);

        this.runEncounter(this.harryPeer, this.hedwigPeer, true);

        // test results
        HedwigMessageChannel harryChannel = harryMessenger.getChannel(URI);
        // variant has a better performance - if order is not of any concern
        HedwigMessageList harryChannelMessages = harryChannel.getMessages(false, false);
        Assert.assertEquals(2, harryChannelMessages.size());
        this.oneEncryptableOneIsNot(harryChannelMessages);
    }

    @Test
    public void test1_8() throws ASAPSecurityException, SharkException, IOException, InterruptedException {
        this.setUpScenario_1();

        SortedMessageFactory harrySortedMessageFactory = new SortedHedwigMessageFactory();
        SortedMessageFactory herminoeSortedMessageFactory = new SortedHedwigMessageFactory();
        SortedMessage harrySortedMessage = harrySortedMessageFactory.produceSortedMessage(MESSAGE_BYTE, null);
        // Alice send a message to Bob
        harryMessenger.sendHedwigMessage(SortedMessageImpl.sortedMessageByteArray(harrySortedMessage) , URI, herminoePeer.getPeerID(), true, true);

        ///////////////////////////////// Encounter Alice - Bob ////////////////////////////////////////////////////
        this.runEncounter(this.harryPeer, this.herminoePeer, true);

        // Test results Bob received message
        HedwigMessageChannel herminoeChannel = herminoeMessenger.getChannel(URI);
        HedwigMessageI herminoeHedwigMessage = herminoeChannel.getMessages().getHedwigMessage(0, true);
        Assert.assertTrue(herminoeHedwigMessage.couldBeDecrypted());
        Assert.assertTrue(herminoeHedwigMessage.encrypted());
        Assert.assertTrue(herminoeHedwigMessage.verified());

        // message received by Bob from Alice?
        Assert.assertTrue(harryPeer.samePeer(herminoeHedwigMessage.getSender()));

        // Convert received bytes to SortedMessage
        SortedMessage herminoeSortedMessage = SortedMessageImpl.byteArrayToSortedMessage(herminoeHedwigMessage.getContent());

        // Add the received message to the factory
        herminoeSortedMessageFactory.addIncomingSortedMessage(herminoeSortedMessage);

        // Check sortedMessage id
        Assert.assertEquals(harrySortedMessage.getID(), herminoeSortedMessage.getID());

        Assert.assertTrue(Utils.compareArrays(herminoeSortedMessage.getContent(), MESSAGE_BYTE));

        // SortedMessage parents should be []
        Assert.assertTrue(herminoeSortedMessage.getParents().size() == 0);

        // Bob send a message to Alice
        SortedMessage herminoeSortedMessage_1 = herminoeSortedMessageFactory.produceSortedMessage(MESSAGE_1_BYTE, null);
        herminoeMessenger.sendHedwigMessage(SortedMessageImpl.sortedMessageByteArray(herminoeSortedMessage_1) , URI, harryPeer.getPeerID(), true, true);

        ///////////////////////////////// Encounter Bob - Alice ////////////////////////////////////////////////////
        this.runEncounter(this.herminoePeer, this.harryPeer, true);

        // Test results Bob received message
        HedwigMessageChannel harryChannel = harryMessenger.getChannel(URI);
        HedwigMessageI harryHedwigMessage_1 = harryChannel.getMessages().getHedwigMessage(0, true);
        Assert.assertTrue(harryHedwigMessage_1.couldBeDecrypted());
        Assert.assertTrue(harryHedwigMessage_1.encrypted());
        Assert.assertTrue(harryHedwigMessage_1.verified());

        // message received by Alice from Bob?
        Assert.assertTrue(herminoePeer.samePeer(harryHedwigMessage_1.getSender()));
        // Convert received bytes to SortedMessage
        SortedMessage harrySortedMessage_1 = SortedMessageImpl.byteArrayToSortedMessage(harryHedwigMessage_1.getContent());
        // Add the received message to the factory
        harrySortedMessageFactory.addIncomingSortedMessage(herminoeSortedMessage);
        // Check sortedMessage id
        Assert.assertEquals(herminoeSortedMessage_1.getID(), harrySortedMessage_1.getID());

        Assert.assertTrue(Utils.compareArrays(harrySortedMessage_1.getContent(), MESSAGE_1_BYTE));

        // SortedMessage parents should be ["id"] bobSortedMessage.getID()
        Assert.assertTrue(harrySortedMessage_1.getParents().size() == 1);
        Assert.assertTrue(harrySortedMessage_1.getParents().contains(herminoeSortedMessage.getID()));

        // Bob send a message to Alice with replyTo relation to bobSortedMessage_1
        SortedMessage herminoneSortedMessage_2 = herminoeSortedMessageFactory.produceSortedMessage(MESSAGE_2_BYTE, herminoeSortedMessage.getID());
        herminoeMessenger.sendHedwigMessage(SortedMessageImpl.sortedMessageByteArray(herminoneSortedMessage_2) , URI, harryPeer.getPeerID(), true, true);

        ///////////////////////////////// Encounter Bob - Alice ////////////////////////////////////////////////////
        this.runEncounter(this.herminoePeer, this.harryPeer, true);

        // Test results Bob received message
        HedwigMessageI harryHedwigMessage_2 = harryChannel.getMessages().getHedwigMessage(1, true);
        Assert.assertTrue(harryHedwigMessage_2.couldBeDecrypted());
        Assert.assertTrue(harryHedwigMessage_2.encrypted());
        Assert.assertTrue(harryHedwigMessage_2.verified());

        // message received by Alice from Bob?
        Assert.assertTrue(herminoePeer.samePeer(harryHedwigMessage_2.getSender()));

        // Convert received bytes to SortedMessage
        SortedMessage harrySortedMessage_2 = SortedMessageImpl.byteArrayToSortedMessage(harryHedwigMessage_2.getContent());

        // Add the received message to the factory
        harrySortedMessageFactory.addIncomingSortedMessage(harrySortedMessage_2);

        // Check sortedMessage id
        Assert.assertEquals(herminoneSortedMessage_2.getID(), harrySortedMessage_2.getID());
        Assert.assertTrue(Utils.compareArrays(harryHedwigMessage_2.getContent(), MESSAGE_2_BYTE));

        // SortedMessage parents should be ["id"] bobSortedMessage_1.getID()
        Assert.assertTrue(herminoneSortedMessage_2.getParents().size() == 1);
        Assert.assertTrue(herminoneSortedMessage_2.getParents().contains(harrySortedMessage_1.getID()));

        // SortedMessage reply To should be bobSortedMessage.getID()
        Assert.assertEquals(herminoneSortedMessage_2.getReplyTo(), herminoeSortedMessage.getID());
    }

}
