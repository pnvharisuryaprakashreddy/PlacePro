# 🎓 PlacePro — Product Submission Document
### Campus Placement Management Portal (TPO System)

---

## 📌 Project Overview & Metadata

| Field | Submission Details |
| :--- | :--- |
| **Project Title** | **PlacePro — Smart Campus Placement Management System** |
| **Developer Name** | P.N.V. Hari Surya Prakash Reddy |
| **Course / Program** | Final Year Capstone Project / Campus Placement Management System |
| **GitHub Repository** | [https://github.com/pnvharisuryaprakashreddy/PlacePro](https://github.com/pnvharisuryaprakashreddy/PlacePro) |
| **Live Server URL** | `http://localhost:8080` |
| **Prometheus Metrics** | `http://localhost:8080/metrics` |
| **Technology Stack** | **Node.js, Java, HTML5, Vanilla CSS3, JavaScript (ES6), Chart.js, Prometheus** |
| **Execution Script** | `chmod +x run.sh && ./run.sh` |

---

## 1. Executive Summary

**PlacePro** is a modern, centralized **Campus Training & Placement Office (TPO) Management System** designed to automate and streamline the entire university placement lifecycle — from student registration and corporate onboarding to drive publication, real-time eligibility verification, candidate shortlisting, interview round tracking, and institutional accreditation reporting.

PlacePro replaces manual record-keeping with a single, role-based **Campus TPO Portal**. It provides distinct workspace portals tailored for four key stakeholders in the campus placement ecosystem:
- 🎓 **Student Portal**: Profile setup, drive discovery, live eligibility engine, 1-click application submission, and interview timeline tracking.
- 🏛️ **Placement Officer Console**: Company onboarding, drive publication, student review queue, candidate shortlisting, and interview scheduling.
- 💼 **Corporate Recruiter Portal**: Company-scoped access, candidate resume inspection, interview evaluation, and hiring decision recording (`SELECTED`, `REJECTED`, `ON_HOLD`).
- ⚡ **System Administrator Console**: Department placement percentage bar charts, salary band doughnut graphs, user access governance, and Prometheus telemetry.

---

## 2. Problem Statement & Core Value Proposition

### The Problem in Existing University Systems
In most colleges and universities today, campus placement operations are managed through disconnected spreadsheets, email threads, physical registers, and informal messaging groups. This manual workflow creates severe operational challenges:
1. **Spreadsheet Chaos & Human Error**: Placement officers spend days cross-referencing student CGPA, active backlogs, and branch eligibility by hand for every company drive.
2. **Lack of Transparency for Students**: Students miss drive deadlines, waste time applying to drives for which they are ineligible, and have no visibility into their application status.
3. **Disorganized Recruiter Coordination**: Corporate recruiters receive shortlists in inconsistent formats through email threads, making interview scheduling and candidate evaluation tedious.
4. **Difficult Accreditation & Audit Reporting**: Compiling end-of-season placement percentages, branch-wise statistics, and salary package summaries takes weeks of manual aggregation.

### The PlacePro Solution
PlacePro delivers a 100% automated, rule-based web platform that guarantees:
- ⚡ **Zero-Ineligible Application Submissions**: The rule engine verifies student CGPA, branch, and backlog limits before unlocking the application button.
- 🎯 **Transparent Candidate Timeline**: Students track their progress live (`APPLIED` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED`).
- 📊 **Real-Time Observability**: Built-in Prometheus metrics exposition on `/metrics` logs login attempts, student registrations by branch, interview rounds, and hiring outcomes.

---

## 3. Technology Stack & Component Specifications

PlacePro is engineered with a **Zero-Dependency Architecture** to ensure zero complex installation overhead, high performance, and total reliability on any operating system.

| Component / Layer | Technology Used | Functionality & Scope |
| :--- | :--- | :--- |
| **Backend Web Service** | **Node.js & Java** | **The Backend Engine**: Powered by a lightweight web engine (`server.js` / `Server.java`) that handles REST API requests, processes student eligibility rules, and serves Prometheus `/metrics` on `http://localhost:8080`. |
| **Execution Script** | **Shell Script (`run.sh`)** | **One-Click Runner**: A simple 1-click launcher script (`./run.sh`) that starts the web server. |
| **User Interface (Frontend)** | **HTML5, CSS3, JavaScript (ES6)** | **The Visual Portal**: <br>• **HTML**: Creates page structures.<br>• **CSS**: Styles the app with a dark theme & glassmorphic cards.<br>• **JavaScript**: Handles student eligibility checks & popup modals dynamically. |
| **Analytics & Monitoring** | **Chart.js & Prometheus** | **Visual Charts & Metrics**: Interactive bar graphs, doughnut charts, and Prometheus log observability (`/metrics`). |
| **Data Storage & Flow** | **REST API & Local Storage** | **Data Management**: <br>• **REST API**: Communicates data between server and browser.<br>• **Local Storage**: Saves student profiles, drive applications, and recruiter decisions in the browser. |

---

## 4. Key Modules & Functional Workflows

### 🎓 Module 1: Student Self-Service Portal
- **Academic Profile Setup**: Roll Number, Branch (CSE, ECE, IT, EEE, MECH, CIVIL), CGPA (0–10), Active Backlogs.
- **Drive Discovery**: Browse active drives (Microsoft, Google, Goldman Sachs, Amazon) with package LPA and cutoff criteria.
- **Live Eligibility Checker**: Instant comparison of profile against company requirements with visual feedback (`✅ ELIGIBLE TO APPLY`).
- **Application Tracker**: Real-time status update timeline.

### 🏛️ Module 2: Placement Officer (TPO) Operations Console
- **Drive Publisher**: Create and publish drives with package LPA, CGPA cutoffs, max backlogs, and allowed branches.
- **Review & Shortlisting Queue**: Filter student applications by drive and click **Shortlist ⭐** or **Reject**.
- **Interview Scheduler**: Coordinate aptitude tests, technical interviews, and HR rounds.

### 💼 Module 3: Corporate Recruiter Portal
- **Company-Scoped Workspace**: Recruiters log in with company credentials (e.g. Microsoft India) and view only relevant candidates.
- **Evaluation Matrix**: Inspect resumes, record candidate scores, and enter final hiring decisions (`SELECTED`, `REJECTED`, `ON_HOLD`).

### ⚡ Module 4: System Administrator & Governance Console
- **Placement Analytics**: Chart.js bar graphs showing department placement rates (CSE, ECE, IT, EEE, MECH, CIVIL).
- **Salary Band Analytics**: Doughnut graphs showing offer distribution (Highest: 42.0 LPA, Average: 14.25 LPA).
- **Observability**: Prometheus metrics monitoring login events, interview rounds, and system health.

---

## 🚀 5. How to Run & Verify

1. **Clone Repository**:
   ```bash
   git clone https://github.com/pnvharisuryaprakashreddy/PlacePro.git
   cd PlacePro
   ```

2. **Start Server**:
   ```bash
   ./run.sh
   ```

3. **Access Endpoints**:
   - **Web Portal**: [http://localhost:8080](http://localhost:8080)
   - **Prometheus Metrics**: [http://localhost:8080/metrics](http://localhost:8080/metrics)

---

## 📜 6. Conclusion & Submission Declaration
PlacePro successfully fulfills all requirements for a centralized, modern Campus Placement Portal with automated eligibility enforcement, multi-role separation, real-time analytics, and Prometheus observability.

**Submitted by:** P.N.V. Hari Surya Prakash Reddy  
**GitHub Repository:** [https://github.com/pnvharisuryaprakashreddy/PlacePro](https://github.com/pnvharisuryaprakashreddy/PlacePro)
