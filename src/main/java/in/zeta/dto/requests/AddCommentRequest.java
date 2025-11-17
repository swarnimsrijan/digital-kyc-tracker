package in.zeta.dto.requests;


import in.zeta.enums.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentRequest {
    @NotBlank(message = "Comment text is required")
    private String commentText;

    @NotNull(message = "Comment type is required")
    private CommentType commentType;

}