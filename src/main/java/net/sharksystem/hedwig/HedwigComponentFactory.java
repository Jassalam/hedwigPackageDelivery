package net.sharksystem.hedwig;

import net.sharksystem.SharkComponentFactory;
import net.sharksystem.contactinformation.SharkContactInformationComponent;
import net.sharksystem.pki.SharkPKIComponent;

public class HedwigComponentFactory implements SharkComponentFactory {
    private final SharkPKIComponent pkiComponent;
    private final SharkContactInformationComponent contactsComponent;

    public HedwigComponentFactory(SharkPKIComponent pkiComponent, SharkContactInformationComponent contactsComponent){
        this.pkiComponent = pkiComponent;
        this.contactsComponent = contactsComponent;
    }

    public HedwigComponentFactory(SharkPKIComponent pkiComponent){this(pkiComponent, null);}

    @Override
    public HedwigComponentImpl getComponent(){return new HedwigComponentImpl(pkiComponent);}
}
