package me.cho.snackball.study.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.location.domain.StudyLocation;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.studyTag.domain.StudyStudyTag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Study extends BaseUserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob @Basic(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private String fullDescription;

    private String thumbnailUrl;

    @Column(nullable = false)
    private int limitOfEnrollment;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyManager> managers = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyStudyTag> studyStudyTags = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyLocation> studyLocations = new ArrayList<>();

    //추가
    private LocalDateTime publishedDate;

    private LocalDateTime closedDate;

    private LocalDateTime recruitingUpdatedDate;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    public static Study createStudy(CreateStudyForm createStudyForm, String updatedDescription, String thumbnailUrl) {
        Study study = new Study();
        study.setTitle(createStudyForm.getTitle());
        study.setFullDescription(updatedDescription);
        study.setThumbnailUrl(thumbnailUrl);
        study.setLimitOfEnrollment(createStudyForm.getLimitOfEnrollment());
        return study;
    }

}
