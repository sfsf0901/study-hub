package me.cho.snackball.studyTag.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.study.domain.Study;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyStudyTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_tag_id")
    private StudyTag studyTag;

    private String name;


    //==생성 메서드==//
    public static StudyStudyTag create(Study study, StudyTag studyTag) {
        StudyStudyTag studyStudyTag = new StudyStudyTag();
        studyStudyTag.study = study;
        studyStudyTag.studyTag = studyTag;
        studyStudyTag.name = studyTag.getName();
        study.getStudyStudyTags().add(studyStudyTag);
        return studyStudyTag;
    }
}
