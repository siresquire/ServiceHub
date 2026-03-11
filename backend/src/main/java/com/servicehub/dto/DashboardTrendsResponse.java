package com.servicehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class DashboardTrendsResponse {
    private Map<String,Long> dailyVolume;
    private String period;
}

