package com.team_5_back_repository.project.domain.member.service;

import com.team_5_back_repository.project.domain.member.dto.RegionDto;
import com.team_5_back_repository.project.domain.member.entity.Region;
import com.team_5_back_repository.project.domain.member.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {
    private final RegionRepository regionRepository;

    @Value("${region.secret-key}") String secretKey;

    public Object searchRegion(String query, Integer page, Integer pageSize) {

        String url = "https://api.vworld.kr/req/data?" +
                "service=data" +
                "&version=2.0" +
                "&request=GetFeature" +
                "&format=json" +
                "&errorformat=json" +
                "&size=" + pageSize +
                "&page=" + page +
                "&data=LT_C_ADEMD_INFO" +
                "&columns=emd_cd,full_nm,emd_kor_nm,emd_eng_nm,ag_geom" +
                "&geometry=false" +
                "&attribute=true" +
                "&crs=EPSG:4326" +
                "&domain=localhost" +
                "&key=" + secretKey +
                "&attrfilter=emd_kor_nm:like:" + query;

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Object.class);
    }

    @Transactional
    public void saveRegions(List<RegionDto> regions) {
        regions.forEach(region -> {
            if(regionRepository.findById(Long.parseLong(region.getCode())).isPresent()) return;
            regionRepository.save(region.toEntity());
        });
    }

    public Optional<Region> findById(Long id) {
        return regionRepository.findById(id);
    }
}