package com.demo.entrymanager.repository.impl;

import com.demo.entrymanager.model.*;
import com.demo.entrymanager.repository.JournalEntryFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryFilterRepositoryImpl implements JournalEntryFilterRepository {
    private static final String STATUS_FIELD = "status";
    private static final String DRAFTED_DATE_FIELD = "draftedDate";
    private static final String ASSIGNED_ACCOUNTANT_FIELD = "assignedAccountant";
    private static final String NAME_FIELD = "name";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<JournalEntry> findWithFilters(final List<Status> statuses, final LocalDateTime startDate,
                                              final LocalDateTime endDate, final String assignedAccountant) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JournalEntry> query = cb.createQuery(JournalEntry.class);
        final Root<JournalEntry> journalEntryRoot = query.from(JournalEntry.class);

        final List<Predicate> predicates = buildPredicates(statuses, startDate, endDate, assignedAccountant, journalEntryRoot, cb);

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }

    private List<Predicate> buildPredicates(List<Status> statuses, LocalDateTime startDate, LocalDateTime endDate, String assignedAccountant, Root<JournalEntry> journalEntryRoot, CriteriaBuilder cb) {
        final List<Predicate> predicates = new ArrayList<>();
        if(statuses != null && !statuses.isEmpty()){
            predicates.add(journalEntryRoot.get(STATUS_FIELD).in(statuses));
        }
        if(startDate != null && endDate != null){
            predicates.add(cb.between(journalEntryRoot.get(DRAFTED_DATE_FIELD), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(journalEntryRoot.get(DRAFTED_DATE_FIELD), startDate));
        }else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(journalEntryRoot.get(DRAFTED_DATE_FIELD), endDate));
        }
        final Join<JournalEntry, Accountant> accountantJoin = journalEntryRoot.join(ASSIGNED_ACCOUNTANT_FIELD, JoinType.LEFT);
        if (assignedAccountant != null && !assignedAccountant.trim().isEmpty()) {
            predicates.add( cb.equal(accountantJoin.get(NAME_FIELD), assignedAccountant));
        }
        return predicates;
    }
}
