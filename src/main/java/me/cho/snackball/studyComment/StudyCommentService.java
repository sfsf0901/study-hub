package me.cho.snackball.studyComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.study.StudyService;
import me.cho.snackball.studyComment.domain.StudyComment;
import me.cho.snackball.studyComment.dto.CreateStudyCommentForm;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudyCommentService {

    private final StudyCommentRepository studyCommentRepository;
    private final StudyService studyService;


    public List<StudyComment> findAll(Long studyId) {
        return studyCommentRepository.findByStudyId(studyId);
    }

    public Long createComment(Long studyId, User user, CreateStudyCommentForm form) {
        Study study = studyService.findStudyById(studyId);
        StudyComment studyComment = studyCommentRepository.save(StudyComment.create(study, user, form));
        return studyComment.getId();
    }

    public void updateComment(Long studyId, Long commentId, User user, CreateStudyCommentForm form) {
        StudyComment comment = findStudyComment(commentId);

        // 권한 확인: 댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            log.warn("User ID {} does not have permission to edit comment ID {}", user.getId(), commentId);
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        // 댓글 수정
        comment.setContent(form.getContent());
    }

    public void deleteStudyComment(Long commentId, User user) {
        StudyComment comment = findStudyComment(commentId);

        if (!comment.getUser().getId().equals(user.getId())) {
            log.warn("User ID {} does not have permission to edit comment ID {}", user.getId(), commentId);
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        studyCommentRepository.delete(comment);
    }

    private StudyComment findStudyComment(Long commentId) {
        return studyCommentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
