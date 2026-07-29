// PlacePro Campus TPO Placement Portal Engine

// State Store
let currentUser = null;
let currentRole = null; // 'student', 'officer', 'recruiter', 'admin'
let studentsList = [];
let drivesList = [];
let applicationsList = [];
let companiesList = [];

let activeModalDrive = null;
let activeEvalApp = null;

let adminDeptChart = null;
let adminCompanyChart = null;

// Initial Seed Data
const initialStudents = [
  { id: 1, name: 'Priya Sharma', rollNumber: '2022CSE104', branch: 'CSE', cgpa: 8.45, backlogs: 0, email: 'priya.sharma@student.college.edu' },
  { id: 2, name: 'Aarav Mehta', rollNumber: '2022ECE052', branch: 'ECE', cgpa: 8.60, backlogs: 0, email: 'aarav.mehta@student.college.edu' },
  { id: 3, name: 'Rohan Verma', rollNumber: '2022IT089', branch: 'IT', cgpa: 8.10, backlogs: 0, email: 'rohan.verma@student.college.edu' },
  { id: 4, name: 'Neha Kapoor', rollNumber: '2022CSE112', branch: 'CSE', cgpa: 7.80, backlogs: 0, email: 'neha.kapoor@student.college.edu' }
];

const initialDrives = [
  { id: 101, company: 'Microsoft India', role: 'Software Development Engineer (SDE-1)', package: '28.5 LPA', minCgpa: 8.0, maxBacklogs: 0, branches: 'CSE, ECE, IT', deadline: '2026-08-05', visitDate: '2026-08-10', status: 'PUBLISHED', desc: 'Core cloud engineering, distributed infrastructure, and microservices architecture.' },
  { id: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', package: '32.0 LPA', minCgpa: 8.5, maxBacklogs: 0, branches: 'CSE, IT', deadline: '2026-08-10', visitDate: '2026-08-15', status: 'PUBLISHED', desc: 'Kubernetes deployment automation, AI platform APIs, and enterprise cloud reliability.' },
  { id: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', package: '26.0 LPA', minCgpa: 7.5, maxBacklogs: 1, branches: 'CSE, ECE, EEE', deadline: '2026-08-12', visitDate: '2026-08-18', status: 'PUBLISHED', desc: 'Financial algorithm modelling, high-frequency data engines, and risk analysis.' },
  { id: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', package: '24.0 LPA', minCgpa: 7.0, maxBacklogs: 0, branches: 'All Branches', deadline: '2026-08-15', visitDate: '2026-08-20', status: 'PUBLISHED', desc: 'AWS serverless compute, global network routing, and edge infrastructure.' },
  { id: 105, company: 'JPMorgan Chase', role: 'Technology Analyst', package: '19.5 LPA', minCgpa: 7.2, maxBacklogs: 1, branches: 'CSE, ECE, IT', deadline: '2026-08-18', visitDate: '2026-08-25', status: 'PUBLISHED', desc: 'Payment engine security, transactional APIs, and ledger platform systems.' }
];

const initialApplications = [
  { id: 'APP-501', driveId: 101, company: 'Microsoft India', role: 'SDE-1', studentId: 1, studentName: 'Priya Sharma', rollNumber: '2022CSE104', branch: 'CSE', cgpa: 8.8, backlogs: 0, status: 'SHORTLISTED', appliedDate: '2026-07-25', interviewDate: '2026-08-02 10:00 AM', venue: 'Seminar Hall A' },
  { id: 'APP-502', driveId: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', studentId: 2, studentName: 'Aarav Mehta', rollNumber: '2022ECE052', branch: 'ECE', cgpa: 8.6, backlogs: 0, status: 'SELECTED', appliedDate: '2026-07-24', interviewDate: '2026-07-28', venue: 'Offer Accepted (32.0 LPA)' },
  { id: 'APP-503', driveId: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', studentId: 3, studentName: 'Rohan Verma', rollNumber: '2022IT089', branch: 'IT', cgpa: 8.1, backlogs: 0, status: 'INTERVIEW_SCHEDULED', appliedDate: '2026-07-26', interviewDate: '2026-08-04 02:30 PM', venue: 'Lab 3 / Zoom Link' },
  { id: 'APP-504', driveId: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', studentId: 4, studentName: 'Neha Kapoor', rollNumber: '2022CSE112', branch: 'CSE', cgpa: 7.8, backlogs: 0, status: 'APPLIED', appliedDate: '2026-07-27', interviewDate: 'TBD', venue: 'Under Review' }
];

document.addEventListener('DOMContentLoaded', () => {
  loadData();
  showAuthScreen('landing');
});

function loadData() {
  const savedStudents = localStorage.getItem('placepro_students');
  studentsList = savedStudents ? JSON.parse(savedStudents) : initialStudents;

  const savedDrives = localStorage.getItem('placepro_drives');
  drivesList = savedDrives ? JSON.parse(savedDrives) : initialDrives;

  const savedApps = localStorage.getItem('placepro_apps');
  applicationsList = savedApps ? JSON.parse(savedApps) : initialApplications;
}

function saveData() {
  localStorage.setItem('placepro_students', JSON.stringify(studentsList));
  localStorage.setItem('placepro_drives', JSON.stringify(drivesList));
  localStorage.setItem('placepro_apps', JSON.stringify(applicationsList));
}

// Navigation & Auth Flow
function showAuthScreen(screen) {
  document.getElementById('landingView').style.display = 'none';
  document.getElementById('studentLoginView').style.display = 'none';
  document.getElementById('studentRegisterView').style.display = 'none';
  document.getElementById('officerLoginView').style.display = 'none';
  document.getElementById('recruiterLoginView').style.display = 'none';
  document.getElementById('adminLoginView').style.display = 'none';
  document.getElementById('appHeader').style.display = 'none';
  hideAllDashboards();

  if (screen === 'landing') {
    document.getElementById('landingView').style.display = 'flex';
  } else if (screen === 'student-login') {
    document.getElementById('studentLoginView').style.display = 'flex';
  } else if (screen === 'student-register') {
    document.getElementById('studentRegisterView').style.display = 'flex';
  } else if (screen === 'officer-login') {
    document.getElementById('officerLoginView').style.display = 'flex';
  } else if (screen === 'recruiter-login') {
    document.getElementById('recruiterLoginView').style.display = 'flex';
  } else if (screen === 'admin-login') {
    document.getElementById('adminLoginView').style.display = 'flex';
  }
}

function hideAllDashboards() {
  document.getElementById('studentDashboard').style.display = 'none';
  document.getElementById('officerDashboard').style.display = 'none';
  document.getElementById('recruiterDashboard').style.display = 'none';
  document.getElementById('adminDashboard').style.display = 'none';
}

function logout() {
  currentUser = null;
  currentRole = null;
  showAuthScreen('landing');
}

// Student Login & Register Handlers with Dynamic Student Search
function handleStudentLogin(e) {
  e.preventDefault();
  const emailInput = document.getElementById('studentEmail').value.trim();

  // Find student matching email in saved students store
  let foundStudent = studentsList.find(s => s.email.toLowerCase() === emailInput.toLowerCase());

  if (!foundStudent) {
    // Generate user profile dynamically based on input email
    const nameFromEmail = emailInput.split('@')[0].replace('.', ' ').replace(/\b\w/g, c => c.toUpperCase());
    const rollFromEmail = '2022' + (emailInput.substring(0, 3).toUpperCase()) + Math.floor(100 + Math.random() * 900);

    foundStudent = {
      id: Date.now(),
      name: nameFromEmail || 'Student Candidate',
      rollNumber: rollFromEmail,
      branch: 'CSE',
      cgpa: 8.20,
      backlogs: 0,
      email: emailInput
    };
    studentsList.push(foundStudent);
    saveData();
  }

  currentUser = foundStudent;
  currentRole = 'student';
  launchDashboard();
}

function handleStudentRegister(e) {
  e.preventDefault();

  const name = document.getElementById('regName').value.trim();
  const rollNumber = document.getElementById('regRoll').value.trim();
  const branch = document.getElementById('regBranch').value;
  const cgpa = parseFloat(document.getElementById('regCgpa').value);
  const backlogs = parseInt(document.getElementById('regBacklogs').value);
  const email = document.getElementById('regEmail').value.trim();

  // Create new registered student object
  const newStudent = {
    id: Date.now(),
    name: name,
    rollNumber: rollNumber,
    branch: branch,
    cgpa: cgpa,
    backlogs: backlogs,
    email: email
  };

  // Add to students list and persist
  studentsList.unshift(newStudent);
  saveData();

  currentUser = newStudent;
  currentRole = 'student';
  launchDashboard();

  alert(`🎉 Welcome ${newStudent.name}! Your student registration is complete.`);
}

function handleOfficerLogin(e) {
  e.preventDefault();
  currentUser = { id: 10, name: 'Dr. Anita Rao', roleName: 'Head Placement Officer' };
  currentRole = 'officer';
  launchDashboard();
}

function handleRecruiterLogin(e) {
  e.preventDefault();
  const company = document.getElementById('recruiterCompanySelect').value;
  const email = document.getElementById('recruiterEmail').value;
  const nameFromEmail = email.split('@')[0].replace('.', ' ').replace(/\b\w/g, c => c.toUpperCase());

  currentUser = { id: 20, name: nameFromEmail || 'Corporate Recruiter', company: company };
  currentRole = 'recruiter';
  launchDashboard();
}

function handleAdminLogin(e) {
  e.preventDefault();
  currentUser = { id: 30, name: 'Rahul Mehta', roleName: 'System Administrator' };
  currentRole = 'admin';
  launchDashboard();
}

// Launch Dashboard after login
function launchDashboard() {
  document.getElementById('landingView').style.display = 'none';
  document.getElementById('studentLoginView').style.display = 'none';
  document.getElementById('studentRegisterView').style.display = 'none';
  document.getElementById('officerLoginView').style.display = 'none';
  document.getElementById('recruiterLoginView').style.display = 'none';
  document.getElementById('adminLoginView').style.display = 'none';

  document.getElementById('appHeader').style.display = 'flex';
  hideAllDashboards();

  // Header Details
  document.getElementById('sessionUserName').innerText = currentUser.name;
  if (currentRole === 'student') {
    document.getElementById('headerRoleBadge').innerText = 'Student Portal';
    document.getElementById('sessionUserDetail').innerText = `${currentUser.rollNumber} • ${currentUser.branch}`;
    setupStudentDashboard();
  } else if (currentRole === 'officer') {
    document.getElementById('headerRoleBadge').innerText = 'Placement Officer';
    document.getElementById('sessionUserDetail').innerText = 'Training & Placement Office';
    setupOfficerDashboard();
  } else if (currentRole === 'recruiter') {
    document.getElementById('headerRoleBadge').innerText = 'Recruiter Portal';
    document.getElementById('sessionUserDetail').innerText = currentUser.company;
    setupRecruiterDashboard();
  } else if (currentRole === 'admin') {
    document.getElementById('headerRoleBadge').innerText = 'Admin Governance';
    document.getElementById('sessionUserDetail').innerText = 'IT Dean Office';
    setupAdminDashboard();
  }
}

// Student Dashboard Setup
function setupStudentDashboard() {
  document.getElementById('studentDashboard').style.display = 'block';
  document.getElementById('stuProfileName').innerText = currentUser.name;
  document.getElementById('stuProfileMeta').innerText = `Roll No: ${currentUser.rollNumber} • ${currentUser.branch} • Email: ${currentUser.email}`;
  document.getElementById('stuCgpaDisplay').innerText = currentUser.cgpa;
  document.getElementById('stuBacklogDisplay').innerText = currentUser.backlogs;

  renderStudentDrives(drivesList);
  renderStudentApps();
}

function renderStudentDrives(drives) {
  const tbody = document.getElementById('studentDrivesTbody');
  tbody.innerHTML = '';

  drives.forEach(d => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight:700; color:#fff;">${d.company}</td>
      <td>${d.role}</td>
      <td style="font-weight:600; color: var(--accent-cyan);">${d.package}</td>
      <td>${d.minCgpa} CGPA</td>
      <td>${d.maxBacklogs}</td>
      <td>${d.branches}</td>
      <td>${d.deadline}</td>
      <td><button class="btn btn-secondary" onclick="openStudentDriveModal(${d.id})">Inspect & Check Eligibility</button></td>
    `;
    tbody.appendChild(tr);
  });
}

function filterStudentDrives() {
  const query = document.getElementById('stuSearchDrive').value.toLowerCase();
  const filtered = drivesList.filter(d => 
    d.company.toLowerCase().includes(query) ||
    d.role.toLowerCase().includes(query) ||
    d.branches.toLowerCase().includes(query)
  );
  renderStudentDrives(filtered);
}

function renderStudentApps() {
  const tbody = document.getElementById('studentAppsTbody');
  tbody.innerHTML = '';

  const myApps = applicationsList.filter(a => a.studentId === currentUser.id || a.rollNumber === currentUser.rollNumber || a.studentName.toLowerCase() === currentUser.name.toLowerCase());
  if (myApps.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="muted" style="text-align:center;">No drive applications submitted yet. Click any drive above to verify eligibility & apply.</td></tr>';
    return;
  }

  myApps.forEach(a => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight:600; color:var(--text-muted);">${a.id}</td>
      <td style="font-weight:700; color:#fff;">${a.company}</td>
      <td>${a.role}</td>
      <td>${a.appliedDate}</td>
      <td><span class="badge ${getBadgeClass(a.status)}">${a.status}</span></td>
      <td style="font-weight:600; color:var(--success);">${a.venue || a.interviewDate}</td>
    `;
    tbody.appendChild(tr);
  });
}

// Student Drive Eligibility & Apply Modal
function openStudentDriveModal(driveId) {
  activeModalDrive = drivesList.find(d => d.id === driveId);
  if (!activeModalDrive) return;

  document.getElementById('sdModalCompany').innerText = activeModalDrive.company;
  document.getElementById('sdModalRole').innerText = activeModalDrive.role;
  document.getElementById('sdModalPackage').innerText = activeModalDrive.package;
  document.getElementById('sdModalCgpa').innerText = activeModalDrive.minCgpa + ' CGPA';
  document.getElementById('sdModalBacklogs').innerText = activeModalDrive.maxBacklogs;
  document.getElementById('sdModalBranches').innerText = activeModalDrive.branches;
  document.getElementById('sdModalDeadline').innerText = activeModalDrive.deadline;
  document.getElementById('sdModalVisit').innerText = activeModalDrive.visitDate || 'TBD';
  document.getElementById('sdModalDesc').innerText = activeModalDrive.desc;

  const box = document.getElementById('eligibilityResultBox');
  box.innerHTML = '⚡ Click "Verify Eligibility Rules" to compare your profile against drive requirements.';
  box.style.borderColor = 'rgba(79, 70, 229, 0.3)';
  document.getElementById('btnSubmitApp').style.display = 'none';

  document.getElementById('driveDetailModal').style.display = 'flex';
}

function closeDriveModal() {
  document.getElementById('driveDetailModal').style.display = 'none';
}

function runEligibilityCheck() {
  if (!activeModalDrive || !currentUser) return;
  const box = document.getElementById('eligibilityResultBox');

  const cgpaOk = currentUser.cgpa >= activeModalDrive.minCgpa;
  const backlogsOk = currentUser.backlogs <= activeModalDrive.maxBacklogs;
  const branchOk = activeModalDrive.branches.includes('All') || activeModalDrive.branches.includes(currentUser.branch);

  if (cgpaOk && backlogsOk && branchOk) {
    box.innerHTML = `✅ <strong>ELIGIBLE TO APPLY:</strong><br>• CGPA (${currentUser.cgpa} ≥ ${activeModalDrive.minCgpa})<br>• Backlogs (${currentUser.backlogs} ≤ ${activeModalDrive.maxBacklogs})<br>• Branch (${currentUser.branch} matches ${activeModalDrive.branches})`;
    box.style.borderColor = 'var(--success)';
    document.getElementById('btnSubmitApp').style.display = 'inline-flex';
  } else {
    let reason = [];
    if (!cgpaOk) reason.push(`Your CGPA (${currentUser.cgpa}) is below cutoff ${activeModalDrive.minCgpa}`);
    if (!backlogsOk) reason.push(`Your Active Backlogs (${currentUser.backlogs}) exceed max limit ${activeModalDrive.maxBacklogs}`);
    if (!branchOk) reason.push(`Your Branch (${currentUser.branch}) is not listed in allowed branches (${activeModalDrive.branches})`);

    box.innerHTML = `❌ <strong>NOT ELIGIBLE:</strong><br>• ` + reason.join('<br>• ');
    box.style.borderColor = 'var(--danger)';
    document.getElementById('btnSubmitApp').style.display = 'none';
  }
}

function submitStudentApplication() {
  if (!activeModalDrive || !currentUser) return;

  const existing = applicationsList.find(a => a.driveId === activeModalDrive.id && (a.studentId === currentUser.id || a.rollNumber === currentUser.rollNumber || a.studentName.toLowerCase() === currentUser.name.toLowerCase()));
  if (existing) {
    alert('⚠️ You have already submitted an application for this drive!');
    closeDriveModal();
    return;
  }

  const newApp = {
    id: 'APP-' + Math.floor(500 + Math.random() * 500),
    driveId: activeModalDrive.id,
    company: activeModalDrive.company,
    role: activeModalDrive.role,
    studentId: currentUser.id,
    studentName: currentUser.name,
    rollNumber: currentUser.rollNumber,
    branch: currentUser.branch,
    cgpa: currentUser.cgpa,
    backlogs: currentUser.backlogs,
    status: 'APPLIED',
    appliedDate: new Date().toISOString().split('T')[0],
    interviewDate: 'TBD',
    venue: 'Under Officer Review'
  };

  applicationsList.unshift(newApp);
  saveData();
  renderStudentApps();
  closeDriveModal();
  alert('🚀 Application Submitted Successfully!\nReference Code: #' + newApp.id);
}

// Officer Dashboard Setup
function setupOfficerDashboard() {
  document.getElementById('officerDashboard').style.display = 'block';
  renderOfficerDrives();
  renderOfficerReviewQueue();
}

function renderOfficerDrives() {
  const tbody = document.getElementById('officerDrivesTbody');
  tbody.innerHTML = '';

  drivesList.forEach(d => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight:600; color:var(--text-muted);">DRV-${d.id}</td>
      <td style="font-weight:700; color:#fff;">${d.company}</td>
      <td>${d.role}</td>
      <td style="font-weight:600; color:var(--accent-cyan);">${d.package}</td>
      <td>${d.minCgpa} CGPA</td>
      <td>${d.branches}</td>
      <td>${d.deadline}</td>
      <td><span class="badge ${getBadgeClass(d.status)}">${d.status}</span></td>
      <td><button class="btn btn-secondary" onclick="alert('Drive Settings Updated')">Edit Drive</button></td>
    `;
    tbody.appendChild(tr);
  });
}

function renderOfficerReviewQueue() {
  const tbody = document.getElementById('officerReviewTbody');
  tbody.innerHTML = '';

  applicationsList.forEach(a => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight:600; color:var(--text-muted);">${a.id}</td>
      <td style="font-weight:700; color:#fff;">${a.studentName}</td>
      <td>${a.rollNumber}</td>
      <td>${a.branch}</td>
      <td style="font-weight:600;">${a.cgpa}</td>
      <td>${a.company} (${a.role})</td>
      <td><span class="badge ${getBadgeClass(a.status)}">${a.status}</span></td>
      <td>
        <button class="btn btn-primary" onclick="officerShortlistApp('${a.id}')">Shortlist ⭐</button>
        <button class="btn btn-secondary" onclick="officerRejectApp('${a.id}')">Reject</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function officerShortlistApp(appId) {
  const app = applicationsList.find(a => a.id === appId);
  if (app) {
    app.status = 'SHORTLISTED';
    app.venue = 'Shortlisted for Interview Round';
    saveData();
    renderOfficerReviewQueue();
    alert(`⭐ Candidate ${app.studentName} shortlisted for ${app.company}!`);
  }
}

function officerRejectApp(appId) {
  const app = applicationsList.find(a => a.id === appId);
  if (app) {
    app.status = 'REJECTED';
    app.venue = 'Not Recommended';
    saveData();
    renderOfficerReviewQueue();
  }
}

function openCreateDriveModal() {
  document.getElementById('createDriveModal').style.display = 'flex';
}
function closeCreateDriveModal() {
  document.getElementById('createDriveModal').style.display = 'none';
}

function handleCreateDrive(e) {
  e.preventDefault();
  const newDrive = {
    id: Math.floor(100 + Math.random() * 900),
    company: document.getElementById('cdCompany').value.trim(),
    role: document.getElementById('cdRole').value.trim(),
    package: document.getElementById('cdPackage').value.trim(),
    minCgpa: parseFloat(document.getElementById('cdCgpa').value),
    maxBacklogs: parseInt(document.getElementById('cdBacklogs').value),
    branches: document.getElementById('cdBranches').value.trim(),
    deadline: document.getElementById('cdDeadline').value,
    status: 'PUBLISHED',
    desc: 'Campus recruitment drive published by Placement Cell.'
  };

  drivesList.unshift(newDrive);
  saveData();
  renderOfficerDrives();
  closeCreateDriveModal();
  alert('⚡ New Placement Drive Published Successfully!');
}

function openAddCompanyModal() {
  const comp = prompt('Enter Company Name to Onboard:');
  if (comp) {
    alert(`🏢 Company "${comp}" Onboarded Successfully!`);
  }
}

// Recruiter Dashboard Setup
function setupRecruiterDashboard() {
  document.getElementById('recruiterDashboard').style.display = 'block';
  document.getElementById('recruiterCompanyHeader').innerText = `💼 Corporate Recruiter Portal — ${currentUser.company}`;

  renderRecruiterCandidates();
}

function renderRecruiterCandidates() {
  const tbody = document.getElementById('recruiterCandidatesTbody');
  tbody.innerHTML = '';

  const candidates = applicationsList.filter(a => a.company === currentUser.company);
  if (candidates.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="muted" style="text-align:center;">No candidate applications for ' + currentUser.company + ' yet.</td></tr>';
    return;
  }

  candidates.forEach(c => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight:600; color:var(--text-muted);">${c.id}</td>
      <td style="font-weight:700; color:#fff;">${c.studentName}</td>
      <td>${c.rollNumber}</td>
      <td>${c.branch}</td>
      <td style="font-weight:600;">${c.cgpa}</td>
      <td>${c.role}</td>
      <td><span class="badge ${getBadgeClass(c.status)}">${c.status}</span></td>
      <td><button class="btn btn-primary" onclick="openEvalModal('${c.id}')">Evaluate Candidate →</button></td>
    `;
    tbody.appendChild(tr);
  });
}

function openEvalModal(appId) {
  activeEvalApp = applicationsList.find(a => a.id === appId);
  if (!activeEvalApp) return;

  document.getElementById('evalCandidateName').innerText = `${activeEvalApp.studentName} (${activeEvalApp.rollNumber})`;
  document.getElementById('evalDriveInfo').innerText = `${activeEvalApp.company} • ${activeEvalApp.role} • CGPA: ${activeEvalApp.cgpa}`;

  document.getElementById('evalCandidateModal').style.display = 'flex';
}

function closeEvalModal() {
  document.getElementById('evalCandidateModal').style.display = 'none';
}

function handleRecordEvaluation(e) {
  e.preventDefault();
  if (!activeEvalApp) return;

  const decision = document.getElementById('evalDecision').value;
  activeEvalApp.status = decision;

  if (decision === 'SELECTED') {
    activeEvalApp.venue = 'Offer Confirmed 🎉';
  } else if (decision === 'REJECTED') {
    activeEvalApp.venue = 'Evaluation Completed';
  } else {
    activeEvalApp.venue = 'Waitlist On Hold';
  }

  saveData();
  renderRecruiterCandidates();
  closeEvalModal();
  alert(`Decision Saved: Candidate ${activeEvalApp.studentName} marked as ${decision}!`);
}

// Admin Dashboard Setup
function setupAdminDashboard() {
  document.getElementById('adminDashboard').style.display = 'block';
  initAdminCharts();
}

function initAdminCharts() {
  const ctx1 = document.getElementById('adminDeptChart');
  if (ctx1 && !adminDeptChart) {
    adminDeptChart = new Chart(ctx1.getContext('2d'), {
      type: 'bar',
      data: {
        labels: ['CSE', 'ECE', 'EEE', 'MECH', 'CIVIL', 'IT'],
        datasets: [{
          label: 'Placement %',
          data: [94.2, 88.5, 82.0, 76.4, 71.0, 92.8],
          backgroundColor: '#4f46e5',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#94a3b8' } },
          y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } }
        }
      }
    });
  }

  const ctx2 = document.getElementById('adminCompanyChart');
  if (ctx2 && !adminCompanyChart) {
    adminCompanyChart = new Chart(ctx2.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: ['Microsoft', 'Google', 'Amazon', 'Goldman Sachs', 'Others'],
        datasets: [{
          data: [35, 25, 20, 15, 45],
          backgroundColor: ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b', '#64748b'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', font: { size: 11 } } } }
      }
    });
  }
}

function getBadgeClass(status) {
  if (status === 'SELECTED' || status === 'PUBLISHED') return 'badge-success';
  if (status === 'SHORTLISTED' || status === 'INTERVIEW_SCHEDULED') return 'badge-warning';
  if (status === 'REJECTED') return 'badge-danger';
  return 'badge-primary';
}
