package me.cho.snackball.studyPost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.studyPost.domain.StudyPost;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
public class StudyPostDto extends BaseUserEntity {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime lastModifiedDate;

    public StudyPostDto(StudyPost studyPost) {
        this.id = studyPost.getId();
        this.nickname = studyPost.getUser().getNickname();
        this.title = studyPost.getTitle();
        this.content = studyPost.getContent();
        this.lastModifiedDate = studyPost.getLastModifiedDate();
    }
}
