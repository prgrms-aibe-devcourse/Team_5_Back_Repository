package com.team_5_back_repository.project.domain.member.repository;

import com.team_5_back_repository.project.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByApiKey(String apiKey);

    @EntityGraph(attributePaths = {"activityRegions", "activityRegions.region"})
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findMemberWithRegions(@Param("id") Long id);
}
