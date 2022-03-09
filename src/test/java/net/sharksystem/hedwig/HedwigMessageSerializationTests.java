package net.sharksystem.hedwig;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPHop;
import net.sharksystem.asap.crypto.InMemoASAPKeyStore;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static net.sharksystem.hedwig.HedwigTestConstants.*;

public class HedwigMessageSerializationTests {
    public static final String WORKING_SUB_DIRECTORY = HedwigTestConstants.ROOT_DIRECTORY
        + HedwigMessageSerializationTests.class.getSimpleName() + "/";
    public static final String MESSAGE = "Hi";
    public static final String URI = "sn2://all";
    public static final String HARRY_FOLDER = WORKING_SUB_DIRECTORY + HARRY_ID;

    @Test
    @Ignore
    public void serializationTestPlain() throws ASAPException, IOException {
        byte[] serializedSNMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, HERMINOE_ID);

        InMemoHedwigMessage hedwigNetMessage =
            InMemoHedwigMessage.parseMessage(serializedSNMessage, new ArrayList<ASAPHop>());

        Assert.assertEquals(MESSAGE, new String(hedwigNetMessage.getContent()));
        Assert.assertEquals(HARRY_ID, hedwigNetMessage.getSender());
        Assert.assertFalse(hedwigNetMessage.verified());
        Assert.assertFalse(hedwigNetMessage.encrypted());
    }

    @Test
    public void serializationTestSigned() throws ASAPException, IOException {
        InMemoASAPKeyStore keyStorageHarry = new InMemoASAPKeyStore(HARRY_ID);
        KeyPair herminoeKeyPair = keyStorageHarry.createTestPeer(HERMINOE_ID); // Alice knows Bob

        InMemoASAPKeyStore keyStorageHerminoe = new InMemoASAPKeyStore(HERMINOE_ID, herminoeKeyPair,System.currentTimeMillis());
        keyStorageHerminoe.addKeyPair(HARRY_ID, keyStorageHarry.getKeyPair()); // Bob knows Alice

        byte[] serializedSNMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, HERMINOE_ID, true, false, keyStorageHarry);

        InMemoHedwigMessage hedwigNetMessage =
            InMemoHedwigMessage.parseMessage(serializedSNMessage, new ArrayList<ASAPHop>(), keyStorageHerminoe);

        Assert.assertEquals(MESSAGE, new String(hedwigNetMessage.getContent()));
        Assert.assertEquals(HARRY_ID, hedwigNetMessage.getSender());
        Assert.assertTrue(hedwigNetMessage.verified());
        Assert.assertFalse(hedwigNetMessage.encrypted());
    }

    @Test
    @Ignore
    public void serializationTestSignedNotVerified() throws ASAPException, IOException {

        InMemoASAPKeyStore keyStorageHarry = new InMemoASAPKeyStore(HARRY_ID);
        KeyPair herminoeKeyPair = keyStorageHarry.createTestPeer(HERMINOE_ID);
        InMemoASAPKeyStore keyStorageHerminoe = new InMemoASAPKeyStore(HERMINOE_ID, herminoeKeyPair,System.currentTimeMillis());
        // Bob does not know Alice

        byte[] serializedSNMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, HERMINOE_ID, true, false, keyStorageHarry);

        InMemoHedwigMessage hedwigNetMessage =
            InMemoHedwigMessage.parseMessage(serializedSNMessage, new ArrayList<ASAPHop>(), keyStorageHerminoe);

        Assert.assertEquals(MESSAGE, new String(hedwigNetMessage.getContent()));
        Assert.assertEquals(HARRY_ID, hedwigNetMessage.getSender());
        Assert.assertFalse(hedwigNetMessage.verified());
        Assert.assertFalse(hedwigNetMessage.encrypted());
    }

    @Test
    public void serializationTestEncrypted() throws ASAPException, IOException {
        InMemoASAPKeyStore keyStorageHarry = new InMemoASAPKeyStore(HARRY_ID);
        KeyPair herminoeKeyPair = keyStorageHarry.createTestPeer(HERMINOE_ID); // Alice knows Bob

        InMemoASAPKeyStore keyStorageHerminoe = new InMemoASAPKeyStore(HERMINOE_ID, herminoeKeyPair,System.currentTimeMillis());
        keyStorageHerminoe.addKeyPair(HARRY_ID, keyStorageHarry.getKeyPair()); // Bob knows Alice

        byte[] serializedSNMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, HERMINOE_ID, false, true,keyStorageHarry);

        InMemoHedwigMessage hedwigNetMessage =
            InMemoHedwigMessage.parseMessage(serializedSNMessage, new ArrayList<ASAPHop>(), keyStorageHerminoe);

        Assert.assertEquals(MESSAGE, new String(hedwigNetMessage.getContent()));
        Assert.assertEquals(HARRY_ID, hedwigNetMessage.getSender());
        Assert.assertFalse(hedwigNetMessage.verified());
        Assert.assertTrue(hedwigNetMessage.encrypted());
    }

    @Test
    public void serializationTestEncryptedAndSigned() throws ASAPException, IOException {
        InMemoASAPKeyStore keyStorageHarry = new InMemoASAPKeyStore(HARRY_ID);
        KeyPair herminoeKeyPair = keyStorageHarry.createTestPeer(HERMINOE_ID); // Alice knows Bob

        InMemoASAPKeyStore keyStorageHerminoe = new InMemoASAPKeyStore(HERMINOE_ID, herminoeKeyPair,System.currentTimeMillis());
        keyStorageHerminoe.addKeyPair(HARRY_ID, keyStorageHarry.getKeyPair()); // Bob knows Alice

        byte[] serializedSNMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, HERMINOE_ID, true, true, keyStorageHarry);

        InMemoHedwigMessage hedwigNetMessage =
            InMemoHedwigMessage.parseMessage(serializedSNMessage, new ArrayList<ASAPHop>(), keyStorageHerminoe);

        Assert.assertEquals(MESSAGE, new String(hedwigNetMessage.getContent()));
        Assert.assertEquals(HARRY_ID, hedwigNetMessage.getSender());
        Assert.assertTrue(hedwigNetMessage.verified());
        Assert.assertTrue(hedwigNetMessage.encrypted());
    }

    @Test
    public void snTestSignedMultipleRecipients() throws ASAPException, IOException, InterruptedException {
        // Alice
        InMemoASAPKeyStore keyStorageHarry = new InMemoASAPKeyStore(HARRY_ID);

        InMemoASAPKeyStore keyStorageHerminoe = new InMemoASAPKeyStore(HERMINOE_ID);
        keyStorageHerminoe.addKeyPair(HARRY_ID, keyStorageHarry.getKeyPair()); // Bob knows Alice

        Set<CharSequence> recipients = new HashSet<>();
        recipients.add(HERMINOE_ID);
        recipients.add(HEDWIG_ID);
        // create Message
        byte[] asapMessage = InMemoHedwigMessage.serializeMessage(
            MESSAGE.getBytes(), HARRY_ID, recipients, true, false, keyStorageHarry);

        long now = System.currentTimeMillis();

        // parse
        InMemoHedwigMessage receivedMessage =
            InMemoHedwigMessage.parseMessage(asapMessage, new ArrayList<ASAPHop>(), keyStorageHerminoe);

        Assert.assertEquals(MESSAGE, new String(receivedMessage.getContent()));
        Assert.assertEquals(2, receivedMessage.getRecipients().size());
        Assert.assertEquals(HARRY_ID, receivedMessage.getSender());
        Assert.assertTrue(receivedMessage.verified());
        Assert.assertFalse(receivedMessage.encrypted());


        // check timestamp
        Timestamp creationTime = receivedMessage.getCreationTime();
        long diff = now - creationTime.getTime();
        System.out.println("diff == " + diff);
        // should not be that long
        Assert.assertTrue(diff < 100);
    }
}
