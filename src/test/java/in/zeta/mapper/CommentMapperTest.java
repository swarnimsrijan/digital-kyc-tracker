package in.zeta.mapper;

import in.zeta.entity.Comment;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.CommentType;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.response.CommentResponse;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void createComment_success() {
        VerificationRequest verificationRequest = createTestVerificationRequest();
        AddCommentRequest request = createTestAddCommentRequest();
        Users user = createTestUser();

        Comment comment = CommentMapper.createComment(verificationRequest, request, user);

        assertNotNull(comment);
        assertEquals(verificationRequest, comment.getVerificationRequest());
        assertEquals(request.getCommentText(), comment.getCommentText());
        assertEquals(request.getCommentType(), comment.getCommentType());
        assertEquals(user, comment.getCreatedBy());
        assertNotNull(comment.getCreatedAt());
    }

    @Test
    void toCommentResponse_success() {
        Comment comment = createTestComment();

        CommentResponse response = CommentMapper.toCommentResponse(comment, comment.isRead());

        assertNotNull(response);
        assertEquals(comment.getId(), response.getId());
        assertEquals(comment.getVerificationRequest().getId(), response.getVerificationRequestId());
        assertEquals(comment.getCreatedBy().getUsername(), response.getUserName());
        assertEquals(comment.isRead(), response.getIsRead());
        assertEquals(comment.getCommentText(), response.getCommentText());
        assertEquals(comment.getCommentType(), response.getCommentType());
        assertEquals(comment.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void mapToCommentEntity_success() {
        CommentCreatedEvent event = createTestCommentCreatedEvent();
        Users user = createTestUser();
        VerificationRequest verificationRequest = createTestVerificationRequest();

        Comment comment = CommentMapper.mapToCommentEntity(event, user, verificationRequest);

        assertNotNull(comment);
        assertEquals(event.getCommentId(), comment.getId());
        assertEquals(verificationRequest, comment.getVerificationRequest());
        assertEquals(user, comment.getCreatedBy());
        assertEquals(event.getCommentText(), comment.getCommentText());
        assertEquals(event.getCommentType(), comment.getCommentType());
        assertEquals(event.getCreatedAt(), comment.getCreatedAt());
    }

    private VerificationRequest createTestVerificationRequest() {
        return VerificationRequest.builder()
                .id(UUID.randomUUID())
                .build();
    }

    private AddCommentRequest createTestAddCommentRequest() {
        return AddCommentRequest.builder()
                .commentText("Test comment")
                .commentType(CommentType.OFFICER_NOTE)
                .build();
    }

    private Users createTestUser() {
        return Users.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .build();
    }

    private Comment createTestComment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .verificationRequest(createTestVerificationRequest())
                .createdBy(createTestUser())
                .commentText("Test comment")
                .commentType(CommentType.OFFICER_NOTE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CommentCreatedEvent createTestCommentCreatedEvent() {
        return CommentCreatedEvent.builder()
                .commentId(UUID.randomUUID())
                .commentText("Test comment")
                .commentType(CommentType.OFFICER_NOTE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}