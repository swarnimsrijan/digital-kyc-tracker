package in.zeta.mapper;

import in.zeta.entity.Comment;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.response.CommentResponse;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment createComment(VerificationRequest verificationRequest, AddCommentRequest addCommentRequest, Users user) {
        return Comment.builder()
                .verificationRequest(verificationRequest)
                .commentText(addCommentRequest.getCommentText())
                .commentType(addCommentRequest.getCommentType())
                .createdAt(LocalDateTime.now())
                .createdBy(user)
                .build();
    }

    public static CommentResponse toCommentResponse(Comment comment, boolean isRead) {
        return CommentResponse.builder()
                .id(comment.getId())
                .verificationRequestId(comment.getVerificationRequest().getId())
                .userName(comment.getCreatedBy().getUsername())
                .commentText(comment.getCommentText())
                .commentType(comment.getCommentType())
                .createdAt(comment.getCreatedAt())
                .isRead(isRead)
                .build();
    }

    public static Comment mapToCommentEntity(CommentCreatedEvent event, Users createdBy, VerificationRequest verificationRequest) {
        Comment comment = new Comment();
        comment.setId(event.getCommentId());
        comment.setVerificationRequest(verificationRequest);
        comment.setCreatedBy(createdBy);
        comment.setCommentText(event.getCommentText());
        comment.setCommentType(event.getCommentType());
        comment.setCreatedAt(event.getCreatedAt());
        return comment;
    }

    private CommentMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
}
