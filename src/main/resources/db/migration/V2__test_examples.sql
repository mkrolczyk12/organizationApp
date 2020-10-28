INSERT INTO year_expenses (year, description, owner_id)
VALUES ('2020', 'Expenses in 2020 year (coronavirus)', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO month_expenses (month, description, owner_id)
VALUES ('january', 'quarantine', 'd2bc314b-d729-459c-b99d-37180d483012'),
       ('february', 'quarantine month 2', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO category_expenses (type, description, owner_id)
VALUES ('Computer', 'everything related with computer', 'd2bc314b-d729-459c-b99d-37180d483012'),
       ('Food', 'All food costs in january', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO expenses_process (price,currency,buy_date,description,transaction_type,notes,owner_id)
VALUES (300,'PLN','2020-03-12T12:30','Witcher III full game','by card','-', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (500.25,'USD','2022-05-22T09:30','graphic card','by cash','---', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (3333.33,'EURO','2020-09-12T12:30','bitcoin','by card','discount', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (33350.99,'USD','2029-12-12T22:30','new car','by card','-', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (360.50,'PLN','2016-02-17T19:30','annual fee for the gaming service','by crypto','-', 'd2bc314b-d729-459c-b99d-37180d483012');