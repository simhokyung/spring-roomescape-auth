INSERT INTO member (name, email, password)
VALUES ('brown', 'brown@example.com', 'password');

INSERT INTO member (name, email, password)
VALUES ('coney', 'coney@example.com', 'password');

INSERT INTO store (name)
VALUES ('gangnam');

INSERT INTO store (name)
VALUES ('jamsil');

INSERT INTO manager (member_id, store_id)
VALUES (1, 1);

INSERT INTO manager (member_id, store_id)
VALUES (2, 2);

-- 이 파일은 테스트 시에 로드되어 기존 하드코딩된 테스트가 실패하는 것을 막기 위해 존재.
SELECT 1;
