package me.cho.snackball.studyTag;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.studyTag.domain.StudyTag;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.cho.snackball.studyTag.domain.QStudyStudyTag.*;
import static me.cho.snackball.studyTag.domain.QStudyTag.*;

@Repository
public class StudyStudyTagQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyStudyTagQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<StudyTag> findStudyTagsByStudyId(Long studyId) {
        return queryFactory
                .select(studyTag)
                .from(studyStudyTag)
                .join(studyStudyTag.studyTag, studyTag) // TODO 패치조인 안하면 N + 1 문제 발생하는지 확인해보자 -> 발생 안 할 거라고 예상
                .where(studyStudyTag.study.id.eq(studyId))
                .fetch();
    }
}
