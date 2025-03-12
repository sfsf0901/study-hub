package me.cho.snackball.study.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.user.domain.User;

@Getter
@RequiredArgsConstructor
public class EnrollmentAcceptEvent {

    private final Study study;
    private final User user;
}
