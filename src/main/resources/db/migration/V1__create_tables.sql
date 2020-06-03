DROP TABLE IF EXISTS year_expenses;
DROP TABLE IF EXISTS month_expenses;
DROP TABLE IF EXISTS expenses_category;
DROP TABLE IF EXISTS expenses_process;

CREATE TABLE year_expenses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    year VARCHAR(15) NOT NULL,
    description VARCHAR(200)
);
CREATE TABLE month_expenses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    month VARCHAR(15) NOT NULL,
    description VARCHAR(200),
    year_id INT,
    FOREIGN KEY (year_id) REFERENCES year_expenses (id)
);
CREATE TABLE expenses_category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    month_id INT,
    FOREIGN KEY (month_id) REFERENCES month_expenses (id)
);
CREATE TABLE expenses_process (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    description VARCHAR(200) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    currency VARCHAR(6),
    transaction_type VARCHAR(15),
    buy_date DATETIME NOT NULL,
    notes VARCHAR(300),
    created_on DATETIME NULL,
    updated_on DATETIME NULL,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES expenses_category (id)
);


DROP TABLE IF EXISTS year_salary;
DROP TABLE IF EXISTS month_salary;

CREATE TABLE year_salary (
    id INT PRIMARY KEY AUTO_INCREMENT,
    year DATETIME,
    description VARCHAR(200)
);
CREATE TABLE month_salary (
    id INT PRIMARY KEY AUTO_INCREMENT,
    month DATETIME,
    netto_price DECIMAL(15,2),
    currency VARCHAR(6) NOT NULL,
    VAT_rate DECIMAL(3,2) NOT NULL,
    company VARCHAR(100),
    notes VARCHAR(200),
    year_id INT,
    FOREIGN KEY (year_id) REFERENCES year_salary (id)
);