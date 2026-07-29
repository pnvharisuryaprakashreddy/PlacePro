const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8080;

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // REST API Endpoints
  if (req.url.startsWith('/api/')) {
    res.setHeader('Content-Type', 'application/json; charset=UTF-8');

    if (req.url.endsWith('/health')) {
      res.writeHead(200);
      res.end(JSON.stringify({ status: 'UP', system: 'Campus Placement Management Portal', port: PORT }));
      return;
    }

    if (req.url.endsWith('/stats')) {
      res.writeHead(200);
      res.end(JSON.stringify({ totalPlacements: 148, placementPercentage: 86.4, avgPackage: 14.25, highestPackage: 42.0, activeDrives: 12, totalStudents: 500 }));
      return;
    }

    if (req.url.endsWith('/drives')) {
      const drives = [
        { id: 101, company: 'Microsoft India', role: 'Software Development Engineer (SDE-1)', package: '28.5 LPA', minCgpa: 8.0, maxBacklogs: 0, branches: 'CSE, ECE, IT', deadline: '2026-08-05', visitDate: '2026-08-10', status: 'PUBLISHED', desc: 'Core cloud engineering, distributed infrastructure, and microservices architecture.' },
        { id: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', package: '32.0 LPA', minCgpa: 8.5, maxBacklogs: 0, branches: 'CSE, IT', deadline: '2026-08-10', visitDate: '2026-08-15', status: 'PUBLISHED', desc: 'Kubernetes deployment automation, AI platform APIs, and enterprise cloud reliability.' },
        { id: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', package: '26.0 LPA', minCgpa: 7.5, maxBacklogs: 1, branches: 'CSE, ECE, EEE', deadline: '2026-08-12', visitDate: '2026-08-18', status: 'PUBLISHED', desc: 'Financial algorithm modelling, high-frequency data engines, and risk analysis.' },
        { id: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', package: '24.0 LPA', minCgpa: 7.0, maxBacklogs: 0, branches: 'All Branches', deadline: '2026-08-15', visitDate: '2026-08-20', status: 'PUBLISHED', desc: 'AWS serverless compute, global network routing, and edge infrastructure.' },
        { id: 105, company: 'JPMorgan Chase', role: 'Technology Analyst', package: '19.5 LPA', minCgpa: 7.2, maxBacklogs: 1, branches: 'CSE, ECE, IT', deadline: '2026-08-18', visitDate: '2026-08-25', status: 'PUBLISHED', desc: 'Payment engine security, transactional APIs, and ledger platform systems.' }
      ];
      res.writeHead(200);
      res.end(JSON.stringify(drives));
      return;
    }

    if (req.url.endsWith('/applications')) {
      const apps = [
        { id: 'APP-501', driveId: 101, company: 'Microsoft India', role: 'SDE-1', studentId: 1, studentName: 'Priya Sharma', rollNumber: '2022CSE104', branch: 'CSE', cgpa: 8.8, backlogs: 0, status: 'SHORTLISTED', appliedDate: '2026-07-25', interviewDate: '2026-08-02 10:00 AM', venue: 'Seminar Hall A' },
        { id: 'APP-502', driveId: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', studentId: 2, studentName: 'Aarav Mehta', rollNumber: '2022ECE052', branch: 'ECE', cgpa: 8.6, backlogs: 0, status: 'SELECTED', appliedDate: '2026-07-24', interviewDate: '2026-07-28', venue: 'Offer Accepted (32.0 LPA)' },
        { id: 'APP-503', driveId: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', studentId: 3, studentName: 'Rohan Verma', rollNumber: '2022IT089', branch: 'IT', cgpa: 8.1, backlogs: 0, status: 'INTERVIEW_SCHEDULED', appliedDate: '2026-07-26', interviewDate: '2026-08-04 02:30 PM', venue: 'Lab 3 / Zoom Link' },
        { id: 'APP-504', driveId: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', studentId: 4, studentName: 'Neha Kapoor', rollNumber: '2022CSE112', branch: 'CSE', cgpa: 7.8, backlogs: 0, status: 'APPLIED', appliedDate: '2026-07-27', interviewDate: 'TBD', venue: 'Under Review' }
      ];
      res.writeHead(200);
      res.end(JSON.stringify(apps));
      return;
    }
  }

  // Static Asset Server
  let filePath = path.join(__dirname, 'public', req.url === '/' ? 'index.html' : req.url);
  let extname = String(path.extname(filePath)).toLowerCase();
  let mimeTypes = {
    '.html': 'text/html; charset=UTF-8',
    '.js': 'application/javascript; charset=UTF-8',
    '.css': 'text/css; charset=UTF-8',
    '.json': 'application/json; charset=UTF-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml'
  };

  let contentType = mimeTypes[extname] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      fs.readFile(path.join(__dirname, 'public', 'index.html'), (err2, fallback) => {
        if (err2) {
          res.writeHead(404);
          res.end('404 File Not Found');
        } else {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
          res.end(fallback, 'utf-8');
        }
      });
    } else {
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content, 'utf-8');
    }
  });
});

server.listen(PORT, () => {
  console.log(`\n==================================================================`);
  console.log(`⚡ Campus Placement Portal Server Running on http://localhost:${PORT}`);
  console.log(`==================================================================\n`);
});
