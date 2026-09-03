package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateCommunityPostDTO {
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 120, message = "帖子标题不能超过120个字符")
    private String title;

    @NotBlank(message = "帖子正文不能为空")
    @Size(max = 5000, message = "帖子正文不能超过5000个字符")
    private String content;

    private List<@Size(max = 500, message = "图片地址不合法") String> imageUrls = new ArrayList<>();

    private List<Long> bookIds = new ArrayList<>();
}
