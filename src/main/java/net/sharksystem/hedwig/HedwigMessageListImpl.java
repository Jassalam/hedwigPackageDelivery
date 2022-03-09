package net.sharksystem.hedwig;

import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPHop;
import net.sharksystem.asap.ASAPMessages;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.List;

public class HedwigMessageListImpl implements HedwigMessageList{
    private final SharkPKIComponent pkiComponent;
    private final ASAPMessages asapMessages;

    public HedwigMessageListImpl(SharkPKIComponent pkiComponent, ASAPChannel asapChannel,
                                boolean sentMessagesOnly, boolean ordered) throws IOException, ASAPException {
        this.pkiComponent = pkiComponent;

        if (sentMessagesOnly) {
            this.asapMessages = asapChannel.getMessages();
        } else {
            if (ordered) {
                this.asapMessages = asapChannel.getMessages(new HedwigMessageComparison(pkiComponent));
            } else {
                this.asapMessages = asapChannel.getMessages(false);
            }
        }
    }

    @Override
    public HedwigMessageI getHedwigMessage(int position, boolean chronologically) throws HedwigMessangerException {
        try {
            List<ASAPHop> hopsList = this.asapMessages.getChunk(position, chronologically).getASAPHopList();
            byte[] content = this.asapMessages.getMessage(position, chronologically);
            return InMemoHedwigMessage.parseMessage(content, hopsList, this.pkiComponent);
        } catch (ASAPException | IOException asapException) {
            throw new HedwigMessangerException(asapException);
        }
    }

    @Override
    public int size() throws IOException {
        return this.asapMessages.size();
    }
}
