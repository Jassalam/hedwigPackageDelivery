package net.sharksystem.hedwig;


import net.sharksystem.SharkException;
import net.sharksystem.SharkPeer;
import net.sharksystem.SharkTestPeerFS;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.hedwig.HedwigTestConstants.*;
import net.sharksystem.pki.CredentialMessage;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;

import java.io.IOException;

import static net.sharksystem.hedwig.AppConstants.CHANNEL_MAKE_OFFER;
import static net.sharksystem.hedwig.AppConstants.URI_MAKE_OFFER;
import static net.sharksystem.hedwig.HedwigTestConstants.*;

public class HedwigTestHelper{
    public static final String MESSAGE = "Hi";
    public static final byte[] MESSAGE_BYTE = MESSAGE.getBytes();
    public static final String MESSAGE_1 = "Hello";
    public static final byte[] MESSAGE_1_BYTE = MESSAGE_1.getBytes();
    public static final String MESSAGE_2 = "Hi, Herminoe";
    public static final byte[] MESSAGE_2_BYTE = MESSAGE_2.getBytes();
    public static final String MESSAGE_3 = "Want to Get Package Delivery Service";
    public static final byte[] MESSAGE_3_BYTE = MESSAGE_3.getBytes();
    public static final String MESSAGE_4 = "Yes, I want.";
    public static final byte[] MESSAGE_4_BYTE = MESSAGE_4.getBytes();
    public static final String MESSAGE_5 = "No, I Don't.";
    public static final byte[] MESSAGE_5_BYTE = MESSAGE_5.getBytes();

    public static final String URI = "sn2://all";

    public static int testNumber = 0;

    public static int portNumber = 10000;


    public static int getPortNumber() { return HedwigTestHelper.portNumber++;}


    public final String subRootFolder;
    public final String harryFolder;
    public final String herminoeFolder;
    public final String hedwigFolder;

    protected SharkTestPeerFS harryPeer;
    protected SharkTestPeerFS hedwigPeer;
    protected SharkTestPeerFS herminoePeer;

    protected HedwigComponent harryMessenger;
    protected HedwigComponent hedwigMessenger;
    protected HedwigComponent herminoeMessenger;

    protected HedwigComponentImpl harryMessengerImpl;
    protected HedwigComponentImpl hedwigMessengerImpl;
    protected HedwigComponentImpl herminoeMessengerImpl;

    public  final String testName;


    public HedwigTestHelper(String testName) {
        this.testName = testName;

        this.subRootFolder = HedwigTestConstants.ROOT_DIRECTORY + testName + "/";

        this.harryFolder = subRootFolder + HARRY_ID;
        this.herminoeFolder = subRootFolder + HERMINOE_ID;
        this.hedwigFolder = subRootFolder + HEDWIG_ID;
    }

    public void setupHarryPeerOnly(){
        System.out.println("Test number = " +testNumber);
        String harryFolderName = harryFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(harryFolderName);
        this.harryPeer = new SharkTestPeerFS(HARRY_ID, harryFolderName);
    }

    /*
     *Scenario 0;
     *Harry and Herminoe exchange msg to ask if she needs a package
     */

    public void setUpScenario_0() throws SharkException, IOException, HedwigMessangerException {
        System.out.println("Test number = " + testNumber);
        String harryFolderName = harryFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(harryFolderName);
        this.harryPeer = new SharkTestPeerFS(HARRY_ID, harryFolderName);
        HedwigTestHelper.setupComponent(this.harryPeer);

        String hermioneFolderName = herminoeFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(hermioneFolderName);
        this.herminoePeer = new SharkTestPeerFS(HERMINOE_ID, hermioneFolderName);
        HedwigTestHelper.setupComponent(this.herminoePeer);

        String hedwigFolderName = hedwigFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(hedwigFolderName);
        this.hedwigPeer = new SharkTestPeerFS(HEDWIG_ID, hedwigFolderName);
        HedwigTestHelper.setupComponent(this.hedwigPeer);

        testNumber++;

        // start Peers
        this.harryPeer.start();
        this.hedwigPeer.start();
        this.herminoePeer.start();

        this.harryMessenger = (HedwigComponent) this.harryPeer.getComponent(HedwigComponent.class);
        this.herminoeMessenger = (HedwigComponent) this.herminoePeer.getComponent(HedwigComponent.class);
        this.hedwigMessenger = (HedwigComponent) this.hedwigPeer.getComponent(HedwigComponent.class);

        this.harryMessengerImpl = (HedwigComponentImpl) this.harryMessenger;
        this.herminoeMessengerImpl =(HedwigComponentImpl) this.herminoeMessenger;
        this.hedwigMessengerImpl = (HedwigComponentImpl) this.hedwigMessenger;

    }


    /*
    *Scenario 1;
    *Harry and Herminoe exchange msg and
     */

    public void setUpScenario_1() throws SharkException, IOException, HedwigMessangerException {
        System.out.println("Test number = " + testNumber);
        String harryFolderName = harryFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(harryFolderName);
        this.harryPeer = new SharkTestPeerFS(HARRY_ID, harryFolderName);
        HedwigTestHelper.setupComponent(this.harryPeer);

        String hermioneFolderName = herminoeFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(hermioneFolderName);
        this.herminoePeer = new SharkTestPeerFS(HERMINOE_ID, hermioneFolderName);
        HedwigTestHelper.setupComponent(this.herminoePeer);

        String hedwigFolderName = hedwigFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(hedwigFolderName);
        this.hedwigPeer = new SharkTestPeerFS(HEDWIG_ID, hedwigFolderName);
        HedwigTestHelper.setupComponent(this.hedwigPeer);

        testNumber++;

        // start Peers
        this.harryPeer.start();
        this.hedwigPeer.start();
        this.herminoePeer.start();

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

        // hedwig gets certificate issued by Harry for Herminoe
        hedwigPKI.addCertificate(harryIssuedHerminoeCert);

        this.harryMessenger = (HedwigComponent) this.harryPeer.getComponent(HedwigComponent.class);
        this.herminoeMessenger = (HedwigComponent) this.herminoePeer.getComponent(HedwigComponent.class);
        this.hedwigMessenger = (HedwigComponent) this.hedwigPeer.getComponent(HedwigComponent.class);

        this.harryMessengerImpl = (HedwigComponentImpl) this.harryMessenger;
        this.herminoeMessengerImpl =(HedwigComponentImpl) this.herminoeMessenger;
        this.hedwigMessengerImpl = (HedwigComponentImpl) this.hedwigMessenger;

    }

    public static HedwigComponent setupComponent(SharkPeer sharkPeer)
        throws SharkException{

        // component factory
        SharkPKIComponentFactory certificateComponentFactory = new SharkPKIComponentFactory();

        // register component with shark peer
        sharkPeer.addComponent(certificateComponentFactory, SharkPKIComponent.class);

        HedwigComponentFactory messengerFactory =
            new HedwigComponentFactory((SharkPKIComponent) sharkPeer.getComponent(SharkPKIComponent.class)
            );

        sharkPeer.addComponent(messengerFactory, HedwigComponent.class);

        return (HedwigComponent)
        sharkPeer.getComponent(HedwigComponent.class);
    }



}
