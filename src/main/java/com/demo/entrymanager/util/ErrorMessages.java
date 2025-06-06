package com.demo.entrymanager.util;

public final class ErrorMessages {

    public static final String JOURNAL_ENTRY_NOT_FOUND = "Journal Entry not found";
    public static final String ACCOUNTANT_NOT_FOUND = "Accountant not found";
    public static final String REVIEW_NOTES_REQUIRED = "Review notes required to review the journal entry";
    public static final String ONLY_JOURNAL_ENTRY_WITH_STATUS_REVIEWED_CAN_BE_APPROVED = "Only Journal Entry with status REVIEWED can be approved.";
    public static final String ONLY_JOURNAL_ENTRY_WITH_STATUS_IN_REVIEW_CAN_BE_REVIEWED = "Only Journal Entry with status IN_REVIEW can be reviewed.";
    public static final String APPROVE_NOTES_REQUIRED = "Approve notes required to approve the journal entry";
    public static final String ONLY_JOURNAL_ENTRY_WITH_STATUS_DRAFT_CAN_BE_ASSIGNED_TO_AN_ACCOUNTANT = "Only Journal Entry with status DRAFT can be assigned to an accountant.";
    public static final String APPROVED_JOURNAL_ENTRIES_CAN_NOT_BE_UPDATED = "APPROVED Journal Entry cannot be updated";
    public static final String INVALID_DATE_RANGE = "Invalid date range";
    public static final String SCENARIO_MISSING = "Scenario is required during draft creation";
    public static final String SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT = "Scenario can only be updated when journal entry status is DRAFT";
    public static final String REVIEW_NOTES_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_IN_REVIEW = "Review notes can only be updated when journal entry status is IN_REVIEW";

    private ErrorMessages(){};

}
