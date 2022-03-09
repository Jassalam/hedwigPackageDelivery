package net.sharksystem.hedwig;

import net.sharksystem.asap.listenermanager.GenericListenerImplementation;
import net.sharksystem.asap.listenermanager.GenericNotifier;

public class HedwigMessageReceivedListenerManager extends GenericListenerImplementation<HedwigMessageReceivedListener> {

    public void addHedwigMessagesReceivedListener(HedwigMessageReceivedListener listener) {
        this.addListener(listener);
    }

    public void removeHedwigMessagesReceivedListener(HedwigMessageReceivedListener listener) {
        this.removeListener(listener);
    }

    protected void notifyHedwigMessageReceivedListener(
        CharSequence uri) {

        HedwigMessagesReceivedNotifier hedwigMessagesReceivedNotifier =
            new HedwigMessagesReceivedNotifier(uri);

        this.notifyAll(hedwigMessagesReceivedNotifier, false);
    }

    private class HedwigMessagesReceivedNotifier implements GenericNotifier<HedwigMessageReceivedListener> {
        private final CharSequence uri;

        public HedwigMessagesReceivedNotifier(CharSequence uri) {
            this.uri = uri;
        }


        @Override
        public void doNotify(HedwigMessageReceivedListener hedwigMessageReceivedListener) {
            hedwigMessageReceivedListener.hedwigMessagesReceived(this.uri);
        }
    }
}
