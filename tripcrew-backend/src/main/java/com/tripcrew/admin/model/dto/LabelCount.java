package com.tripcrew.admin.model.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 라벨별 건수(역할/상태 분포 도넛 차트용). label 은 enum name(예: USER, ACTIVE).
 */
@Getter
@Setter
public class LabelCount {
    private String label;
    private long count;
}
