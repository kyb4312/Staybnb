import http from 'k6/http';
import {check, sleep} from 'k6';
import {Trend} from 'k6/metrics';

let serviceLogicTrend = new Trend('service_logic_duration');
let sqlLogicTrend = new Trend('sql_logic_duration');

// 벤치마크 설정
export const options = {
    // 시나리오 정의 (두 엔드포인트 각각에 대한 시나리오)
    scenarios: {
        serviceLogicScenario: {
            executor: 'constant-vus', // 고정된 가상 사용자 수
            vus: 100,                 // 100명의 가상 사용자
            duration: '10s',         // 10초 동안 실행
            exec: 'updateAvailabilityServiceLogic', // 실행할 함수
            tags: {endpoint_type: 'service_logic'}, // 결과 필터링을 위한 태그
        },
        sqlLogicScenario: {
            executor: 'constant-vus',
            vus: 100,
            duration: '10s',
            exec: 'updateAvailabilitySqlLogic',
            tags: {endpoint_type: 'sql_logic'},
        },
    },
    // 기타 옵션
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내에 완료되어야 함
        'http_req_failed{endpoint_type:service_logic}': ['rate<0.01'], // 서비스 로직 엔드포인트 실패율 1% 미만
        'http_req_failed{endpoint_type:sql_logic}': ['rate<0.01'],     // SQL 로직 엔드포인트 실패율 1% 미만
    },
    noConnectionReuse: false, // 커넥션 재사용 여부 (보통 true가 기본이지만, 명시적으로 false로 두어 각 요청이 새로운 커넥션으로 테스트될 수 있도록 함)
    userAgent: 'k6/2endpoints-test',
};

// 테스트에 사용할 사용자 인증 정보 (user1@test.com ~ user250@test.com)
const USER_CREDENTIALS = new Array(250).fill(null).map((_, i) => ({
    email: `user${i + 1}@test.com`,
    password: 'password',
}));

export function setup() {
    const tokens = [];

    console.log('Fetching JWT tokens for users in setup()...');

    for (const user of USER_CREDENTIALS) {
        const res = http.post(
            'http://localhost:8080/users/login',
            JSON.stringify({
                email: user.email,
                password: user.password,
            }),
            {
                headers: {'Content-Type': 'application/json'},
            }
        );

        check(res, {
            'login status 200': (r) => r.status === 200,
        });

        if (res.status === 200) {
            tokens.push(res.body); // 응답 본문이 바로 토큰
        } else {
            console.error(`Login failed for user ${user.email}`);
            throw new Error('Failed to fetch all tokens');
        }
    }

    return {tokens};
}


// 엔드포인트 1 (서비스 레이어 로직) 호출 함수
export function updateAvailabilityServiceLogic(setupData) {
    const userId = __VU + 1;
    const roomId = userId;
    const token = setupData.tokens[userId - 1];

    // 매번 다른 날짜 범위 생성
    const dateSelected = generateRandomDateRanges(
        1, 9, // 최소 1개 ~ 최대 9개의 날짜 범위
        0, 30, // 각 날짜 범위의 최소 0일 ~ 최대 30일 지속
        0, 7 // 시작 날짜는 오늘로부터 최소 1일 ~ 최대 7일 후
    );

    const isAvailable = Math.random() < 0.5;

    const payload = JSON.stringify({
        dateSelected: dateSelected,
        isAvailable: isAvailable,
    });

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // 필요한 경우 인증 헤더 추가
    };

    const res = http.post(`http://localhost:8080/host/rooms/${roomId}/availability`, payload, {headers: headers});
    serviceLogicTrend.add(res.timings.duration);

    check(res, {
        'serviceLogic status is 200': (r) => r.status === 200,
        'serviceLogic response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1); // 각 요청 사이에 1초 대기 (사용자 행동 시뮬레이션)
}

// 엔드포인트 2 (SQL 쿼리 로직) 호출 함수
export function updateAvailabilitySqlLogic(setupData) {
    const userId = __VU + 1;
    const roomId = userId;
    const token = setupData.tokens[userId - 1];

    // 매번 다른 날짜 범위 생성
    const dateSelected = generateRandomDateRanges(
        1, 9, // 최소 1개 ~ 최대 9개의 날짜 범위
        0, 30, // 각 날짜 범위의 최소 0일 ~ 최대 30일 지속
        0, 7 // 시작 날짜는 오늘로부터 최소 1일 ~ 최대 7일 후
    );

    const isAvailable = Math.random() < 0.5;

    const payload = JSON.stringify({
        dateSelected: dateSelected,
        isAvailable: isAvailable,
    });

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // 필요한 경우 인증 헤더 추가
    };

    const res = http.post(`http://localhost:8080/host/rooms/${roomId}/availability/sql`, payload, {headers: headers});
    sqlLogicTrend.add(res.timings.duration);

    check(res, {
        'sqlLogic status is 200': (r) => r.status === 200,
        'sqlLogic response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1); // 각 요청 사이에 1초 대기
}

// Function to generate a random integer within a range
function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Function to generate a random date in the future
function getRandomFutureDate(minDaysAhead, maxDaysAhead) {
    const today = new Date();
    const randomDays = getRandomInt(minDaysAhead, maxDaysAhead);
    const futureDate = new Date(today);
    futureDate.setDate(today.getDate() + randomDays);
    return futureDate.toISOString().split('T')[0]; // YYYY-MM-DD format
}

/**
 *
 * @param minRanges 하나의 요청에 최소, 최대 몇 개의 날짜 범위를 담아 보낼 건지
 * @param maxRanges
 * @param minDurationDays 하나의 날짜 범위 duration의 최소, 최대
 * @param maxDurationDays
 * @param minStartDaysAhead 현재 날짜로부터 최소, 최대 며칠 떨어진 날짜에 대한 요청을 보낼지
 * @param maxStartDaysAhead
 * @returns {*[]}
 */
// Function to generate a list of random date ranges
function generateRandomDateRanges(minRanges, maxRanges, minDurationDays, maxDurationDays, minStartDaysAhead, maxStartDaysAhead) {
    const numRanges = getRandomInt(minRanges, maxRanges);
    const dateRanges = [];
    let lastEndDate = null;

    for (let i = 0; i < numRanges; i++) {
        let startDate;
        // Ensure start date is after the previous end date to avoid overlaps (if desired)
        if (lastEndDate) {
            // Start at least 1 day after the previous range ends
            const nextStartOffset = getRandomInt(1, 7); // Gap between ranges
            const nextStartDate = new Date(lastEndDate);
            nextStartDate.setDate(nextStartDate.getDate() + nextStartOffset);
            startDate = nextStartDate.toISOString().split('T')[0];
        } else {
            startDate = getRandomFutureDate(minStartDaysAhead, maxStartDaysAhead);
        }

        const currentStartDateObj = new Date(startDate);
        const durationDays = getRandomInt(minDurationDays, maxDurationDays);
        const endDateObj = new Date(currentStartDateObj);
        endDateObj.setDate(currentStartDateObj.getDate() + durationDays);
        const endDate = endDateObj.toISOString().split('T')[0];

        dateRanges.push({startDate: startDate, endDate: endDate});
        lastEndDate = endDateObj; // Update lastEndDate for the next iteration
    }
    return dateRanges;
}