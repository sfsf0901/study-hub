package me.cho.snackball.study.event;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.study.domain.Study;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyCreateEvent {

    private final Study study;
}
