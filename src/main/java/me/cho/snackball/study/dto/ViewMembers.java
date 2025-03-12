package me.cho.snackball.study.dto;

import lombok.Data;
import me.cho.snackball.study.domain.*;

import java.util.List;

@Data
public class ViewMembers {

    private List<String> managerNicknames;

    private List<String> memberNicknames;

    public ViewMembers(Study study) {
        this.managerNicknames = study.getManagers().stream()
                .map(manager -> manager.getUser().getNickname())
                .toList();
        this.memberNicknames = study.getMembers().stream()
                .map(member -> member.getUser().getNickname())
                .toList();

    }
}
