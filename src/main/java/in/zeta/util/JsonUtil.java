package in.zeta.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import in.zeta.dto.requests.events.*;
import in.zeta.exception.JsonParsingException;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.NoArgsConstructor;
import olympus.trace.OlympusSpectra;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JsonUtil {
    private static final SpectraLogger log = OlympusSpectra.getLogger(JsonUtil.class);

    // Custom formatter that handles nanoseconds
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter();

    private static final ObjectMapper mapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(FLEXIBLE_FORMATTER)
        );
        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        return new ObjectMapper()
                .registerModule(javaTimeModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    public static AuditLogCreatedEvent parseAuditLogCreatedEvent(String json) {
        try {
            log.debug("Parsing AuditLogCreatedEvent from JSON");
            return mapper.readValue(json, AuditLogCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AuditLogCreatedEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse AuditLogCreatedEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing AuditLogCreatedEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing AuditLogCreatedEvent", e);
        }
    }

    public static NotificationCreatedEvent parseNotificationCreatedEvent(String json) {
        try {
            log.debug("Parsing NotificationCreatedEvent from JSON");
            return mapper.readValue(json, NotificationCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse NotificationCreatedEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse NotificationCreatedEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing NotificationCreatedEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing NotificationCreatedEvent", e);
        }
    }

    public static CommentCreatedEvent parseCommentCreatedEvent(String json) {
        try {
            log.debug("Parsing CommentCreatedEvent from JSON");
            return mapper.readValue(json, CommentCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse CommentCreatedEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse CommentCreatedEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing CommentCreatedEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing CommentCreatedEvent", e);
        }
    }

    public static CommentUpdatedEvent parseCommentUpdatedEvent(String json) {
        try {
            log.debug("Parsing CommentUpdatedEvent from JSON")
                    .log();
            return mapper.readValue(json, CommentUpdatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse CommentUpdatedEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse CommentUpdatedEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing CommentUpdatedEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing CommentUpdatedEvent", e);
        }
    }

    public static CommentDeletedEvent parseCommentDeletedEvent(String json) {
        try {
            log.debug("Parsing CommentDeletedEvent from JSON");
            return mapper.readValue(json, CommentDeletedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse CommentDeletedEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse CommentDeletedEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing CommentDeletedEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing CommentDeletedEvent", e);
        }
    }

    public static StatusUpdateEvent parseStatusUpdateEvent(String json) {
        try {
            log.debug("Parsing StatusUpdateEvent from JSON");
            return mapper.readValue(json, StatusUpdateEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse StatusUpdateEvent")
                    .attr("error", e.getMessage())
                    .attr("json", json)
                    .log();
            throw new JsonParsingException("Failed to parse StatusUpdateEvent payload", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing StatusUpdateEvent")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Unexpected error while parsing StatusUpdateEvent", e);
        }
    }

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Failed to convert object to JSON string", e);
        }
    }

    public static String toPrettyJson(Object object) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to pretty JSON")
                    .attr("error", e.getMessage())
                    .log();
            throw new JsonParsingException("Failed to convert object to pretty JSON string", e);
        }
    }

    public static boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON")
                    .attr("error", e.getMessage())
                    .log();
            return false;
        }
    }
}