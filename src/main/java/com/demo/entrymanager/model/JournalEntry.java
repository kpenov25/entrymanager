package com.demo.entrymanager.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String scenario;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime draftedDate;
    private LocalDateTime reviewedDate;
    private LocalDateTime approvedDate;
    private String reviewNotes;
    private String approveNotes;

    @ManyToOne
    @JoinColumn(name = "assignedAccountantId")
    private Accountant assignedAccountant;

    public JournalEntry() {
    }

    public JournalEntry(Long id, String scenario, Status status, LocalDateTime draftedDate) {
        this.id = id;
        this.scenario = scenario;
        this.status = status;
        this.draftedDate = draftedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getDraftedDate() {
        return draftedDate;
    }

    public void setDraftedDate(LocalDateTime draftedDate) {
        this.draftedDate = draftedDate;
    }

    public LocalDateTime getReviewedDate() {
        return reviewedDate;
    }

    public void setReviewedDate(LocalDateTime reviewedDate) {
        this.reviewedDate = reviewedDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getApproveNotes() {
        return approveNotes;
    }

    public void setApproveNotes(String approveNotes) {
        this.approveNotes = approveNotes;
    }

    public Accountant getAssignedAccountant() {
        return assignedAccountant;
    }

    public void setAssignedAccountant(Accountant assignedAccountant) {
        this.assignedAccountant = assignedAccountant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalEntry)) return false;
        final JournalEntry journalEntry = (JournalEntry) o;
        return Objects.equals(id, journalEntry.id) &&
                Objects.equals(scenario, journalEntry.scenario) &&
                status == journalEntry.status &&
                Objects.equals(draftedDate, journalEntry.draftedDate) &&
                Objects.equals(reviewedDate, journalEntry.reviewedDate) &&
                Objects.equals(approvedDate, journalEntry.approvedDate) &&
                Objects.equals(reviewNotes, journalEntry.reviewNotes) &&
                Objects.equals(approveNotes, journalEntry.approveNotes)&&
                Objects.equals(assignedAccountant, journalEntry.assignedAccountant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scenario, status, draftedDate, reviewedDate, approvedDate, reviewNotes, approveNotes, assignedAccountant);
    }

}
