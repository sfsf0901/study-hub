package me.cho.snackball.study;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.study.domain.Study;
import org.springframework.stereotype.Repository;

import static me.cho.snackball.location.domain.QStudyLocation.*;
import static me.cho.snackball.study.domain.QStudy.*;
import static me.cho.snackball.studyTag.domain.QStudyStudyTag.*;

@Repository
public class StudyQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Study findStudyWithStudyStudyTagsAndStudyLocationsById(Long id) {
        return queryFactory
                .selectFrom(study)
                .join(study.studyStudyTags, studyStudyTag).fetchJoin()
                .join(study.studyLocations, studyLocation).fetchJoin()
                .where(study.id.eq(id))
                .fetchOne();
    }


}
