package me.cho.snackball.study.domain;

import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.study.dto.CreateStudyForm;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Study extends BaseUserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyManager> managers = new HashSet<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyMember> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyStudyTag> studyStudyTags = new HashSet<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyLocation> studyLocations = new HashSet<>();

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
        return study;
    }

//    public boolean isManager(CustomUserDetails customUserDetails) {
//        User user = customUserDetails.getUser();
//        return this.managers.contains()
//    }
}
