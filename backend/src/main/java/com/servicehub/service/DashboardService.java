package com.servicehub.service;

import com.servicehub.dto.DashboardStatsResponse;
import com.servicehub.dto.DashboardTrendsResponse;
import com.servicehub.model.ServiceRequest;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ServiceRequestRepository requestRepository;

    public DashboardStatsResponse getDashboardStats(){
        List<ServiceRequest> all = requestRepository.findAll();

        //total request
        long total = all.size();

        //all requests that are open
        long openRequest =  all.stream()
                .filter(o->o.getStatus() == RequestStatus.OPEN ||
                        o.getStatus() == RequestStatus.IN_PROGRESS ||
                        o.getStatus() == RequestStatus.ASSIGNED)
                .count();

        //all requests that are resolved
        long resolvedRequest = all.stream()
                .filter(r->r.getStatus() == RequestStatus.RESOLVED
                        || r.getStatus() == RequestStatus.CLOSED)
                .count();

        // average resolution time in hours
        double avgResolutionHours = all.stream()
                .filter(avg->avg.getResolvedAt() !=null && avg.getCreatedAt() !=null)
                .mapToLong(r-> ChronoUnit.HOURS.between(r.getCreatedAt(),r.getResolvedAt()))
                .average()
                .orElse(0.0);

        // slaCompliance — requests resolved before deadline
        double withDeadLine = all.stream()
                .filter(r->r.getResolutionSlaDeadline() !=null && r.getResolvedAt() !=null)
                .count();

        double compliant = all.stream()
                .filter(r->r.getResolutionSlaDeadline() !=null && r.getResolvedAt() !=null
                        && r.getResolvedAt().isBefore(r.getResolutionSlaDeadline()))
                .count();

        double slaComplianceRate = withDeadLine > 0 ? (compliant/withDeadLine) * 100 : 0.0;

        // group by category
        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(
                        r->r.getCategory().name(),
                        Collectors.counting()
                ));

        //group by priority
        Map<String, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(
                        r->r.getPriority().name(),
                        Collectors.counting()));

        //group by status
        Map<String,Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(
                        r->r.getStatus().name(),
                        Collectors.counting()));


        return DashboardStatsResponse.builder()
                .totalRequests(total)
                .openRequests(openRequest)
                .resolvedRequests(resolvedRequest)
                .avgResolutionHours(avgResolutionHours)
                .requestsByCategory(byCategory)
                .requestsByPriority(byPriority)
                .requestsByStatus(byStatus)
                .slaComplianceRate(slaComplianceRate)
                .build();
    }

    public DashboardStatsResponse getSlaStas(){

        List<ServiceRequest> all = requestRepository.findAll();

        Map<String,Long>  slaByCategory = new HashMap<>();

        for(RequestCategory category : RequestCategory.values()){

            double withDeadLine = all.stream()
                    .filter(r->r.getCategory() ==category)
                    .filter(r->r.getResolutionSlaDeadline() !=null &&
                            r.getResolvedAt() !=null)
                    .count();

            double complaint = all.stream()
                    .filter(r->r.getCategory() ==category)
                    .filter(
                            r->r.getResolutionSlaDeadline() !=null &&
                                    r.getResolvedAt()!=null &&
                                    r.getResolvedAt().isBefore(r.getResolutionSlaDeadline()))
                    .count();

            double slaRate =withDeadLine > 0 ?(complaint/withDeadLine)*100 : 0.0;

            slaByCategory.put(category.name(), (long) slaRate);
        }

        double avg = all.stream()
                .filter(r->r.getResolvedAt()!=null && r.getCreatedAt() !=null)
                .mapToDouble(r->ChronoUnit.HOURS.between(r.getCreatedAt(),r.getResolvedAt()))
                .average()
                .orElse(0.0);

        return DashboardStatsResponse.builder()
                .avgResolutionHours(avg)
                .slaByCategory(slaByCategory)
                .build();
    }

    public DashboardTrendsResponse getTrends(int days){

        List<ServiceRequest> all = requestRepository.findAll();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        Map<String,Long> dailyVolume = all.stream()
                .filter(r->r.getCreatedAt().isAfter(startDate))
                .collect(Collectors.groupingBy(
                        r->r.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return DashboardTrendsResponse.builder()
                .dailyVolume(dailyVolume)
                .period(days + " days")
                .build();
    }
}
