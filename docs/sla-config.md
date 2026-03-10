
# SLA Configuration – ServiceHub

## Overview

ServiceHub uses a **Service Level Agreement (SLA) matrix** to define the expected
response and resolution times for service requests.

SLA policies determine how quickly requests must be **acknowledged** and **resolved**
based on their **category** and **priority**.

The SLA configuration is stored in the **`sla_policies`** database table and used by the SLA engine to:

- Track response times
- Track resolution times
- Detect SLA breaches
- Trigger automatic escalation

---

# Request Categories

ServiceHub supports the following request categories:

| Category | Description |
|--------|-------------|
| IT_SUPPORT | Technical issues such as software, hardware, or access problems |
| HR_REQUEST | Human resource related requests such as leave or documentation |
| FACILITIES | Office maintenance, workspace, or equipment issues |

---

# Priority Levels

Requests are assigned one of the following priority levels:

| Priority | Description |
|--------|-------------|
| CRITICAL | Urgent issues that require immediate attention and resolution |
| HIGH | Critical issues that significantly impact operations |
| MEDIUM | Issues affecting productivity but not critical |
| LOW | Minor issues with limited impact |

---

# SLA Metrics

Two metrics are tracked for each request in **hours**, as stored in the database:

- **`response_hours`** – maximum time to respond to the request  
- **`resolution_hours`** – maximum time to fully resolve the request  

## Response Time

Response time measures how quickly a request receives its **first response**:

```

Request Creation → First Agent Response

```

Typically when the request transitions from:

```

OPEN → ASSIGNED

```

---

## Resolution Time

Resolution time measures how long it takes for the request to be fully resolved:

```

Request Creation → Request Resolution

````

This occurs when the request reaches the **RESOLVED** status.

---

# SLA Matrix

The following SLA rules define response and resolution targets for each
category and priority combination. Times are stored in **hours** in the database.

| Category | Priority | Response Time | Resolution Time |
|--------|--------|--------------|----------------|
| IT_SUPPORT | CRITICAL | 0.5 | 2 |
| IT_SUPPORT | HIGH | 1 | 4 |
| IT_SUPPORT | MEDIUM | 4 | 24 |
| IT_SUPPORT | LOW | 8 | 48 |
| HR_REQUEST | CRITICAL | 1 | 4 |
| HR_REQUEST | HIGH | 2 | 8 |
| HR_REQUEST | MEDIUM | 8 | 48 |
| FACILITIES | CRITICAL | 0.5 | 4 |
| FACILITIES | HIGH | 1 | 8 |
| FACILITIES | MEDIUM | 4 | 24 |

---

# Database Configuration (`sla_policies` table)

| Column | Type | Description |
|------|------|-------------|
| id | SERIAL | Unique identifier |
| category | `ticket_category` | Request category |
| priority | `ticket_priority` | Request priority |
| response_hours | NUMERIC(6,2) | Maximum response time in hours |
| resolution_hours | NUMERIC(6,2) | Maximum resolution time in hours |
| resolution_time_hours | NUMERIC(6,2) | Generated column (same as resolution_hours) |
| created_at | TIMESTAMPTZ | Record creation timestamp |
| updated_at | TIMESTAMPTZ | Record last update timestamp |

**Constraints:**

- `(category, priority)` combination must be unique (`uq_sla_category_priority`)  
- `response_hours` and `resolution_hours` must be > 0

---

# Example SLA Record

| category | priority | response_hours | resolution_hours |
|---------|---------|----------------|----------------|
| IT_SUPPORT | CRITICAL | 0.5 | 2 |

This means:

- Agent must respond within **30 minutes** (0.5 hours)  
- Request must be resolved within **2 hours**

---

# SLA Deadline Calculation

When a request is created, the system retrieves the matching SLA record and calculates deadlines:

```
response_deadline = created_at + response_hours
resolution_deadline = created_at + resolution_hours
````

These deadlines are used to monitor SLA compliance.

---

# SLA Breach Detection

An SLA breach occurs when the current time exceeds the configured deadline.

## Response SLA Breach

```
current_time > response_deadline
AND request has not been assigned
```

## Resolution SLA Breach

```
current_time > resolution_deadline
AND request status != RESOLVED
AND request status != CLOSED
```

Breached requests are flagged for monitoring and escalation.

---

# Automatic Escalation

ServiceHub supports automatic escalation when an SLA breach occurs. Escalation actions may include:

* Increasing ticket priority
* Notifying supervisors or administrators
* Highlighting the request in dashboards
* Reassigning the request to a senior agent

**Example escalation flow:**

1. A user submits an **IT_SUPPORT CRITICAL** request.
2. The SLA defines:

    * Response: **0.5 hours (30 minutes)**
    * Resolution: **2 hours**
3. If no response occurs within **30 minutes**, a **Response SLA Breach** is triggered.
4. If unresolved after **2 hours**, a **Resolution SLA Breach** occurs.
5. The system escalates the request to Admin or supervisor.

---

# Summary

The SLA configuration enables ServiceHub to:

* Define service response expectations
* Monitor operational performance
* Detect SLA violations
* Automatically escalate unresolved requests
* Provide SLA metrics for dashboards and analytics
