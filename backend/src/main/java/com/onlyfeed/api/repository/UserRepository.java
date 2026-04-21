package com.onlyfeed.api.repository;

import com.onlyfeed.api.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<UserAccount> findTop10ByUsernameContainingIgnoreCaseOrderByUsernameAsc(String query);

    Page<UserAccount> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
