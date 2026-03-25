package com.skillsync.mentor.specification;

import org.springframework.data.jpa.domain.Specification;
import com.skillsync.mentor.entity.Mentor;

public class MentorSpecification {

    public static Specification<Mentor> hasMinPrice(Double minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null :
                        cb.greaterThanOrEqualTo(root.get("hourlyRate"), minPrice);
    }

    public static Specification<Mentor> hasMaxPrice(Double maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null :
                        cb.lessThanOrEqualTo(root.get("hourlyRate"), maxPrice);
    }

    public static Specification<Mentor> hasRating(Double rating) {
        return (root, query, cb) ->
                rating == null ? null :
                        cb.greaterThanOrEqualTo(root.get("averageRating"), rating);
    }

    public static Specification<Mentor> isAvailable(Boolean available) {
        return (root, query, cb) ->
                available == null ? null :
                        cb.equal(root.get("available"), available);
    }
}