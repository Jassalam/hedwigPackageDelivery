package net.sharksystem.hedwig;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    HedwigAppTest.class,
    HedwigMessageSerializationTests.class,
    HedwigPackageDeliveryTest.class
})
public class HedwigV1TestSuite {
}
