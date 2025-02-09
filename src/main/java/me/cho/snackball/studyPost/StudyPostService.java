package me.cho.snackball.study.board;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.study.board.domain.StudyPost;
import me.cho.snackball.study.board.dto.CreateStudyPostForm;
import me.cho.snackball.study.board.dto.UpdateStudyPostForm;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyPostService {

    private final StudyPostRepository studyPostRepository;


    public Long createStudyPost(User user, Study study, CreateStudyPostForm form) {
        StudyPost studyPost = studyPostRepository.save(StudyPost.create(user, study, form));
        return studyPost.getId();
    }

    public Long updateStudyPost(User user, Long studyPostId, UpdateStudyPostForm updateStudyPostForm) {
        StudyPost studyPost = findStudyPost(studyPostId);
        isAuthor(user, studyPost);

        studyPost.update(updateStudyPostForm);
        return null;
    }

    public void delete(User user, Long studyPostId) {
        StudyPost studyPost = findStudyPost(studyPostId);
        isAuthor(user, studyPost);

        studyPost.setDeleted(true);
    }

    public StudyPost findStudyPost(Long studyPostId) {
        return studyPostRepository.findById(studyPostId).orElseThrow(()-> new IllegalArgumentException("해당 게시물이 존재하지 않습니다. studyPostId = " + studyPostId));
    }

    private void isAuthor(User user, StudyPost studyPost) {
        if (!studyPost.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 게시물의 작성자가 아닙니다. userId: " + user.getId());
        }
    }
}
