package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Pure pricing engine for fixed-price book bundles. It deliberately has no
 * Spring or persistence dependency so that all matching and rounding rules
 * remain executable in small deterministic unit tests.
 */
@Service
public class BundlePricingService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public PricingResult price(Map<Long, CartBook> cartBooks,
                               List<BundleCandidate> bundles) {
        Map<Long, CartBook> safeCart = cartBooks == null ? Map.of() : cartBooks;
        List<BundleCandidate> eligible = new ArrayList<>();
        for (BundleCandidate bundle : bundles == null ? List.<BundleCandidate>of() : bundles) {
            if (isEligible(bundle, safeCart)) {
                eligible.add(normalize(bundle));
            }
        }
        eligible.sort(Comparator.comparing(BundleCandidate::id));

        BigDecimal regular = money(safeCart.values().stream()
                .map(book -> money(book.salePrice()).multiply(BigDecimal.valueOf(book.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        SearchState state = new SearchState(eligible);
        search(0, new HashSet<>(), new ArrayList<>(), BigDecimal.ZERO, state);
        List<Long> selectedIds = state.bestIds == null ? List.of() : List.copyOf(state.bestIds);
        BigDecimal discount = money(state.bestSavings == null ? BigDecimal.ZERO : state.bestSavings);
        Map<Long, BigDecimal> allocations = allocateDiscounts(eligible, selectedIds);
        return new PricingResult(
                regular,
                discount,
                money(regular.subtract(discount)),
                List.copyOf(selectedIds),
                eligible.stream().map(BundleCandidate::id).toList(),
                allocations,
                eligible);
    }

    private void search(int index,
                        Set<Long> usedBooks,
                        List<BundleCandidate> selected,
                        BigDecimal savings,
                        SearchState state) {
        if (index >= state.candidates.size()) {
            List<Long> ids = selected.stream().map(BundleCandidate::id).sorted().toList();
            if (state.bestSavings == null
                    || savings.compareTo(state.bestSavings) > 0
                    || savings.compareTo(state.bestSavings) == 0 && lexicographicallySmaller(ids, state.bestIds)) {
                state.bestSavings = savings;
                state.bestIds = ids;
            }
            return;
        }

        // Excluding a candidate is always a valid branch.
        search(index + 1, usedBooks, selected, savings, state);

        BundleCandidate candidate = state.candidates.get(index);
        if (!overlaps(candidate, usedBooks)) {
            Set<Long> nextUsed = new HashSet<>(usedBooks);
            candidate.members().forEach(member -> nextUsed.add(member.bookId()));
            selected.add(candidate);
            search(index + 1, nextUsed, selected,
                    savings.add(candidate.savings()), state);
            selected.remove(selected.size() - 1);
        }
    }

    private boolean isEligible(BundleCandidate bundle, Map<Long, CartBook> cartBooks) {
        if (bundle == null || bundle.id() == null || bundle.bundlePrice() == null
                || bundle.members() == null || bundle.members().size() < 2
                || bundle.members().size() > 10 || bundle.bundlePrice().signum() < 0) {
            return false;
        }
        Set<Long> ids = new HashSet<>();
        BigDecimal regular = BigDecimal.ZERO;
        for (BundleMember member : bundle.members()) {
            if (member == null || member.bookId() == null || !ids.add(member.bookId())) {
                return false;
            }
            CartBook cartBook = cartBooks.get(member.bookId());
            if (cartBook == null || cartBook.quantity() < 1 || cartBook.salePrice() == null
                    || cartBook.salePrice().signum() < 0) {
                return false;
            }
            regular = regular.add(money(cartBook.salePrice()));
        }
        return money(bundle.bundlePrice()).compareTo(money(regular)) < 0;
    }

    private BundleCandidate normalize(BundleCandidate bundle) {
        List<BundleMember> members = bundle.members().stream()
                .sorted(Comparator.comparing(BundleMember::bookId))
                .toList();
        BigDecimal regular = members.stream().map(BundleMember::salePrice)
                .map(BundlePricingService::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new BundleCandidate(bundle.id(), bundle.name(), money(bundle.bundlePrice()), members,
                money(regular.subtract(bundle.bundlePrice())));
    }

    private boolean overlaps(BundleCandidate candidate, Set<Long> usedBooks) {
        return candidate.members().stream().anyMatch(member -> usedBooks.contains(member.bookId()));
    }

    private boolean lexicographicallySmaller(List<Long> left, List<Long> right) {
        if (right == null) return true;
        int count = Math.min(left.size(), right.size());
        for (int i = 0; i < count; i++) {
            int comparison = Long.compare(left.get(i), right.get(i));
            if (comparison != 0) return comparison < 0;
        }
        return left.size() < right.size();
    }

    private Map<Long, BigDecimal> allocateDiscounts(List<BundleCandidate> eligible,
                                                     List<Long> selectedIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long selectedId : selectedIds) {
            BundleCandidate bundle = eligible.stream()
                    .filter(candidate -> candidate.id().equals(selectedId))
                    .findFirst().orElse(null);
            if (bundle == null) continue;
            BigDecimal remaining = bundle.savings();
            for (int i = 0; i < bundle.members().size(); i++) {
                BundleMember member = bundle.members().get(i);
                BigDecimal allocated;
                if (i == bundle.members().size() - 1) {
                    allocated = remaining;
                } else {
                    allocated = money(bundle.savings()
                            .multiply(member.salePrice())
                            .divide(bundle.regularAmount(), 8, ROUNDING));
                    remaining = money(remaining.subtract(allocated));
                }
                result.merge(member.bookId(), allocated, BigDecimal::add);
            }
        }
        return result.entrySet().stream().collect(HashMap::new,
                (map, entry) -> map.put(entry.getKey(), money(entry.getValue())), HashMap::putAll);
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(MONEY_SCALE, ROUNDING);
    }

    private static final class SearchState {
        private final List<BundleCandidate> candidates;
        private BigDecimal bestSavings;
        private List<Long> bestIds;

        private SearchState(List<BundleCandidate> candidates) {
            this.candidates = candidates;
        }
    }

    public record CartBook(Long bookId, int quantity, BigDecimal salePrice) {
    }

    public record BundleMember(Long bookId, String title, String coverUrl, BigDecimal salePrice) {
    }

    public record BundleCandidate(Long id,
                                  String name,
                                  BigDecimal bundlePrice,
                                  List<BundleMember> members,
                                  BigDecimal computedSavings) {
        public BundleCandidate(Long id, String name, BigDecimal bundlePrice, List<BundleMember> members) {
            this(id, name, bundlePrice, members, null);
        }

        private BigDecimal regularAmount() {
            return members.stream().map(BundleMember::salePrice).map(BundlePricingService::money)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal savings() {
            return computedSavings != null ? money(computedSavings) : money(regularAmount().subtract(bundlePrice));
        }
    }

    public record PricingResult(BigDecimal regularAmount,
                                BigDecimal discountAmount,
                                BigDecimal payableAmount,
                                List<Long> selectedBundleIds,
                                List<Long> eligibleBundleIds,
                                Map<Long, BigDecimal> bookDiscountAllocations,
                                List<BundleCandidate> eligibleBundles) {
    }
}


