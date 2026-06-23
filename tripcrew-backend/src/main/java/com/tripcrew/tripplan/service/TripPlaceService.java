package com.tripcrew.tripplan.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.mapper.AttractionMapper;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.exception.TripPlaceNotFoundException;
import com.tripcrew.tripplan.model.dto.TripPlace;
import com.tripcrew.tripplan.model.dto.TripPlaceCreateRequest;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse.RoutePoint;
import com.tripcrew.tripplan.model.dto.TripPlaceOptimizeRequest;
import com.tripcrew.tripplan.model.dto.TripPlaceReorderRequest;
import com.tripcrew.tripplan.model.dto.TripPlaceResponse;
import com.tripcrew.tripplan.model.dto.TripPlaceScheduleRequest;
import com.tripcrew.tripplan.model.dto.TripPlan;
import com.tripcrew.tripplan.model.mapper.TripPlaceMapper;
import com.tripcrew.tripplan.model.mapper.TripPlanMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final TripPlanMapper tripPlanMapper;
    private final TripPlaceMapper tripPlaceMapper;
    private final AttractionMapper attractionMapper;
    private final NaverDirectionsService naverDirectionsService;
    private final TripPlanAccessService accessService;

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> list(Long planId, Long userId) {
        accessService.requireMember(planId, userId);
        return tripPlaceMapper.findByPlanId(planId).stream()
                .map(TripPlaceResponse::from)
                .toList();
    }

    @Transactional
    public TripPlaceResponse create(Long planId, Long userId, TripPlaceCreateRequest request) {
        TripPlan plan = accessService.requireEditor(planId, userId);
        validateVisitDay(plan, request.visitDay());
        TripPlace place = buildPlace(planId, request);
        Integer visitDay = place.getVisitDay();
        place.setOrderIndex(tripPlaceMapper.maxOrderIndex(planId, visitDay) + 1);

        tripPlaceMapper.insert(place);
        return TripPlaceResponse.from(findPlaceOrThrow(place.getId(), planId));
    }

    @Transactional
    public TripPlaceResponse updateSchedule(Long planId, Long placeId, Long userId, TripPlaceScheduleRequest request) {
        TripPlan plan = accessService.requireEditor(planId, userId);
        validateVisitDay(plan, request.visitDay());
        findPlaceOrThrow(placeId, planId);

        Integer visitDay = request.visitDay();
        Integer orderIndex = request.orderIndex();
        if (orderIndex == null) {
            orderIndex = tripPlaceMapper.maxOrderIndex(planId, visitDay) + 1;
        }

        tripPlaceMapper.updateSchedule(placeId, planId, visitDay, orderIndex);
        return TripPlaceResponse.from(findPlaceOrThrow(placeId, planId));
    }

    @Transactional
    public List<TripPlaceResponse> reorder(Long planId, Long userId, TripPlaceReorderRequest request) {
        TripPlan plan = accessService.requireEditor(planId, userId);
        validateVisitDay(plan, request.visitDay());
        Integer visitDay = request.visitDay();
        List<TripPlace> places = tripPlaceMapper.findByPlanId(planId);
        Map<Long, TripPlace> byId = places.stream()
                .collect(Collectors.toMap(TripPlace::getId, Function.identity()));

        for (Long placeId : request.placeIds()) {
            TripPlace place = byId.get(placeId);
            if (place == null) {
                throw new TripPlaceNotFoundException();
            }
            if (!sameVisitDay(place.getVisitDay(), visitDay)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 Day의 장소만 순서를 변경할 수 있습니다.");
            }
        }

        int order = 1;
        for (Long placeId : request.placeIds()) {
            tripPlaceMapper.updateOrderIndex(placeId, planId, order++);
        }

        return list(planId, userId);
    }

    @Transactional
    public List<TripPlaceResponse> optimize(Long planId, Long userId, TripPlaceOptimizeRequest request) {
        TripPlan plan = accessService.requireEditor(planId, userId);
        validateVisitDay(plan, request.visitDay());
        List<TripPlace> places = tripPlaceMapper.findByPlanIdAndVisitDay(planId, request.visitDay());
        if (places.size() < 2) {
            return places.stream().map(TripPlaceResponse::from).toList();
        }
        if (places.stream().anyMatch((place) -> place.getLatitude() == null || place.getLongitude() == null)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "좌표가 없는 장소는 동선 최적화할 수 없습니다.");
        }

        List<TripPlace> optimized = naverDirectionsService.isConfigured()
                ? optimizeByDrivingDuration(places)
                : twoOpt(nearestNeighbor(places));
        int order = 1;
        for (TripPlace place : optimized) {
            tripPlaceMapper.updateOrderIndex(place.getId(), planId, order++);
        }

        return tripPlaceMapper.findByPlanIdAndVisitDay(planId, request.visitDay()).stream()
                .map(TripPlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DrivingRouteResponse drivingRoute(Long planId, Long userId, Integer visitDay) {
        TripPlan plan = accessService.requireMember(planId, userId);
        validateVisitDay(plan, visitDay);
        List<TripPlace> places = tripPlaceMapper.findByPlanIdAndVisitDay(planId, visitDay);
        if (places.size() < 2) {
            return new DrivingRouteResponse(List.of());
        }
        if (places.stream().anyMatch(place -> place.getLatitude() == null || place.getLongitude() == null)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "좌표가 없는 장소는 도로 경로를 표시할 수 없습니다.");
        }
        if (!naverDirectionsService.isConfigured()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "네이버 Directions 설정이 필요합니다.");
        }

        List<TripPlace> ordered = orderedPlaces(places);
        List<RoutePoint> path = new ArrayList<>();
        for (int index = 0; index < ordered.size() - 1; index++) {
            TripPlace from = ordered.get(index);
            TripPlace to = ordered.get(index + 1);
            List<RoutePoint> segment = naverDirectionsService.drivingPath(
                    from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
            path.addAll(index == 0 ? segment : segment.subList(1, segment.size()));
        }
        return new DrivingRouteResponse(path);
    }

    @Transactional
    public void delete(Long planId, Long placeId, Long userId) {
        accessService.requireEditor(planId, userId);
        int affected = tripPlaceMapper.deleteByIdAndPlanId(placeId, planId);
        if (affected == 0) {
            throw new TripPlaceNotFoundException();
        }
    }

    private TripPlace buildPlace(Long planId, TripPlaceCreateRequest request) {
        if (request.attractionId() != null) {
            AttractionDetailResponse attraction = attractionMapper.findByNo(request.attractionId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "관광지를 찾을 수 없습니다."));

            return TripPlace.builder()
                    .tripPlanId(planId)
                    .attractionId(attraction.getNo())
                    .name(cleanDisplayName(attraction.getTitle()))
                    .latitude(attraction.getLatitude())
                    .longitude(attraction.getLongitude())
                    .visitDay(request.visitDay())
                    .memo(request.memo())
                    .build();
        }

        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "직접 추가 장소는 이름이 필요합니다.");
        }

        return TripPlace.builder()
                .tripPlanId(planId)
                .name(name)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .visitDay(request.visitDay())
                .memo(request.memo())
                .build();
    }

    private TripPlace findPlaceOrThrow(Long placeId, Long planId) {
        return tripPlaceMapper.findByIdAndPlanId(placeId, planId)
                .orElseThrow(TripPlaceNotFoundException::new);
    }

    private String cleanDisplayName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("(?:\\s+\\(?#?\\d{5,}\\)?)+\\s*$", "")
                .replaceAll("^\\s*(?:\\(?#?\\d{5,}\\)?\\s+)+", "");
    }

    private void validateVisitDay(TripPlan plan, Integer visitDay) {
        if (visitDay == null) {
            return;
        }

        int maxDay = dayCount(plan);
        if (visitDay > maxDay) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "여행 기간을 벗어난 Day입니다.");
        }
    }

    private int dayCount(TripPlan plan) {
        if (plan.getStartDate() == null || plan.getEndDate() == null) {
            return 1;
        }
        return (int) ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
    }

    private boolean sameVisitDay(Integer left, Integer right) {
        return left == null ? right == null : left.equals(right);
    }

    private List<TripPlace> nearestNeighbor(List<TripPlace> places) {
        List<TripPlace> remaining = new ArrayList<>(places);
        remaining.sort(Comparator.comparing(TripPlace::getOrderIndex).thenComparing(TripPlace::getId));

        List<TripPlace> route = new ArrayList<>();
        route.add(remaining.remove(0));
        while (!remaining.isEmpty()) {
            TripPlace current = route.get(route.size() - 1);
            TripPlace next = remaining.stream()
                    .min(Comparator.comparingDouble((place) -> distanceKm(current, place)))
                    .orElseThrow();
            route.add(next);
            remaining.remove(next);
        }
        return route;
    }

    private List<TripPlace> optimizeByDrivingDuration(List<TripPlace> places) {
        List<TripPlace> ordered = orderedPlaces(places);
        long[][] durations = drivingDurationMatrix(ordered);
        return twoOpt(nearestNeighbor(ordered, durations), ordered, durations);
    }

    private long[][] drivingDurationMatrix(List<TripPlace> places) {
        int size = places.size();
        long[][] durations = new long[size][size];
        for (int from = 0; from < size; from++) {
            for (int to = 0; to < size; to++) {
                if (from == to) {
                    continue;
                }
                TripPlace start = places.get(from);
                TripPlace goal = places.get(to);
                durations[from][to] = naverDirectionsService.drivingDurationMillis(
                        start.getLatitude(),
                        start.getLongitude(),
                        goal.getLatitude(),
                        goal.getLongitude()
                );
            }
        }
        return durations;
    }

    private List<TripPlace> nearestNeighbor(List<TripPlace> places, long[][] durations) {
        List<Integer> remaining = new ArrayList<>();
        for (int i = 1; i < places.size(); i++) {
            remaining.add(i);
        }

        List<TripPlace> route = new ArrayList<>();
        int current = 0;
        route.add(places.get(current));
        while (!remaining.isEmpty()) {
            final int from = current;
            int next = remaining.stream()
                    .min(Comparator.comparingLong((index) -> durations[from][index]))
                    .orElseThrow();
            route.add(places.get(next));
            remaining.remove(Integer.valueOf(next));
            current = next;
        }
        return route;
    }

    private List<TripPlace> twoOpt(List<TripPlace> route) {
        List<TripPlace> best = new ArrayList<>(route);
        boolean improved = true;

        while (improved) {
            improved = false;
            for (int i = 1; i < best.size() - 1; i++) {
                for (int k = i + 1; k < best.size(); k++) {
                    List<TripPlace> candidate = twoOptSwap(best, i, k);
                    if (routeDistance(candidate) < routeDistance(best)) {
                        best = candidate;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private List<TripPlace> twoOpt(List<TripPlace> route, List<TripPlace> original, long[][] durations) {
        List<TripPlace> best = new ArrayList<>(route);
        boolean improved = true;

        while (improved) {
            improved = false;
            for (int i = 1; i < best.size() - 1; i++) {
                for (int k = i + 1; k < best.size(); k++) {
                    List<TripPlace> candidate = twoOptSwap(best, i, k);
                    if (routeDuration(candidate, original, durations) < routeDuration(best, original, durations)) {
                        best = candidate;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private List<TripPlace> orderedPlaces(List<TripPlace> places) {
        return places.stream()
                .sorted(Comparator.comparing(TripPlace::getOrderIndex).thenComparing(TripPlace::getId))
                .toList();
    }

    private List<TripPlace> twoOptSwap(List<TripPlace> route, int i, int k) {
        List<TripPlace> result = new ArrayList<>();
        result.addAll(route.subList(0, i));

        List<TripPlace> reversed = new ArrayList<>(route.subList(i, k + 1));
        java.util.Collections.reverse(reversed);
        result.addAll(reversed);

        result.addAll(route.subList(k + 1, route.size()));
        return result;
    }

    private double routeDistance(List<TripPlace> route) {
        double total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += distanceKm(route.get(i), route.get(i + 1));
        }
        return total;
    }

    private long routeDuration(List<TripPlace> route, List<TripPlace> original, long[][] durations) {
        Map<Long, Integer> indexById = new java.util.HashMap<>();
        for (int i = 0; i < original.size(); i++) {
            indexById.put(original.get(i).getId(), i);
        }

        long total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            int from = indexById.get(route.get(i).getId());
            int to = indexById.get(route.get(i + 1).getId());
            total += durations[from][to];
        }
        return total;
    }

    private double distanceKm(TripPlace from, TripPlace to) {
        double lat1 = toRadians(from.getLatitude());
        double lat2 = toRadians(to.getLatitude());
        double deltaLat = toRadians(to.getLatitude().subtract(from.getLatitude()));
        double deltaLng = toRadians(to.getLongitude().subtract(from.getLongitude()));

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double toRadians(BigDecimal value) {
        return Math.toRadians(value.doubleValue());
    }
}
