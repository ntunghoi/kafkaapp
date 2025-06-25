INSERT INTO user_profiles(id, name, email, password) VALUES (0, 'admin', 'admin@email.com', '$2a$10$lhIOQSpSHsnOuu8ygnmoq.qPMjegMJ02pcltJo/LY1u9ivbV2plpm');

INSERT INTO user_profiles(id, name, email, password, preferred_currency) VALUES (1, 'user1', 'user1@email.com', '$2a$10$lhIOQSpSHsnOuu8ygnmoq.qPMjegMJ02pcltJo/LY1u9ivbV2plpm', 'EUR');
INSERT INTO user_profiles(id, name, email, password, preferred_currency) VALUES (2, 'user2', 'user2@email.com', '$2a$10$lhIOQSpSHsnOuu8ygnmoq.qPMjegMJ02pcltJo/LY1u9ivbV2plpm', 'CHF');
INSERT INTO user_profiles(id, name, email, password, preferred_currency) VALUES (3, 'user3', 'user3@email.com', '$2a$10$lhIOQSpSHsnOuu8ygnmoq.qPMjegMJ02pcltJo/LY1u9ivbV2plpm', 'EUR');
INSERT INTO user_profiles(id, name, email, password, preferred_currency) VALUES (4, 'user4', 'user4@email.com', '$2a$10$lhIOQSpSHsnOuu8ygnmoq.qPMjegMJ02pcltJo/LY1u9ivbV2plpm', 'GBP');

ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 5;

INSERT INTO roles(code, description) VALUES ('admin', 'administrator');
INSERT INTO roles(code, description) VALUES ('client', 'client');

INSERT INTO user_roles (user_id, role_code) VALUES (0, 'admin');
INSERT INTO user_roles (user_id, role_code) VALUES (1, 'client');
INSERT INTO user_roles (user_id, role_code) VALUES (2, 'client');
INSERT INTO user_roles (user_id, role_code) VALUES (3, 'client');
INSERT INTO user_roles (user_id, role_code) VALUES (4, 'client');

INSERT INTO exchange_rates (currency_code, rate) VALUES ('USD', 1.0);
INSERT INTO exchange_rates (currency_code, rate) VALUES ('CHF', 0.8066);
INSERT INTO exchange_rates (currency_code, rate) VALUES ('GBP', 0.7435);