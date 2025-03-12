package me.cho.snackball.study.domain;

import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyManager> managers = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> members = new ArrayList<>();

    @Column(unique = true)
    private String path;

    private String title;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

//    @Lob @Basic(fetch = FetchType.EAGER)
//    private String image;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyStudyTag> studyStudyTags = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyLocation> studyLocations = new ArrayList<>();

    //추가
    private int limitOfEnrollment;

    private LocalDateTime publishedDate;

    private LocalDateTime closedDate;

    private LocalDateTime recruitingUpdatedDate;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public static Study createStudy(CreateStudyForm createStudyForm) {
        Study study = new Study();
        study.setTitle(createStudyForm.getTitle());
//        study.setShortDescription(createStudyForm.getShortDescription());
        study.setFullDescription(createStudyForm.getFullDescription());
        study.setLimitOfEnrollment(createStudyForm.getLimitOfEnrollment());
        return study;
    }

//    public boolean isManager(CustomUserDetails customUserDetails) {
//        User user = customUserDetails.getUser();
//        return this.managers.contains()
//    }
}
