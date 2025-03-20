package me.cho.snackball.study;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.dto.SearchConditions;
import me.cho.snackball.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static me.cho.snackball.study.domain.QStudy.*;

@Repository
public class StudyQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Study> findByPublished(SearchConditions searchConditions, int offset, int limit) {
        return queryFactory
                .selectFrom(study)
                .where(study.published.isTrue()
                        .and(studyContains(searchConditions.getKeyword()))
                        .or(studyTagContains(searchConditions.getKeyword()))
                        .or(studyLocationContains(searchConditions.getKeyword()))
                        .and(studyTagContains(searchConditions.getTag()))
                        .and(studyLocationContains(searchConditions.getLocation()))
                )
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public long countByPublished(SearchConditions searchConditions) {
        Long count = queryFactory
                .select(study.count())  // count() 사용
                .from(study)
                .where(study.published.isTrue()
                        .and(studyContains(searchConditions.getKeyword()))
                        .or(studyTagContains(searchConditions.getKeyword()))
                        .or(studyLocationContains(searchConditions.getKeyword()))
                        .and(studyTagContains(searchConditions.getTag()))
                        .and(studyLocationContains(searchConditions.getLocation()))
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    public List<Study> findByManager(User user, int offset, int limit) {
        return queryFactory
                .selectFrom(study)
                .where(studyManagerEq(user))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public long countByManager(User user) {
        Long count = queryFactory
                .select(study.count())  // count() 사용
                .from(study)
                .where(studyManagerEq(user))
                .fetchOne();

        return count != null ? count : 0;
    }

    public List<Study> findByPublishedAndManager(User user, int offset, int limit) {
        return queryFactory
                .selectFrom(study)
                .where(study.published.isTrue(),
                        studyManagerEq(user)
                )
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public long countByPublishedAndManager(User user) {
        Long count = queryFactory
                .select(study.count())  // count() 사용
                .from(study)
                .where(study.published.isTrue(),
                        studyManagerEq(user)
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    public List<Study> findByPublishedAndMember(User user, int offset, int limit) {
        return queryFactory
                .selectFrom(study)
                .where(study.published.isTrue(),
                        study.members.any().active.isTrue(),
                        studyMemberEq(user)
                )
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public long countByPublishedAndMember(User user) {
        Long count = queryFactory
                .select(study.count())  // count() 사용
                .from(study)
                .where(study.published.isTrue(),
                        study.members.any().active.isTrue(),
                        studyMemberEq(user)
                )
                .fetchOne();

        return count != null ? count : 0;
    }



    private BooleanExpression studyManagerEq(User user) {
        return user != null ? study.managers.any().user.eq(user) : null;
    }

    private BooleanExpression studyMemberEq(User user) {
        return user != null ? study.members.any().user.eq(user) : null;
    }

    public Page<Study> findByPublishedPage(SearchConditions searchConditions, int page, int limit) {
        int offset = page * limit;

        List<Study> contents = queryFactory
                .selectFrom(study)
                .where(
                        study.published.eq(true)
//                        studyContains(searchConditions.getKeyword()),
//                        studyTagContains(searchConditions.getKeyword()),
//                        studyLocationContains(searchConditions.getKeyword()),
//                        studyTagContains(searchConditions.getTag()),
//                        studyLocationContains(searchConditions.getLocation())
                        )
//                .distinct()
                .offset(offset)
                .limit(limit)
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(study.count())
                .from(study)
                .where(
                        study.published.eq(true)
//                        studyContains(searchConditions.getKeyword()),
//                        studyTagContains(searchConditions.getKeyword()),
//                        studyLocationContains(searchConditions.getKeyword()),
//                        studyTagContains(searchConditions.getTag()),
//                        studyLocationContains(searchConditions.getLocation())
                );

        return PageableExecutionUtils.getPage(contents, Pageable.ofSize(limit).withPage(page), countQuery::fetchOne);
    }

    public List<Study> findAll(int offset, int limit) {
        return queryFactory
                .selectFrom(study)
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    private BooleanExpression studyContains(String keyword) {
        return StringUtils.hasText(keyword) ? study.title.containsIgnoreCase(keyword.trim()) : null;
    }

    private BooleanExpression studyTagContains(String tag) {
        return StringUtils.hasText(tag) ? study.studyStudyTags.any().name.containsIgnoreCase(tag.trim()) : null;
    }

    private BooleanExpression studyLocationContains(String location) {
        return StringUtils.hasText(location) ? study.studyLocations.any().city.containsIgnoreCase(location.trim()) : null;
    }


}
