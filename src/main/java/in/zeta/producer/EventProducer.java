package in.zeta.producer;


import com.google.gson.Gson;
import in.zeta.oms.atropos.client.AtroposPublisherClient;
import in.zeta.oms.atropos.model.PublishMode;
import in.zeta.oms.atropos.response.PublishEventResponse;
import in.zeta.spectra.capture.SpectraLogger;
import olympus.pubsub.model.OperationType;
import olympus.pubsub.model.PubSubEvent;
import olympus.pubsub.model.TopicScope;
import olympus.trace.OlympusSpectra;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class EventProducer {

    private static final SpectraLogger logger = OlympusSpectra.getLogger(EventProducer.class);
    private final PublishMode publishMode;
    private final AtroposPublisherClient atroposPublisherClient;
    private final Gson gson;

    public EventProducer(
            AtroposPublisherClient atroposPublisherClient,
            Gson gson,
            @Value("${atropos.publish.mode}") String publishModeString
    ) {
        this.atroposPublisherClient = atroposPublisherClient;
        this.gson = gson;

        try {
            this.publishMode = PublishMode.valueOf(publishModeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid publish mode: " + publishModeString).log();
            throw new IllegalArgumentException("Invalid publish mode: " + publishModeString, e);
        } catch (Exception e) {
            logger.error("Unexpected error while getting publish mode: " + e.getMessage(), e).log();
            throw new RuntimeException("Unexpected error while getting publish mode", e);
        }
    }

    public <T> CompletionStage<PublishEventResponse> publishEvent(
            String eventType,
            String objectId,
            String topic,
            T eventData
    ){
        logger.info("Publishing event:")
                .attr("eventType", eventType)
                .attr("objectId", objectId)
                .attr("topic", topic)
                .log();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Map<String, Object> eventDataMap = objectMapper.convertValue(
                eventData,
                new TypeReference<Map<String, Object>>() {}
        );

        logger.info("Event data converted to map:")
                .attr("eventDataMap", eventDataMap)
                .log();

        PubSubEvent.Builder builder = buildEvent(objectId, topic, TopicScope.SYSTEM, eventDataMap);

        logger.info("Built PubSubEvent:")
//                .attr("event", builder.toString())
                .log();



        return atroposPublisherClient.publish(builder, publishMode);

    }


    private PubSubEvent.Builder buildEvent(String objectId,
                                           String topic,
                                           TopicScope topicScope,
                                           Map<String, Object> eventData) {

        logger.info("Building PubSubEvent:")
                .attr("objectId", objectId)
                .attr("topic", topic)
                .attr("topicScope", topicScope)
                .attr("eventData", eventData)
                .log();

        logger.info("Serializing event data to JSON tree...")
                .attr("eventData", gson.toJsonTree(eventData))
                .log();

        return new PubSubEvent.Builder()
                .tenant("0")
                .topicScope(topicScope)
                .objectType(topic)
                .objectID(objectId)
                .operationType(OperationType.CREATED)
                .sourceAttributes(new NameValuePair[0])
                .tags(List.of())
                .stateMachineState("default")
                .data(gson.toJsonTree(eventData));
    }

}
