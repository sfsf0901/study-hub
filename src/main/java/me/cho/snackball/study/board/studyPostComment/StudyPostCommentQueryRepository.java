package me.cho.snackball.study.board.studyPostComment;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import me.cho.snackball.study.board.StudyPostSearchCondition;
import me.cho.snackball.study.board.domain.StudyPost;
import me.cho.snackball.study.board.studyPostComment.domain.QStudyPostComment;
import me.cho.snackball.study.board.studyPostComment.domain.StudyPostComment;
import me.cho.snackball.user.domain.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.cho.snackball.study.board.studyPostComment.domain.QStudyPostComment.*;
import static me.cho.snackball.user.domain.QUser.*;

@Repository
@Transactional(readOnly = true)
public class StudyPostCommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public StudyPostCommentQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<StudyPostComment> findAllWithUser(Long studyPostId) {
        return queryFactory
                .selectFrom(studyPostComment)
                .join(studyPostComment.user, user).fetchJoin()
                .where(studyPostComment.studyPost.id.eq(studyPostId))
                .orderBy(studyPostComment.lastModifiedDate.desc())
                .fetch();
    }
    public Page<StudyPostComment> findAllWithUserPage(Long studyPostCommentId, Long studyPostId, Pageable pageable) {
        List<StudyPostComment> fetch = queryFactory
                .selectFrom(studyPostComment)
                .join(studyPostComment.user, user).fetchJoin()
                .where(studyPostComment.studyPost.id.eq(studyPostId))
                .orderBy(studyPostComment.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(studyPostComment.count())
                .from(studyPostComment)
                .where(studyPostComment.studyPost.id.eq(studyPostId));

        return PageableExecutionUtils.getPage(fetch, pageable, count::fetchOne);
    }

}
