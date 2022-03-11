package net.sharksystem.hedwig;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    HedwigAppTest.class,
    HedwigPackageDeliveryTest.class,
    HedwigMessageSerializationTests.class
})
public class HedwigV1TestSuite {
}
