package in.zeta.producer;

import com.google.gson.Gson;
import in.zeta.oms.atropos.client.AtroposPublisherClient;
import in.zeta.oms.atropos.model.PublishMode;
import in.zeta.oms.atropos.response.PublishEventResponse;
import olympus.common.JID;
import olympus.pubsub.PubSubMessagingService;
import olympus.pubsub.model.PubSubEvent;
import olympus.pubsub.model.TopicScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventProducerTest {

    private AtroposPublisherClient atroposPublisherClient;
    private Gson gson;
    private EventProducer eventProducer;

    @BeforeEach
    void setUp() {
        atroposPublisherClient = mock(AtroposPublisherClient.class);
        gson = new Gson();
        eventProducer = new EventProducer(atroposPublisherClient, gson, "KINESIS");
    }

    @Test
    void testPublishEventSuccess() throws Exception {
        // Arrange
        String eventType = "VerificationCreated";
        String objectId = "123";
        String topic = "verification";
        Map<String, Object> eventData = Map.of("id", 123, "status", "CREATED");

        PublishEventResponse mockResponse = mock(PublishEventResponse.class);
        CompletableFuture<PublishEventResponse> future = CompletableFuture.completedFuture(mockResponse);

        ArgumentCaptor<PubSubEvent.Builder> eventCaptor = ArgumentCaptor.forClass(PubSubEvent.Builder.class);

        when(atroposPublisherClient.publish(eventCaptor.capture(), eq(PublishMode.KINESIS)))
                .thenReturn(future);
        
        CompletionStage<PublishEventResponse> result =
                eventProducer.publishEvent(eventType, objectId, topic, eventData);

        assertNotNull(result);

        PubSubMessagingService mockPubSubService = mock(PubSubMessagingService.class);
        when(mockPubSubService.getInstanceJID()).thenReturn(mock(JID.class));
        when(mockPubSubService.getServiceName()).thenReturn("test-service");

        PubSubEvent.Builder builder = eventCaptor.getValue();
        builder.publisher(mockPubSubService);
        builder.origin(mockPubSubService);
        PubSubEvent builtEvent = builder.build();

        verify(atroposPublisherClient, times(1))
                .publish(any(), eq(PublishMode.KINESIS));
    }
    @Test
    void testPublishModeInvalid() {
        // Assert invalid publish mode triggers exception
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new EventProducer(atroposPublisherClient, gson, "INVALID_MODE")
        );

        assertTrue(ex.getMessage().contains("Invalid publish mode"));
    }
}
