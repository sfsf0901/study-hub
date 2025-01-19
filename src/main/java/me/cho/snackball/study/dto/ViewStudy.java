package me.cho.snackball.study.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import me.cho.snackball.study.domain.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class ViewStudy {

    private Long id;

    private Set<StudyManager> managers;

    private Set<StudyMember> members;

    private String title;

    private Set<StudyStudyTag> studyStudyTags;

    private Set<StudyLocation> studyLocations;

    private String shortDescription;

    private String fullDescription;

    private LocalDateTime publishedDate;

    private LocalDateTime closedDate;

    private LocalDateTime recruitingUpdatedDate;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public ViewStudy(Study study) {
        this.id = study.getId();
        this.managers = study.getManagers();
        this.members = study.getMembers();
        this.title = study.getTitle();
        this.studyStudyTags = study.getStudyStudyTags();
        this.studyLocations = study.getStudyLocations();
        this.shortDescription = study.getShortDescription();
        this.fullDescription = study.getFullDescription();
        this.publishedDate = study.getPublishedDate();
        this.closedDate = study.getClosedDate();
        this.recruitingUpdatedDate = study.getRecruitingUpdatedDate();
        this.recruiting = study.isRecruiting();
        this.published = study.isPublished();
        this.closed = study.isClosed();
        this.useBanner = study.isUseBanner();
    }
}
