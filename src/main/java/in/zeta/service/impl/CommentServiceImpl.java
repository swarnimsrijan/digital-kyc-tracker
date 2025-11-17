package in.zeta.service.impl;

import in.zeta.entity.Comment;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import in.zeta.enums.AuditAction;
import in.zeta.enums.EntityType;
import in.zeta.dto.requests.events.AuditLogCreatedEvent;
import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.requests.events.CommentDeletedEvent;
import in.zeta.dto.requests.events.CommentUpdatedEvent;
import in.zeta.exception.InvalidOperationException;
import in.zeta.exception.ResourceNotFoundException;
import in.zeta.producer.EventProducer;
import in.zeta.repository.CommentRepository;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.response.CommentResponse;
import in.zeta.service.*;
import in.zeta.spectra.capture.SpectraLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static in.zeta.mapper.AuditLogMapper.createAuditLogEvent;
import static in.zeta.mapper.CommentMapper.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final VerificationRequestService verificationRequestService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(CommentServiceImpl.class);
    private final EventProducer eventProducer;
    private final UserService userService;

    @Value("${atropos.comment.topic}")
    private String commentTopic;

    @Override
    public CommentResponse addCommentToRequest(UUID verificationId, @Valid AddCommentRequest addCommentRequest) {
        VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(verificationId);
        Users user = verificationRequest.getAssignedOfficer();

        logger.info("Publishing comment created event")
                .attr("verificationId", verificationId)
                .attr("userId", user.getId())
                .attr("commentText", addCommentRequest.getCommentText())
                .log();

        CommentCreatedEvent event = CommentCreatedEvent.builder()
                .commentAction(AuditAction.COMMENT_ADDED)
                .commentId(UUID.randomUUID())
                .verificationRequestId(verificationId)
                .createdBy(user.getId())
                .commentText(addCommentRequest.getCommentText())
                .commentType(addCommentRequest.getCommentType())
                .createdAt(LocalDateTime.now())
                .build();

        eventProducer.publishEvent(
                EntityType.COMMENT.toString(),
                event.getCommentId().toString(),
                commentTopic,
                event
        );

        return CommentResponse.builder()
                .commentAction(event.getCommentAction())
                .id(event.getCommentId())
                .verificationRequestId(verificationId)
                .userName(user.getUsername())
                .commentText(addCommentRequest.getCommentText())
                .commentType(addCommentRequest.getCommentType())
                .createdAt(event.getCreatedAt())
                .build();
    }

    @Override
    public CommentResponse updateComment(UUID commentId, @Valid AddCommentRequest addCommentRequest, UUID userId) {
        Users user = userService.getByIdOrThrow(userId, "User not found with ID: " + userId);
        Comment comment = getComment(commentId);

        CommentUpdatedEvent event = CommentUpdatedEvent.builder()
                .commentAction(AuditAction.COMMENT_UPDATED)
                .commentId(commentId)
                .verificationRequestId(comment.getVerificationRequest().getId())
                .updatedBy(user.getId())
                .oldCommentText(null)
                .newCommentText(addCommentRequest.getCommentText())
                .updatedAt(LocalDateTime.now())
                .build();

        eventProducer.publishEvent(EntityType.COMMENT.toString(), commentId.toString(), commentTopic, event);

        return CommentResponse.builder()
                .commentAction(event.getCommentAction())
                .id(commentId)
                .commentText(addCommentRequest.getCommentText())
                .commentType(addCommentRequest.getCommentType())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public CommentResponse deleteComment(UUID commentId, UUID officerId) {
        Users user = userService.getByIdOrThrow(officerId, "User not found with ID: " + officerId);
        Comment comment = getComment(commentId);

        CommentDeletedEvent event = CommentDeletedEvent.builder()
                .commentAction(AuditAction.COMMENT_DELETED)
                .commentId(commentId)
                .verificationRequestId(comment.getVerificationRequest().getId())
                .deletedBy(user.getId())
                .commentText(null)
                .deletedAt(LocalDateTime.now())
                .build();

        eventProducer.publishEvent(EntityType.COMMENT.toString(), commentId.toString(), commentTopic, event);


        return CommentResponse.builder()
                .commentAction(event.getCommentAction())
                .id(commentId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public CommentResponse readCommentById(UUID commentId, UUID userId) {
        Comment comment = getComment(commentId);
        Users user = userService.getByIdOrThrow(userId, "Customer not found with ID: " + userId);

        if(!comment.getVerificationRequest().getCustomer().getId().equals(userId)){
            throw new InvalidOperationException("User not authorized to view this comment " + commentId);
        }

        comment.setRead(true);
        commentRepository.save(comment);

        logger.info("Reading comment by ID")
                .attr("commentId", commentId)
                .attr("userId", userId)
                .log();
        //
        AuditLogCreatedEvent event = createAuditLogEvent(
                EntityType.COMMENT,
                comment.getId(),
                AuditAction.COMMENT_VIEWED,
                user,
                null,
                String.format("Comment Read successfully: %s", comment.getId())
        );

        auditService.publishAuditLogEvent(event);

        logger.info("Audit log created for comment read")
                .attr("commentId", commentId)
                .attr("userId", userId)
                .log();

        return toCommentResponse(comment, true);
    }

    @Override
    public List<CommentResponse> readCommentsByOfficer(UUID officerId) {

        logger.info("Reading comments for officer")
                .attr("officerId", officerId)
                .log();

        List<Comment> comments = commentRepository.findByVerificationRequestAssignedOfficerId(officerId);

        logger.info("Comments retrieved for officer")
                .attr("officerId", officerId)
                .attr("commentCount", comments.size())
                .log();

        return mapCommentsToResponses(comments);
    }

    @Override
    public List<CommentResponse> readCommentsToCustomer(UUID customerId) {

        logger.info("Reading comments for customer")
                .attr("customerId", customerId)
                .log();

        List<Comment> comments = commentRepository.findByVerificationRequestCustomerId(customerId);

        logger.info("Comments retrieved for customer")
                .attr("customerId", customerId)
                .attr("commentCount", comments.size())
                .log();

        return mapCommentsToResponses(comments);
    }

    @Override
    public CommentResponse updateCommentFromEvent(CommentUpdatedEvent commentUpdatedEvent) {
        Comment comment = getComment(commentUpdatedEvent.getCommentId());
        comment.setCommentText(commentUpdatedEvent.getNewCommentText());
        commentRepository.save(comment);
        return toCommentResponse(comment, false);
    }

    @Override
    public CommentResponse deleteCommentFromEvent(CommentDeletedEvent commentDeletedEvent) {
        Comment comment = getComment(commentDeletedEvent.getCommentId());
        commentRepository.delete(comment);

        return CommentResponse.builder()
                .id(commentDeletedEvent.getCommentId())
                .createdAt(commentDeletedEvent.getDeletedAt())
                .build();
    }

    private List<CommentResponse> mapCommentsToResponses(List<Comment> comments) {
        return comments.stream().map(comment -> toCommentResponse(comment, comment.isRead())).toList();
    }

    private Comment getComment(UUID commentId) {
        logger.info("Fetching comment with ID")
                .attr("commentId", commentId)
                .log();
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
    }

    @Override
    public void saveComment(CommentCreatedEvent commentCreatedEvent) {
        Users user = userService.getByIdOrThrow(commentCreatedEvent.getCreatedBy(), "User not found with ID: " + commentCreatedEvent.getCreatedBy());
        VerificationRequest verificationRequest = verificationRequestService.getByIdOrThrow(commentCreatedEvent.getVerificationRequestId());
        Comment comment = mapToCommentEntity(commentCreatedEvent, user, verificationRequest);
        commentRepository.save(comment);
    }


    @Override
    public CommentResponse getCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        return toCommentResponse(comment, true);
    }

    @Override
    public void processCommentEvent(String eventPayload){
        return;
    }
}
