package me.cho.snackball.study.board;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.study.board.domain.StudyPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static me.cho.snackball.study.board.domain.QStudyPost.*;
import static me.cho.snackball.user.domain.QUser.*;

@Repository
@Transactional(readOnly = true)
public class StudyPostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyPostQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<StudyPost> findAllWithUser(Long studyId, StudyPostSearchCondition condition, Pageable pageable) {
        List<StudyPost> fetch =  queryFactory
                .selectFrom(studyPost)
                .join(studyPost.user, user).fetchJoin()
                .where(studyPost.study.id.eq(studyId)
                        .and(studyPost.isDeleted.eq(false))
                        .and(titleContains(condition.getKeyword()))
                        .or(nicknameContains(condition.getKeyword())))
                .orderBy(studyPost.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(studyPost.count())
                .from(studyPost)
                .where(studyPost.isDeleted.eq(false)
                        .and(titleContains(condition.getKeyword()))
                        .or(nicknameContains(condition.getKeyword())));

        return PageableExecutionUtils.getPage(fetch, pageable, count::fetchOne);
    }

    private BooleanExpression titleContains(String keyword) {
        return StringUtils.hasText(keyword) ? studyPost.title.containsIgnoreCase(keyword) : null;
    }

/*
    private BooleanExpression contentContains(String keyword) {
        return StringUtils.hasText(keyword) ? studyPost.content.containsIgnoreCase(keyword) : null;
    }
*/

    private BooleanExpression nicknameContains(String keyword) {
        return StringUtils.hasText(keyword) ? studyPost.user.nickname.containsIgnoreCase(keyword) : null;
    }
}
