package com.example.demo.service;

import com.example.demo.entity.BookBundle;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BundleHomeOrdering {
    private BundleHomeOrdering() {
    }

    public static List<BundlePosition> sort(List<BundlePosition> positions) {
        return positions.stream()
                .sorted(Comparator.comparing(BundlePosition::homePinned).reversed()
                        .thenComparing(BundlePosition::homePriority, Comparator.reverseOrder())
                        .thenComparing(BundlePosition::createTime, Comparator.reverseOrder())
                        .thenComparing(BundlePosition::id, Comparator.reverseOrder()))
                .toList();
    }

    public static List<BookBundle> sortBundles(List<BookBundle> bundles) {
        Map<Long, BookBundle> bundlesById = bundles.stream()
                .collect(java.util.stream.Collectors.toMap(
                        BookBundle::getId,
                        bundle -> bundle,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        return sort(bundles.stream()
                .map(bundle -> new BundlePosition(
                        bundle.getId(),
                        Boolean.TRUE.equals(bundle.getHomePinned()),
                        bundle.getHomePriority() == null ? 0 : bundle.getHomePriority(),
                        bundle.getCreateTime()))
                .toList()).stream()
                .map(position -> bundlesById.get(position.id()))
                .toList();
    }

    public record BundlePosition(Long id, boolean homePinned, int homePriority, LocalDateTime createTime) {
    }
}
