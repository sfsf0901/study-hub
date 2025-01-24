package me.cho.snackball.study.dto;

import lombok.Data;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyManager;
import me.cho.snackball.study.domain.StudyMember;

import java.util.Set;

@Data
public class ViewManagersAndMembers {

    private Set<StudyManager> managers;

    private Set<StudyMember> members;

    public ViewManagersAndMembers(Study study) {
        this.managers = study.getManagers();
        this.members = study.getMembers();
    }
}
