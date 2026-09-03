-- Run manually on environments where spring.jpa.hibernate.ddl-auto=validate
ALTER TABLE course_subscriptions
    ADD COLUMN payment_type VARCHAR(30) NULL,
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_PAID',
    ADD COLUMN paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
