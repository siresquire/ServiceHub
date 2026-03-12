package com.servicehub.service;

import com.servicehub.dto.DashboardStatsResponse;
import com.servicehub.dto.DashboardTrendsResponse;
import com.servicehub.model.ServiceRequest;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.ServiceRequestRepository;
import com.servicehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

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

        // SLA Compliance — Improved calculation
        // Count all tickets with SLA deadlines (resolved or still open)
        long totalWithSla = all.stream()
                .filter(r -> r.getResolutionSlaDeadline() != null)
                .count();

        // Count compliant tickets:
        // 1. Resolved tickets that were resolved before deadline
        // 2. Open/In-Progress tickets that haven't breached yet (slaBreached = false)
        long compliantTickets = all.stream()
                .filter(r -> r.getResolutionSlaDeadline() != null)
                .filter(r -> {
                    // If resolved, check if resolved before deadline
                    if (r.getResolvedAt() != null) {
                        return r.getResolvedAt().isBefore(r.getResolutionSlaDeadline()) 
                            || r.getResolvedAt().isEqual(r.getResolutionSlaDeadline());
                    }
                    // If not resolved, check if not breached yet
                    return r.getSlaBreached() == null || !r.getSlaBreached();
                })
                .count();

        double slaComplianceRate = totalWithSla > 0 ? (compliantTickets * 100.0 / totalWithSla) : 100.0;

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
                .totalUsers(userRepository.count())
                .agentCount(userRepository.countByRole(Role.AGENT))
                .totalDepartments(departmentRepository.count())
                .avgResolutionHours(avgResolutionHours)
                .requestsByCategory(byCategory)
                .requestsByPriority(byPriority)
                .requestsByStatus(byStatus)
                .slaComplianceRate(slaComplianceRate)
                .build();
    }

    /**
     * Returns SLA performance metrics broken down by request category.
     * For each category, calculates the percentage of requests resolved
     * within their SLA deadline. Also includes overall average resolution time.
     *
     * <p>Result is cached under the key {@code "sla"} and automatically
     * evicted every 5 minutes alongside other dashboard caches.</p>
     *
     * @return {@link DashboardStatsResponse} containing SLA compliance rates
     *         per category and overall average resolution hours
     */
    public DashboardStatsResponse getSlaStas(){

        List<ServiceRequest> all = requestRepository.findAll();

        Map<String,Long>  slaByCategory = new HashMap<>();

        for(RequestCategory category : RequestCategory.values()){

            // Count all tickets in this category with SLA deadlines
            long totalWithSla = all.stream()
                    .filter(r -> r.getCategory() == category)
                    .filter(r -> r.getResolutionSlaDeadline() != null)
                    .count();

            // Count compliant tickets in this category
            long compliant = all.stream()
                    .filter(r -> r.getCategory() == category)
                    .filter(r -> r.getResolutionSlaDeadline() != null)
                    .filter(r -> {
                        // If resolved, check if resolved before deadline
                        if (r.getResolvedAt() != null) {
                            return r.getResolvedAt().isBefore(r.getResolutionSlaDeadline())
                                || r.getResolvedAt().isEqual(r.getResolutionSlaDeadline());
                        }
                        // If not resolved, check if not breached yet
                        return r.getSlaBreached() == null || !r.getSlaBreached();
                    })
                    .count();

            double slaRate = totalWithSla > 0 ? (compliant * 100.0 / totalWithSla) : 100.0;

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


    /**
     * Returns daily ticket volume over the specified number of past days.
     * Groups requests by their creation date and counts them per day.
     * Used to render the trend chart on the analytics dashboard.
     *
     * <p>Result is cached per period value (e.g. 7 days, 30 days).
     * Cache is evicted every 5 minutes to reflect new tickets.</p>
     *
     * @param days the number of days to look back (e.g. 7 or 30)
     * @return {@link DashboardTrendsResponse} containing a date-to-count map
     *         and the period label
     */
//    @Cacheable(key = "'trends:' + #days")
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
