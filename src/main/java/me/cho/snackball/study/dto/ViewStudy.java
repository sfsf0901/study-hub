package me.cho.snackball.study.dto;

import lombok.Data;
import me.cho.snackball.location.domain.StudyLocation;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.studyTag.domain.StudyStudyTag;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ViewStudy {

    private Long id;

    private List<StudyManager> managers;

    private List<StudyMember> members;

    private String title;

    private List<StudyStudyTag> studyStudyTags;

    private List<StudyLocation> studyLocations;

    private String shortDescription;

    private String fullDescription;

    private int limitOfEnrollment;

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
//        this.shortDescription = study.getShortDescription();
        this.fullDescription = study.getFullDescription();
        this.limitOfEnrollment = study.getLimitOfEnrollment();
        this.publishedDate = study.getPublishedDate();
        this.closedDate = study.getClosedDate();
        this.recruitingUpdatedDate = study.getRecruitingUpdatedDate();
        this.recruiting = study.isRecruiting();
        this.published = study.isPublished();
        this.closed = study.isClosed();
        this.useBanner = study.isUseBanner();
    }
}
