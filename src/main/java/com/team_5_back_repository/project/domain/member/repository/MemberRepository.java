package com.team_5_back_repository.project.domain.member.repository;

import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
    Optional<Object> findByNickname(String nickname);
}
