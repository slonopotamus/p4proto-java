package ru.bozaro.p4;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import ru.bozaro.p4.proto.Client;

import java.io.IOException;

/**
 * @author Marat Radchenko
 */
public final class P4HelpTest {

    @Test(dataProvider = "all", dataProviderClass = P4TesterDataProvider.class)
    void test(@NotNull P4TesterFactory factory) throws Exception {
        try (P4Tester tester = factory.createTester(); Client client = tester.connect()) {
            client.p4((message, severityHolder) -> {
                if (!"client-Message".equals(message.getFunc()))
                    throw new IOException("Unexpected call: " + message.getFunc());
                return null;
            }, "help");
        }
    }
}
