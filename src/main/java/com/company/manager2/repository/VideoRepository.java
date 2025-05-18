package com.company.manager2.repository;

import com.company.manager2.entity.user.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {

    Optional<Video> findByName(String name);
}
