package in.zeta.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.dto.requests.AddCommentRequest;
import in.zeta.dto.response.CommentResponse;
import in.zeta.entity.Users;
import in.zeta.enums.CommentType;
import in.zeta.service.CommentService;
import in.zeta.service.VerificationRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private VerificationRequestService verificationRequestService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID verificationId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID commentId = UUID.randomUUID();
    private final UUID officerId = UUID.randomUUID();

    @Test
    void testAddComment_Success() throws Exception {
        AddCommentRequest request = AddCommentRequest.builder()
                .commentText("Test comment content")
                .commentType(CommentType.GENERAL)
                .build();

        Users mockUser = new Users();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        CommentResponse expectedResponse = CommentResponse.builder()
                .id(commentId)
                .commentText("Test comment content")
                .userName("testuser")
                .verificationRequestId(verificationId)
                .createdAt(LocalDateTime.now())
                .build();

        when(verificationRequestService.getUserByVerificationRequest(verificationId))
                .thenReturn(mockUser);
        when(commentService.addCommentToRequest(eq(verificationId), any(AddCommentRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/tenants/{tenantId}/comment/verification/{verificationId}/add",
                        tenantId, verificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentText").value("Test comment content"))
                .andExpect(jsonPath("$.data.userName").value("testuser"));

        verify(verificationRequestService).getUserByVerificationRequest(verificationId);
        verify(commentService).addCommentToRequest(eq(verificationId), any(AddCommentRequest.class));
    }
}