package me.cho.snackball.location;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.location.domain.QStudyLocation;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.cho.snackball.location.domain.QStudyLocation.*;


@Repository
public class StudyLocationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyLocationQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Location> findLocationByStudyId(Long studyId) {
        return queryFactory
                .select(studyLocation.location)
                .from(studyLocation)
                .join(studyLocation.location) // TODO 없으면 N + 1 문제 발생하는지 확인해보자
                .where(studyLocation.study.id.eq(studyId))
                .fetch();
    }
}
