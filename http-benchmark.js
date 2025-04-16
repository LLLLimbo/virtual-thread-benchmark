import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const responseTrend = new Trend('response_time');
const errorRate = new Rate('error_rate');
const successCounter = new Counter('success_counter');

export const options = {
    stages: [
        { duration: '30s', target: 100 },  // 预热
        { duration: '1m', target: 500 },   // 逐步增加负载
        { duration: '2m', target: 1000 },  // 稳定负载
        { duration: '30s', target: 0 },   // 降低负载
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'], // 95%的请求应该低于500ms
        'http_req_failed': ['rate<0.01'],   // 错误率应该低于1%
    },
};

const BASE_URL = 'http://localhost:8080';

export default function() {
    const ioRes = http.get(`${BASE_URL}/io_task`);
    check(ioRes, {
        'io-status-200': (r) => r.status === 200,
    });
    responseTrend.add(ioRes.timings.duration);

    if (ioRes.status === 200) {
        successCounter.add(1);
    } else {
        errorRate.add(1);
    }

    const computeRes = http.get(`${BASE_URL}/cpu_task`);
    check(computeRes, {
        'compute-status-200': (r) => r.status === 200,
    });
    responseTrend.add(computeRes.timings.duration);

    // 模拟用户思考时间
    sleep(1);
}