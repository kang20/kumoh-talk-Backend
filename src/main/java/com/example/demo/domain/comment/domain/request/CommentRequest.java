package com.example.demo.domain.comment.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    @NotBlank(message = "내용은 필수 항목입니다.")
    @Size(min = 1, max = 500, message = "최대 제한 500글자 입니다.")
    private String content;

    private Long groupId;

}
