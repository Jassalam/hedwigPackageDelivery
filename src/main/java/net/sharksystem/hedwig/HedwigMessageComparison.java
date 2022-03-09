package net.sharksystem.hedwig;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPHop;
import net.sharksystem.asap.ASAPMessageCompare;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.ArrayList;

public class HedwigMessageComparison implements ASAPMessageCompare {
    private final SharkPKIComponent pki;

    HedwigMessageComparison(SharkPKIComponent pki) {
        this.pki = pki;
    }

    @Override
    public boolean earlier(byte[] msgA, byte[] msgB) {
        try {
            InMemoHedwigMessage hedwigMsgA = InMemoHedwigMessage.parseMessage(msgA, new ArrayList<ASAPHop>(), this.pki);
            InMemoHedwigMessage hedwigMsgB = InMemoHedwigMessage.parseMessage(msgB, new ArrayList<ASAPHop>(), this.pki);

            long creationTimeA = -1;
            long creationTimeB = -1;

            if(hedwigMsgA.couldBeDecrypted()) {
                creationTimeA = hedwigMsgA.getCreationTime().getTime();
            }
            if(hedwigMsgB.couldBeDecrypted()) {
                creationTimeB = hedwigMsgB.getCreationTime().getTime();
            }

            return creationTimeA < creationTimeB;

        } catch (IOException | ASAPException e) {
            // no choice: interface prevents me from throwing an exception
            return false;
        }
    }
}
