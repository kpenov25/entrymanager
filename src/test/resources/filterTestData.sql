INSERT INTO accountant (id, name) VALUES (1, 'David Marshall 1');
INSERT INTO accountant (id, name) VALUES (2, 'David Marshall 2');

INSERT INTO journal_entry (id, scenario, status, drafted_date, reviewed_date, approved_date, review_notes, approve_notes, assigned_accountant_id)
VALUES (1, 'test data layer - scenario 1', 'DRAFT', '2025-05-28T08:30:00', NULL, NULL, NULL, NULL, NULL);

INSERT INTO journal_entry (id, scenario, status, drafted_date, reviewed_date, approved_date, review_notes, approve_notes, assigned_accountant_id)
VALUES (2, 'test data layer - scenario 2', 'IN_REVIEW', CURRENT_TIMESTAMP(), NULL, NULL, NULL, NULL, 1);

INSERT INTO journal_entry (id, scenario, status, drafted_date, reviewed_date, approved_date, review_notes, approve_notes, assigned_accountant_id)
VALUES (3, 'test data layer - scenario 3', 'DRAFT', CURRENT_TIMESTAMP(), NULL, NULL, NULL, NULL, NULL);

INSERT INTO journal_entry (id, scenario, status, drafted_date, reviewed_date, approved_date, review_notes, approve_notes, assigned_accountant_id)
VALUES (4, 'test data layer - scenario 4', 'REVIEWED', '2025-05-28T08:30:00', CURRENT_TIMESTAMP(), NULL, 'test review notes s4', NULL, 2);

INSERT INTO journal_entry (id, scenario, status, drafted_date, reviewed_date, approved_date, review_notes, approve_notes, assigned_accountant_id)
VALUES (5, 'test data layer - scenario 5', 'APPROVED', '2025-05-28T08:30:00', '2025-06-01T08:30:00', CURRENT_TIMESTAMP(), 'test review notes s5', 'test approve notes s5', 2);