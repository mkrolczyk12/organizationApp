INSERT INTO year_expenses (year, description, owner_id)
VALUES ('2020', 'mock description 1', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO month_expenses (month, description, owner_id)
VALUES ('january', 'mock month1', 'd2bc314b-d729-459c-b99d-37180d483012'),
       ('february', 'mock month2', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO category_expenses (type, description, owner_id)
VALUES ('category1', 'mock description', 'd2bc314b-d729-459c-b99d-37180d483012'),
       ('category2', 'mock description222', 'd2bc314b-d729-459c-b99d-37180d483012');

INSERT INTO expenses_process (price,currency,buy_date,description,transaction_type,notes,owner_id)
VALUES (3000,'PLN','2020-03-12T12:30','silnik','by card','---', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (50.25,'USD','2022-05-22T09:30','skrzynia','by cash','---', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (3333.33,'EURO','2020-09-12T12:30','lusterko','by crypto','farcik', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (3333.39,'USD','2029-12-12T22:30','narty','by crypto','dobry wydatek', 'd2bc314b-d729-459c-b99d-37180d483012'),
       (367344.33,'USD','2016-02-17T19:30','porsche','by card','za dolary', 'd2bc314b-d729-459c-b99d-37180d483012');