CREATE
OR REPLACE PROCEDURE update_room_availability(
    IN p_room_id BIGINT,
    IN p_date_ranges DATERANGE[],
    IN p_is_available BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
r_avail RECORD;
    r_block
DATERANGE;
    remain_ranges
DATERANGE[];
BEGIN
    -- 1. 기존 데이터 중 겹치는 것만 조회
FOR r_avail IN
SELECT *
FROM availability
WHERE room_id = p_room_id
  AND EXISTS (SELECT 1
              FROM unnest(p_date_ranges) AS req
              WHERE req && availability.date_range)
    LOOP
        remain_ranges := ARRAY[r_avail.date_range];

-- 2. 차집합 수행 (정렬된 비겹침 요청 범위에 대해 순서대로 처리)
FOREACH
r_block IN ARRAY p_date_ranges LOOP
            remain_ranges := (
                SELECT array_agg(r)
                FROM (
                    SELECT * FROM unnest(remain_ranges) AS r
                    WHERE NOT r && r_block
                    UNION ALL
                    SELECT daterange(lower(r), lower(r_block), '[)')
                    FROM unnest(remain_ranges) AS r
                    WHERE lower(r) < lower(r_block) AND upper(r) > lower(r_block) AND r && r_block
                    UNION ALL
                    SELECT daterange(upper(r_block), upper(r), '[)')
                    FROM unnest(remain_ranges) AS r
                    WHERE upper(r) > upper(r_block) AND lower(r) < upper(r_block) AND r && r_block
                ) sub
                WHERE lower(sub.r) < upper(sub.r)  -- 유효 범위만
            );
END LOOP;

        -- 3. 기존 겹친 데이터 삭제
DELETE
FROM availability
WHERE id = r_avail.id;

-- 4. 겹치지 않은 부분 복원
IF
remain_ranges IS NOT NULL THEN
            INSERT INTO availability (room_id, date_range, is_available)
SELECT p_room_id, r, r_avail.is_available
FROM unnest(remain_ranges) AS r;
END IF;
END LOOP;

    -- 5. 새로 요청된 범위 저장
INSERT INTO availability (room_id, date_range, is_available)
SELECT p_room_id, r, p_is_available
FROM unnest(p_date_ranges) AS r;
END;
$$;
