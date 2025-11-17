package in.zeta.service.impl;

import in.zeta.entity.Comment;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.AuditAction;
import in.zeta.enums.CommentType;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.requests.events.CommentDeletedEvent;
import in.zeta.dto.requests.events.CommentUpdatedEvent;
import in.zeta.dto.response.CommentResponse;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.CommentRepository;
import in.zeta.service.AuditService;
import in.zeta.service.UserService;
import in.zeta.service.VerificationRequestService;
import in.zeta.spectra.capture.SpectraLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private VerificationRequestService verificationRequestService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private SpectraLogger logger;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID testUserId;
    private UUID testCommentId;
    private UUID testVerificationId;
    private Users testUser;
    private Comment testComment;
    private VerificationRequest testVerificationRequest;
    private AddCommentRequest testAddCommentRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();
        testVerificationId = UUID.randomUUID();

        testUser = Users.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .build();

        testVerificationRequest = VerificationRequest.builder()
                .id(testVerificationId)
                .assignedOfficer(testUser)
                .customer(testUser)
                .build();

        testComment = Comment.builder()
                .id(testCommentId)
                .commentText("Test comment")
                .commentType(CommentType.GENERAL)
                .verificationRequest(testVerificationRequest)
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        testAddCommentRequest = AddCommentRequest.builder()
                .commentText("Test comment")
                .commentType(CommentType.GENERAL)
                .build();

        ReflectionTestUtils.setField(commentService, "commentTopic", "comment-topic");
    }

    @Test
    void addCommentToRequest_Success() {
        // Given
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);

        // When
        CommentResponse result = commentService.addCommentToRequest(testVerificationId, testAddCommentRequest);

        // Then
        assertNotNull(result);
        assertEquals(testAddCommentRequest.getCommentText(), result.getCommentText());
        assertEquals(AuditAction.COMMENT_ADDED, result.getCommentAction());
        verify(eventProducer).publishEvent(anyString(), anyString(), eq("comment-topic"), any());
    }

    @Test
    void updateComment_Success() {
        // Given
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When
        CommentResponse result = commentService.updateComment(testCommentId, testAddCommentRequest, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testAddCommentRequest.getCommentText(), result.getCommentText());
        assertEquals(AuditAction.COMMENT_UPDATED, result.getCommentAction());
        verify(eventProducer).publishEvent(anyString(), anyString(), eq("comment-topic"), any());
    }

    @Test
    void deleteComment_Success() {
        // Given
        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When
        CommentResponse result = commentService.deleteComment(testCommentId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(AuditAction.COMMENT_DELETED, result.getCommentAction());
        verify(eventProducer).publishEvent(anyString(), anyString(), eq("comment-topic"), any());
    }

    @Test
    void readCommentById_Success() {
        // Given
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        when(userService.getByIdOrThrow(testUserId, "Customer not found with ID: " + testUserId))
                .thenReturn(testUser);

        // When
        CommentResponse result = commentService.readCommentById(testCommentId, testUserId);

        // Then
        assertNotNull(result);
        verify(auditService).publishAuditLogEvent(any());
    }

    @Test
    void readCommentById_UnauthorizedUser() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        Users unauthorizedUser = Users.builder().id(unauthorizedUserId).build();

        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        when(userService.getByIdOrThrow(unauthorizedUserId, "Customer not found with ID: " + unauthorizedUserId))
                .thenReturn(unauthorizedUser);

        // When & Then
        assertThrows(InvalidOperationException.class,
                () -> commentService.readCommentById(testCommentId, unauthorizedUserId));
    }

    @Test
    void readCommentsByOfficer_Success() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByVerificationRequestAssignedOfficerId(testUserId))
                .thenReturn(comments);

        // When
        List<CommentResponse> result = commentService.readCommentsByOfficer(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void readCommentsToCustomer_Success() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByVerificationRequestCustomerId(testUserId))
                .thenReturn(comments);

        // When
        List<CommentResponse> result = commentService.readCommentsToCustomer(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateCommentFromEvent_Success() {
        // Given
        CommentUpdatedEvent event = CommentUpdatedEvent.builder()
                .commentId(testCommentId)
                .newCommentText("Updated comment")
                .build();

        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        CommentResponse result = commentService.updateCommentFromEvent(event);

        // Then
        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteCommentFromEvent_Success() {
        // Given
        CommentDeletedEvent event = CommentDeletedEvent.builder()
                .commentId(testCommentId)
                .deletedAt(LocalDateTime.now())
                .build();

        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When
        CommentResponse result = commentService.deleteCommentFromEvent(event);

        // Then
        assertNotNull(result);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void saveComment_Success() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
                .commentId(testCommentId)
                .verificationRequestId(testVerificationId)
                .createdBy(testUserId)
                .commentText("Test comment")
                .commentType(CommentType.GENERAL)
                .build();

        when(userService.getByIdOrThrow(testUserId, "User not found with ID: " + testUserId))
                .thenReturn(testUser);
        when(verificationRequestService.getByIdOrThrow(testVerificationId))
                .thenReturn(testVerificationRequest);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // When
        commentService.saveComment(event);

        // Then
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void getCommentById_Success() {
        // Given
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        // When
        CommentResponse result = commentService.getCommentById(testCommentId);

        // Then
        assertNotNull(result);
    }

    @Test
    void getCommentById_NotFound() {
        // Given
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentById(testCommentId));
    }
}