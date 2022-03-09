package net.sharksystem.hedwig;

import java.io.IOException;

public interface HedwigMessageList {
    HedwigMessageI getHedwigMessage(int position, boolean chronologically) throws HedwigMessangerException;

    int size() throws IOException;
}
