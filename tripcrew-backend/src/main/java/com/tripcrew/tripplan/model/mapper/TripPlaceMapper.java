package com.tripcrew.tripplan.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.tripplan.model.dto.TripPlace;

@Mapper
public interface TripPlaceMapper {

    int insert(TripPlace place);

    List<TripPlace> findByPlanId(Long planId);

    List<TripPlace> findByPlanIdAndVisitDay(@Param("planId") Long planId,
                                            @Param("visitDay") Integer visitDay);

    Optional<TripPlace> findByIdAndPlanId(@Param("id") Long id,
                                          @Param("planId") Long planId);

    int maxOrderIndex(@Param("planId") Long planId,
                      @Param("visitDay") Integer visitDay);

    int updateSchedule(@Param("id") Long id,
                       @Param("planId") Long planId,
                       @Param("visitDay") Integer visitDay,
                       @Param("orderIndex") Integer orderIndex);

    int updateOrderIndex(@Param("id") Long id,
                         @Param("planId") Long planId,
                         @Param("orderIndex") Integer orderIndex);

    int deleteByIdAndPlanId(@Param("id") Long id,
                            @Param("planId") Long planId);
}
