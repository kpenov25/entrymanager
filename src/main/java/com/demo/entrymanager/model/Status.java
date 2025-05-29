package com.demo.entrymanager.model;

public enum Status {
    DRAFT,
    IN_REVIEW, /* for simplicity IN_PROGRESS state is used instead of the following states: SUBMITTED, REVIEWED, APPROVED */
    REVIEWED,
    APPROVED

    /* the states REJECTED, CANCELLED and ARCHIVED are omitted for simplicity, more states can be added to fit the changing requirements */
}
