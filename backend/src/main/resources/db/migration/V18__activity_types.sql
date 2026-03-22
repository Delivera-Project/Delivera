CREATE TABLE activity_types (
    code       VARCHAR(50)  PRIMARY KEY,
    label_es   VARCHAR(100) NOT NULL,
    label_en   VARCHAR(100) NOT NULL,
    sort_order SMALLINT     NOT NULL DEFAULT 0
);

INSERT INTO activity_types (code, label_es, label_en, sort_order) VALUES
    ('INDUSTRY',     'Industria',     'Industry',     1),
    ('DISTRIBUTION', 'Distribución',  'Distribution', 2),
    ('FOOD',         'Alimentación',  'Food',         3),
    ('RETAIL',       'Retail',        'Retail',       4),
    ('TRANSPORT',    'Transporte',    'Transport',    5),
    ('OTHER',        'Otro',          'Other',        6);
