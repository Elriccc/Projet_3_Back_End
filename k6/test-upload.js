import http from 'k6/http';
import { sleep, check } from 'k6';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

const crashImage = open('crash.png', 'b');
const url = 'http://localhost:8081/api/files'

// test configuration
export const options = {
  vus: 10,
  duration: '30s',

  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete under 500ms
  }
};

// test scenario
export default function() {
  const fd = new FormData();
  fd.append("expirationTime", "7")
  fd.append("file", http.file(crashImage, 'crash.png', 'image/png'));
  let res = http.post(url, fd.body(), {
      headers: { 'Content-Type': 'multipart/form-data; boundary=' + fd.boundary },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'response time is acceptable': (r) => r.timings.duration < 1000
  });

  // simulate user activity
  sleep(1);
}