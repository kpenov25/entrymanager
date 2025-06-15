package com.demo.entrymanager.repository.impl;

import com.demo.entrymanager.model.Accountant;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.repository.JournalEntryFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryFilterRepositoryImpl implements JournalEntryFilterRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<JournalEntry> findWithFilters(final List<Status> statuses, final LocalDateTime startDate,
                                              final LocalDateTime endDate, final String assignedAccountant) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JournalEntry> query = cb.createQuery(JournalEntry.class);
        final Root<JournalEntry> journalEntryRoot = query.from(JournalEntry.class);

        List<Predicate> predicates = new ArrayList<>();

        if(statuses != null && !statuses.isEmpty()){
            predicates.add(journalEntryRoot.get("status").in(statuses));
        }
        if(startDate != null && endDate != null){
            predicates.add(cb.between(journalEntryRoot.get("draftedDate"), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(journalEntryRoot.get("draftedDate"), startDate));
        }else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(journalEntryRoot.get("draftedDate"), endDate));
        }


        final Join<JournalEntry, Accountant> accountantJoin = journalEntryRoot.join("assignedAccountant", JoinType.LEFT);
        if (assignedAccountant != null && !assignedAccountant.trim().isEmpty()) {
            predicates.add( cb.equal(accountantJoin.get("name"), assignedAccountant));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
