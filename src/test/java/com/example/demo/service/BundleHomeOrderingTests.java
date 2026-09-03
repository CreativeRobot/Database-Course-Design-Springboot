package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BundleHomeOrderingTests {

    @Test
    void placesPinnedBundlesFirstByPriorityThenPlacesOtherBundlesByNewestCreationTime() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 3, 12, 0);

        List<BundleHomeOrdering.BundlePosition> sorted = BundleHomeOrdering.sort(List.of(
                position(1L, false, 0, now.minusDays(1)),
                position(2L, true, 10, now.minusDays(5)),
                position(3L, true, 30, now.minusDays(10)),
                position(4L, false, 0, now)));

        assertThat(sorted).extracting(BundleHomeOrdering.BundlePosition::id)
                .containsExactly(3L, 2L, 4L, 1L);
    }

    private static BundleHomeOrdering.BundlePosition position(
            long id, boolean homePinned, int homePriority, LocalDateTime createTime) {
        return new BundleHomeOrdering.BundlePosition(id, homePinned, homePriority, createTime);
    }
}
