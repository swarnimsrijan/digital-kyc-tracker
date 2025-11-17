package in.zeta.service;

import in.zeta.dto.requests.events.CommentCreatedEvent;
import in.zeta.dto.requests.events.CommentDeletedEvent;
import in.zeta.dto.requests.events.CommentUpdatedEvent;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.response.CommentResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse addCommentToRequest(UUID verificationId, @Valid AddCommentRequest addCommentRequest);
    List<CommentResponse> readCommentsByOfficer(UUID officerId);
    List<CommentResponse> readCommentsToCustomer(UUID customerId);
    CommentResponse updateComment(UUID commentId, @Valid AddCommentRequest addCommentRequest, UUID userId);
    CommentResponse updateCommentFromEvent(CommentUpdatedEvent commentUpdatedEvent);
    CommentResponse deleteComment(UUID commentId, UUID officerId);
    CommentResponse deleteCommentFromEvent(CommentDeletedEvent commentDeletedEvent);
    CommentResponse readCommentById(UUID commentId, UUID userId);
    void saveComment(CommentCreatedEvent commentCreatedEvent);
    CommentResponse getCommentById(UUID commentId);
    void processCommentEvent(String eventPayload);
}
