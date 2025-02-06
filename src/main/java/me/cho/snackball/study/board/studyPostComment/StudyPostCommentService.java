package me.cho.snackball.study.board.studyPostComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.study.board.StudyPostService;
import me.cho.snackball.study.board.domain.StudyPost;
import me.cho.snackball.study.board.studyPostComment.domain.StudyPostComment;
import me.cho.snackball.study.board.studyPostComment.dto.CreateStudyPostCommentForm;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudyPostCommentService {

    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyPostService studyPostService;

    public Long createComment(Long studyPostId, User user, CreateStudyPostCommentForm form) {
        StudyPost studyPost = studyPostService.findStudyPost(studyPostId);
        StudyPostComment studyPostComment = studyPostCommentRepository.save(StudyPostComment.create(studyPost, user, form));
        return studyPostComment.getId();
    }

    public void updateComment(Long studyPostId, Long commentId, User user, CreateStudyPostCommentForm form) {
        StudyPostComment comment = findStudyPostComment(commentId);

        // 권한 확인: 댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            log.warn("User ID {} does not have permission to edit comment ID {}", user.getId(), commentId);
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        // 댓글 수정
        comment.setContent(form.getContent());
    }

    public void deleteStudyComment(Long commentId, User user) {
        StudyPostComment comment = findStudyPostComment(commentId);

        if (!comment.getUser().getId().equals(user.getId())) {
            log.warn("User ID {} does not have permission to edit comment ID {}", user.getId(), commentId);
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        studyPostCommentRepository.delete(comment);
    }

    public StudyPostComment findStudyPostComment(Long commentId) {
        return studyPostCommentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
