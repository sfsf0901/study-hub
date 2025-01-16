package me.cho.snackball.user;

import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);

}
