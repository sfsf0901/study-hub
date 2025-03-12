package me.cho.snackball.study;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.study.domain.QStudyMember;
import me.cho.snackball.study.domain.StudyMember;
import me.cho.snackball.user.domain.QUser;
import org.springframework.stereotype.Repository;

import static me.cho.snackball.study.domain.QStudyMember.*;
import static me.cho.snackball.user.domain.QUser.*;

@Repository
public class StudyMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyMemberQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public StudyMember findByIdWithUser(long studyMemberId) {
        return queryFactory
                .selectFrom(studyMember)
                .join(studyMember.user, user).fetchJoin()
                .where(studyMember.id.eq(studyMemberId))
                .fetchOne();
    }
}
