package in.zeta.controller;

import in.zeta.constants.Messages;
import in.zeta.entity.Users;
import in.zeta.providers.CommentProvider;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.response.ApiResponse;
import in.zeta.dto.response.CommentResponse;
import in.zeta.service.CommentService;
import in.zeta.service.VerificationRequestService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenants/{tenantId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final VerificationRequestService verificationRequestService;
    private final SpectraLogger logger = OlympusSpectra.getLogger(CommentController.class);

    @PostMapping("verification/{verificationId}/add")
    @SandboxAuthorizedSync(action = "comment.create", object = "$$verificationId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable UUID verificationId,
            @Valid @RequestBody AddCommentRequest addCommentRequest) {

        Users user = verificationRequestService
                .getUserByVerificationRequest(verificationId);

        logger.info("Adding comment to verification request:")
                .attr(Messages.Keys.VERIFICATION_ID, verificationId)
                .attr(Messages.Keys.USER_ID, user.getId())
                .log();

        CommentResponse response = commentService
                .addCommentToRequest(verificationId, addCommentRequest);

        logger.info("Comment added to verification request:")
                .attr(Messages.Keys.VERIFICATION_ID, verificationId)
                .attr(Messages.Keys.USER_ID, user.getId())
                .log();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Messages.Comment.COMMENT_ADDED, response));
    }

    @GetMapping("customer/{customerId}/read")
    @SandboxAuthorizedSync(action = "comment.read", object = "$$customerId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readCommentsToCustomer(
            @PathVariable UUID customerId) {

        logger.info(Messages.Comment.READING_COMMENTS_FOR_CUSTOMER)
                .attr("customerId:", customerId)
                .log();

        List<CommentResponse> responses = commentService
                .readCommentsToCustomer(customerId);

        logger.info("Comments retrieved for customer:")
                .attr("customerId:", customerId)
                .attr("commentCount:", responses.size())
                .log();

        return ResponseEntity.ok(
                ApiResponse.success(Messages.Comment.COMMENT_RETRIEVED_BY_ID, responses));
    }

    @GetMapping("{commentId}/user/{userId}/read")
    @SandboxAuthorizedSync(action = "comment.create", object = "$$userId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<CommentResponse>> readCommentById(
            @PathVariable UUID commentId,
            @PathVariable UUID userId) {

        logger.info(Messages.Comment.READING_COMMENT_BY_ID)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .log();

        CommentResponse response = commentService
                .readCommentById(commentId, userId);

        logger.info(Messages.Comment.COMMENT_RETRIEVED_BY_ID)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .log();

        return ResponseEntity.ok(
                ApiResponse.success(Messages.Comment.COMMENT_RETRIEVED_BY_ID, response));
    }

    @PutMapping("{commentId}/user/{userId}/update")
    @SandboxAuthorizedSync(action = "comment.update", object = "$$userId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @PathVariable UUID userId,
            @Valid @RequestBody AddCommentRequest addCommentRequest) {

        logger.info(Messages.Comment.UPDATING_COMMENT)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .attr(Messages.Keys.USER_ID, userId)
                .log();

        CommentResponse response = commentService
                .updateComment(commentId, addCommentRequest, userId);

        logger.info(Messages.Comment.COMMENT_UPDATED)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .attr(Messages.Keys.USER_ID, userId)
                .log();

        return ResponseEntity.ok(
                ApiResponse.success(Messages.Comment.COMMENT_UPDATED, response));
    }

    @DeleteMapping("{commentId}/officer/{officerId}/delete")
    @SandboxAuthorizedSync(action = "comment.delete", object = "$$officerId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<CommentResponse>> deleteComment(
            @PathVariable UUID commentId,
            @PathVariable UUID officerId) {

        logger.info(Messages.Comment.DELETING_COMMENT)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .attr(Messages.Keys.OFFICER_ID, officerId)
                .log();

        CommentResponse response = commentService
                .deleteComment(commentId, officerId);

        logger.info(Messages.Comment.COMMENT_DELETED)
                .attr(Messages.Keys.COMMENT_ID, commentId)
                .attr(Messages.Keys.OFFICER_ID, officerId)
                .log();

        return ResponseEntity.ok(
                ApiResponse.success(Messages.Comment.COMMENT_DELETED, response));
    }

    @GetMapping("officer/{officerId}/read")
    @SandboxAuthorizedSync(action = "comment.read", object = "$$officerId$$@" + CommentProvider.OBJECT_TYPE + ".cipher.app", tenantID = "$$tenants$$")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readCommentsByOfficer(
            @PathVariable UUID officerId) {

        logger.info(Messages.Comment.READING_COMMENT_BY_ID)
                .attr(Messages.Keys.OFFICER_ID, officerId)
                .log();

        List<CommentResponse> responses = commentService
                .readCommentsByOfficer(officerId);

        logger.info(Messages.Comment.COMMENTS_RETRIEVED_BY_OFFICER)
                .attr(Messages.Keys.OFFICER_ID, officerId)
                .attr(Messages.Keys.COMMENT_COUNT, responses.size())
                .log();
        return ResponseEntity.ok(
                ApiResponse.success(Messages.Comment.COMMENTS_RETRIEVED_BY_OFFICER, responses));
    }
}
