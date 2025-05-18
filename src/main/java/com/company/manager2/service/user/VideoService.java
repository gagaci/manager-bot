package com.company.manager2.service.user;

import com.company.manager2.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public String getFirstVideo() {
        return String.valueOf(videoRepository.findByName("video_1").get().getFileId());
    }

    public String getSecondVideo() {
        return String.valueOf(videoRepository.findByName("video_2").get().getFileId());
    }

}
