package com.skillsync.mentor.specification;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorSkill;

public class MentorSearchSpecification {

    public static Specification<Mentor> hasSkill(Long skillId) {

        return (root, query, cb) -> {

            if (skillId == null)
                return null;

            Join<Mentor, MentorSkill> join =
                    root.join("mentorSkills");

            return cb.equal(join.get("skillId"), skillId);
        };
    }

    public static Specification<Mentor> minPrice(Double min) {
        return (root, query, cb) ->
                min == null ? null :
                        cb.greaterThanOrEqualTo(root.get("hourlyRate"), min);
    }

    public static Specification<Mentor> maxPrice(Double max) {
        return (root, query, cb) ->
                max == null ? null :
                        cb.lessThanOrEqualTo(root.get("hourlyRate"), max);
    }

    public static Specification<Mentor> minRating(Double rating) {
        return (root, query, cb) ->
                rating == null ? null :
                        cb.greaterThanOrEqualTo(root.get("averageRating"), rating);
    }

    public static Specification<Mentor> availability(Boolean available) {
        return (root, query, cb) ->
                available == null ? null :
                        cb.equal(root.get("available"), available);
    }
}