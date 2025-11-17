package in.zeta.mapper;

import in.zeta.dto.requests.events.*;
import in.zeta.dto.response.*;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class EventMapper {

    public static NotificationResponse toNotificationResponse(NotificationCreatedEvent event) {
        return NotificationResponse.builder()
                .id(event.getNotificationId())
                .userId(event.getUserId())
                .verificationRequestId(event.getVerificationRequestId())
                .notificationType(event.getNotificationType())
                .message(event.getMessage())
                .createdAt(event.getCreatedAt())
                .isRead(false)
                .readAt(null)
                .build();
    }


    public static CommentResponse toCommentResponse(CommentCreatedEvent event, String userName) {
        return CommentResponse.builder()
                .commentAction(event.getCommentAction())
                .id(event.getCommentId())
                .verificationRequestId(event.getVerificationRequestId())
                .userName(userName)
                .commentText(event.getCommentText())
                .commentType(event.getCommentType())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public static CommentResponse toCommentResponseFromUpdate(CommentUpdatedEvent event, String userName) {
        return CommentResponse.builder()
                .commentAction(event.getCommentAction())
                .id(event.getCommentId())
                .verificationRequestId(event.getVerificationRequestId())
                .userName(userName)
                .commentText(event.getNewCommentText())
                .commentType(null)
                .createdAt(event.getUpdatedAt())
                .build();
    }

}