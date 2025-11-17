package in.zeta.config;

import in.zeta.config.AtroposPublisherConfig;
import in.zeta.oms.atropos.client.AtroposPublisherClient;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AtroposPublisherConfigTest {

    @Test
    void atroposPublisherClientBean_isPresentAndNotNull() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(AtroposPublisherConfig.class);
            ctx.refresh();

            AtroposPublisherClient client = ctx.getBean(AtroposPublisherClient.class);
            assertNotNull(client);
        }
    }

    @Test
    void atroposPublisherClientBean_isSingleton() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(AtroposPublisherConfig.class);
            ctx.refresh();

            AtroposPublisherClient first = ctx.getBean(AtroposPublisherClient.class);
            AtroposPublisherClient second = ctx.getBean(AtroposPublisherClient.class);
            assertSame(first, second);
        }
    }
}