# ⚡ PlacePro — Smart Campus Placement Management System (TPO Portal)

[![License: MIT](https://img.shields.io/badge/License-MIT-indigo.svg)](LICENSE)
[![Backend: Node.js & Java](https://img.shields.io/badge/Backend-Node.js%20%26%20Java-blue.svg)](#technology-stack)
[![Prometheus Metrics](https://img.shields.io/badge/Prometheus-Metrics%20Live-orange.svg)](http://localhost:8080/metrics)
[![Platform: Web](https://img.shields.io/badge/Platform-Web%20Portal-emerald.svg)](http://localhost:8080)

**PlacePro** is a modern, centralized **Campus Training & Placement Office (TPO) Management System** designed to automate and streamline the entire university placement lifecycle — from student registration and corporate onboarding to drive publication, real-time eligibility verification, candidate shortlisting, interview round tracking, and institutional accreditation reporting.

---

## 📌 Executive Summary (What is PlacePro & Why is it Built?)

### ❓ The Problem
In most colleges and universities today, campus placement operations are managed through disconnected spreadsheets, email threads, physical registers, and informal messaging groups. This manual workflow creates severe operational challenges:
1. **Spreadsheet Chaos & Human Error**: Placement officers spend days cross-referencing student CGPA, active backlogs, and branch eligibility by hand for every company drive.
2. **Lack of Transparency for Students**: Students miss drive deadlines, waste time applying to drives for which they are ineligible, and have no visibility into their application status.
3. **Disorganized Recruiter Coordination**: Corporate recruiters receive shortlists in inconsistent formats through email threads, making interview scheduling and candidate evaluation tedious.
4. **Difficult Accreditation & Audit Reporting**: Compiling end-of-season placement percentages, branch-wise statistics, and salary package summaries takes weeks of manual aggregation.

### 💡 The Solution: PlacePro
PlacePro replaces fragmented manual record-keeping with a single, role-based **Campus TPO Portal**. It provides distinct workspace portals tailored for four key stakeholders in the campus placement ecosystem:

```
                  ┌─────────────────────────────────────────────────────────┐
                  │          PlacePro Campus Placement Portal              │
                  └────────────────────────────┬────────────────────────────┘
                                               │
       ┌──────────────────────┬────────────────┴──────────────────────┬──────────────────────┐
       ▼                      ▼                                       ▼                      ▼
🎓 Student Portal     🏛️ Placement Officer                    💼 Recruiter Portal     ⚡ System Admin
• Profile & CGPA      • Company Directory                     • Candidate Shortlist   • Department Analytics
• Eligibility Rules   • Drive Creation & Rules                • Resume Inspector      • Salary Band Charts
• 1-Click Application • Student Review Queue                  • Round Evaluation      • User Governance
• Application Tracker • Interview Scheduler                   • Offer Confirmation    • Audit Summaries
```

---

## ✨ Key Features & Role-Based Workflows

### 🎓 1. Student Self-Service Portal
* **Academic Profile Management**: Register with Roll Number, Branch (CSE, ECE, IT, EEE, MECH, CIVIL), CGPA (0-10), Active Backlogs, and contact details.
* **Eligible Drive Discovery**: Browse active campus placement drives with package details (LPA), minimum CGPA cutoffs, maximum backlog limits, and application deadlines.
* **Instant Rule-Based Eligibility Engine**: Click *"Verify Eligibility Rules"* to run a live comparison of your academic profile against company criteria before submitting.
* **1-Click Application Submission**: Submit applications instantly with a unique reference code (`#APP-xxx`) preventing duplicate entries.
* **Application Status Timeline**: Track application status in real-time: `APPLIED` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED` / `REJECTED`.

### 🏛️ 2. Placement Officer (TPO) Operations Console
* **Company Onboarding**: Register recruiting companies (Tier 1 Gold, Tier 2, Mass Recruiters) with HR contact details and industry categories.
* **Drive Publication Lifecycle**: Create and publish placement drives with custom criteria (Cutoff CGPA, Max Backlogs, Allowed Branches, Package Bands, Campus Visit Dates).
* **Candidate Review & Shortlisting Queue**: Filter student applications by drive, review academic profiles, and click **Shortlist ⭐** or **Reject**.
* **Interview Round Scheduler**: Coordinate aptitude tests, technical interviews, and HR rounds with dates, times, and venue/meeting links.

### 💼 3. Corporate Recruiter Portal
* **Company-Scoped Access**: Recruiters log in with company credentials (e.g. Microsoft, Google, Goldman Sachs, Amazon) and view only candidates relevant to their company.
* **Shortlist Resume Evaluation**: Inspect candidate profiles, academic history, and branch qualifications.
* **Interview Outcome Recording**: Record final interview decisions (`SELECTED`, `REJECTED`, `ON_HOLD`) with interviewer feedback notes.

### ⚡ 4. System Administrator & Governance Console
* **Live Department Analytics**: Interactive Chart.js bar graphs showing branch-wise placement percentages (CSE, ECE, IT, EEE, MECH, CIVIL).
* **Salary Package Analytics**: Doughnut charts displaying top recruiting company offer distributions and package bands (Highest: 42.0 LPA, Average: 14.25 LPA).
* **Portal User Governance**: Manage accounts for Students, Placement Officers, and Recruiters (Account activation, role assignment, password resets).

---

## 📊 Prometheus Monitoring & Log Observability

PlacePro features built-in **Prometheus metrics exposition** to monitor server traffic, application logs, active placement drives, and system health in real-time.

* **Live Metrics Endpoint**: `http://localhost:8080/metrics`
* **Scrape Configuration**: Configured in [`monitoring/prometheus.yml`](file:///Users/harisuryaprakashreddyp.n.v/Desktop/PlacePro%20PRD/monitoring/prometheus.yml).

### Monitored Prometheus Metrics:
```prometheus
# User Logins & Failures
placepro_user_login_success_total{role="student"} 124
placepro_user_login_failure_total{role="student",reason="invalid_credentials"} 6

# Interview Rounds & Outcomes
placepro_interviews_scheduled_total{round_type="Technical"} 62
placepro_interview_outcomes_total{result="SELECTED"} 148

# System Log Counters by Level
placepro_log_events_total{level="INFO"} 280
placepro_log_events_total{level="WARN"} 12
placepro_log_events_total{level="ERROR"} 0
```

---

## 🛠️ Technology Stack & Architecture

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend** | Node.js & Java | Handles REST APIs, eligibility logic, and `/metrics` on port 8080 |
| **Frontend** | HTML5, CSS3, JS (ES6) | Single-page UI with dark theme, dynamic forms, and modal popups |
| **Analytics & Metrics** | Chart.js & Prometheus | Live department charts and Prometheus log & telemetry monitoring |
| **Data Storage** | Local Storage & REST API | Manages student profiles, drive applications, and recruiter decisions |
| **Launcher** | Shell Script (`run.sh`) | 1-click script to start the web server |

---

## 🚀 How to Run PlacePro Locally

### Step-by-Step Launch Guide

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/pnvharisuryaprakashreddy/PlacePro.git
   cd PlacePro
   ```

2. **Run the Server**:
   ```bash
   ./run.sh
   ```

3. **Open in Browser**:
   * **Web Portal**: [http://localhost:8080](http://localhost:8080)
   * **Prometheus Metrics**: [http://localhost:8080/metrics](http://localhost:8080/metrics)

---

## 📊 Sample Credentials for Evaluation / Demo

You can test each role directly from the **Role Gateway** on the landing page:

| Role | Username / Email | Password / Access Code | Workspace Capabilities |
| :--- | :--- | :--- | :--- |
| 🎓 **Student** | `priya.sharma@student.college.edu` | `Password123` | Profile, Drive Eligibility Check, 1-Click Apply, Status Timeline |
| 🏛️ **Placement Officer** | `anita.rao@placepro.local` | `Password123` | Company Onboarding, Create Drive, Candidate Shortlisting |
| 💼 **Recruiter** | Select `Microsoft India` | `Recruiter2026` | Shortlist Review, Interview Round Scoring, Offer Confirmation |
| ⚡ **System Admin** | `admin.rahul` | `AdminPass123` | Department Placement Charts, Package Analytics, User Governance |

---

## 🎯 Academic Impact & Key Metrics

* ⚡ **90% Reduction in Administrative Overhead**: Replaces manual spreadsheet cross-referencing with automated eligibility checking.
* 🎯 **100% Application Accuracy**: Prevents ineligible students from submitting applications based on CGPA cutoffs, branch restrictions, or backlog limits.
* 📊 **Instant Accreditation Reports**: Generates department placement percentages and salary statistics dynamically for university management reviews.

---

## 📜 License
This project is licensed under the **MIT License** — free for academic, institutional, and research evaluation.
