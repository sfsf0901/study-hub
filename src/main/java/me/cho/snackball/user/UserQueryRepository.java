package me.cho.snackball.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.location.domain.QUserLocation;
import me.cho.snackball.study.dto.SearchConditions;
import me.cho.snackball.studyTag.domain.QUserStudyTag;
import me.cho.snackball.studyTag.domain.StudyTag;
import me.cho.snackball.user.domain.QUser;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.cho.snackball.location.domain.QUserLocation.*;
import static me.cho.snackball.study.domain.QStudy.study;
import static me.cho.snackball.studyTag.domain.QUserStudyTag.*;
import static me.cho.snackball.user.domain.QUser.*;

@Repository
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    public UserQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<User> findByStudyTagsAndLocations(List<StudyTag> studyTags, List<Location> locations) {
        return queryFactory
                .selectFrom(user)
                .join(user.userStudyTags, userStudyTag)
                .join(user.userLocations, userLocation)
                .where(userStudyTag.studyTag.in(studyTags)
                        ,userLocation.location.in(locations))
                .fetch();
    }

}
