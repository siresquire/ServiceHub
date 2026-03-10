import logging
import random
import csv
from datetime import datetime, timedelta
from pathlib import Path

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)


# --- Base Data ---
categories = ['IT_SUPPORT', 'FACILITIES', 'HR_REQUEST']
priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
statuses = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']

# Realistic ticket templates per category
TICKET_TEMPLATES = {
    'IT_SUPPORT': [
        ("Laptop won't turn on", "Device is completely unresponsive after attempted power on. Battery charged."),
        ("VPN connection dropping", "VPN disconnects every 15-20 minutes, disrupting remote work sessions."),
        ("Password reset required", "Account locked after multiple failed login attempts. Need immediate access."),
        ("Printer offline on floor 3", "HP LaserJet Pro MFP M428fdw showing offline, unable to print documents."),
        ("Outlook not syncing emails", "New emails not appearing in inbox. Last sync timestamp shows 6 hours ago."),
        ("Software installation request", "Need Adobe Acrobat Pro installed for contract review workflows."),
        ("Dual monitor setup not working", "Second monitor not detected after office move. Display settings updated."),
        ("Slow computer performance", "System taking 5+ minutes to boot. Multiple applications crashing frequently."),
        ("Access to shared drive denied", "Permission error when accessing Q:/Finance/Reports after team transfer."),
        ("Video call audio issues", "Microphone not detected in Microsoft Teams. Works in other applications."),
    ],
    'FACILITIES': [
        ("HVAC not cooling meeting room B", "Temperature in Room B consistently 5 degrees above thermostat setting."),
        ("Broken chair in open workspace", "Office chair armrest snapped. Creating hazard in aisle 4B."),
        ("Water leak under sink in kitchen", "Slow drip detected under the 2nd floor kitchen sink. Small puddle forming."),
        ("Parking lot light out", "Light pole #7 in east parking lot not functioning. Safety concern after hours."),
        ("Elevator making grinding noise", "Elevator 2 produces loud grinding sound between floors 3 and 4."),
        ("Conference room projector mount loose", "Ceiling mount for projector in Conference Room A is visibly loose."),
        ("Restroom paper towel dispenser broken", "Dispenser in men's restroom 1st floor jammed, needs replacement."),
        ("Badge reader not working at back door", "Card reader at rear entrance B unresponsive since this morning."),
        ("Mold spotted in storage room", "Small mold patch visible on north wall of basement storage room 3."),
        ("Standing desk won't adjust", "Electric standing desk motor not responding to height adjustment panel."),
    ],
    'HR_REQUEST': [
        ("Onboarding checklist for new hire", "New analyst joining Monday. Need standard onboarding package prepared."),
        ("Request for remote work approval", "Requesting permanent 3-day remote schedule per new hybrid policy."),
        ("Update emergency contact information", "Need to update next-of-kin details in HR portal after recent move."),
        ("Clarification on PTO carry-over policy", "Unclear on maximum PTO rollover days allowed into next fiscal year."),
        ("Payroll discrepancy for last pay period", "Overtime hours from 11/15 not reflected in most recent paycheck."),
        ("Request for employment verification letter", "Need official letter for mortgage application by end of week."),
        ("Benefits enrollment question", "Missed open enrollment window - inquiring about qualifying life event."),
        ("Performance review scheduling", "Q4 review not yet scheduled. Manager unresponsive to calendar requests."),
        ("Training certification reimbursement", "AWS Solutions Architect exam fee pending reimbursement since October."),
        ("Name change documentation", "Legal name change complete - need to update all HR and payroll records."),
    ],
}

FIRST_NAMES = [
    "James", "Maria", "David", "Sarah", "Michael", "Emily", "Robert", "Jessica",
    "William", "Ashley", "Daniel", "Amanda", "Matthew", "Melissa", "Christopher",
    "Stephanie", "Andrew", "Rebecca", "Joshua", "Laura", "Kevin", "Jennifer",
    "Brian", "Linda", "Steven", "Patricia", "Edward", "Barbara", "Timothy", "Karen",
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
]

DEPARTMENTS = [
    "Engineering", "Marketing", "Finance", "Operations", "Sales",
    "Legal", "Product", "Design", "Customer Success", "People & Culture",
]

AGENTS = [
    "alex.turner", "jordan.kim", "priya.sharma", "chris.nguyen",
    "taylor.brooks", "morgan.patel", "sam.okafor", "casey.reyes",
]

RESOLUTION_NOTES = {
    'IT_SUPPORT': [
        "Issue resolved remotely via TeamViewer. Driver update applied.",
        "Ticket closed after confirming fix with user. Root cause: outdated firmware.",
        "Escalated to Tier 2 - resolved after network configuration change.",
        "Hardware replaced under warranty. User confirmed full functionality.",
        "Resolved by clearing application cache and resetting user profile.",
    ],
    'FACILITIES': [
        "Work order submitted to external contractor. Repair completed same day.",
        "Replaced faulty component. Area inspected and cleared.",
        "Temporary fix applied. Permanent repair scheduled for next maintenance window.",
        "Vendor dispatched and issue resolved within 4 hours.",
        "Item replaced from facilities inventory. No further action needed.",
    ],
    'HR_REQUEST': [
        "Documentation processed and updated in HRIS system.",
        "Request approved by department head. HR file updated accordingly.",
        "Employee notified via email with attached confirmation documents.",
        "Escalated to payroll team - corrective payment issued next cycle.",
        "Meeting scheduled with HR Business Partner to discuss further.",
    ],
}

def random_date(start_days_ago=90, end_days_ago=0):
    """Generate a random datetime within the past N days."""
    start = datetime.now() - timedelta(days=start_days_ago)
    end = datetime.now() - timedelta(days=end_days_ago)
    delta = end - start
    return start + timedelta(seconds=random.randint(0, int(delta.total_seconds())))


def generate_ticket(ticket_id):
    """Generate a single realistic support ticket with all fields populated."""
    category = random.choice(categories)
    priority = random.choices(priorities, weights=[30, 40, 20, 10])[0]
    # All tickets are RESOLVED or CLOSED so every lifecycle field is populated
    status = random.choices(['RESOLVED', 'CLOSED'], weights=[50, 50])[0]

    title, description = random.choice(TICKET_TEMPLATES[category])

    first = random.choice(FIRST_NAMES)
    last = random.choice(LAST_NAMES)
    submitter_name = f"{first} {last}"
    submitter_email = f"{first.lower()}.{last.lower()}@company.com"
    department = random.choice(DEPARTMENTS)

    created_at = random_date(start_days_ago=90)

    # All tickets are assigned → always populated
    assigned_at = created_at + timedelta(minutes=random.randint(10, 120))
    assigned_to = random.choice(AGENTS)

    # All tickets are resolved → always populated
    resolved_at = assigned_at + timedelta(hours=random.randint(1, 72))
    resolution_note = random.choice(RESOLUTION_NOTES[category])
    updated_at = resolved_at

    # All tickets are closed → always populated
    closed_at = resolved_at + timedelta(hours=random.randint(1, 24))
    updated_at = closed_at

    # SLA breach logic based on priority
    sla_threshold = {'LOW': 72, 'MEDIUM': 48, 'HIGH': 24, 'CRITICAL': 4}
    sla_deadline = created_at + timedelta(hours=sla_threshold[priority])
    sla_breached = resolved_at > sla_deadline

    return {
        "ticket_id": f"TKT-{ticket_id:05d}",
        "title": title,
        "description": description,
        "category": category,
        "priority": priority,
        "status": status,
        "submitter_name": submitter_name,
        "submitter_email": submitter_email,
        "department": department,
        "assigned_to": assigned_to,
        "created_at": created_at.isoformat(),
        "updated_at": updated_at.isoformat(),
        "assigned_at": assigned_at.isoformat(),
        "resolved_at": resolved_at.isoformat(),
        "closed_at": closed_at.isoformat(),
        "resolution_note": resolution_note,
        "sla_breached": sla_breached,
        "satisfaction_score": random.randint(1, 5),
    }


def generate_dataset(n=800):
    """Generate n tickets and return as a list of dicts."""
    log.info("Generating %d tickets...", n)
    start_id = random.randint(1000, 9000)
    tickets = [generate_ticket(start_id + i) for i in range(n)]
    log.info("Generation complete: %d tickets created.", len(tickets))
    return tickets

def save_csv(tickets, path=None):
    if not tickets:
        log.warning("No tickets to save - skipping CSV write.")
        return
    output = Path(path) if path else Path(__file__).parent / "data" / "sample_data.csv"
    output.parent.mkdir(parents=True, exist_ok=True)
    log.info("Writing CSV to %s", output)
    with output.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=tickets[0].keys())
        writer.writeheader()
        writer.writerows(tickets)
    log.info("Saved %d tickets to %s", len(tickets), output)


def print_summary(tickets):
    from collections import Counter
    breached = sum(1 for t in tickets if t['sla_breached'])
    resolved = [t for t in tickets if t['satisfaction_score']]
    avg_sat = sum(t['satisfaction_score'] for t in resolved) / len(resolved) if resolved else None

    log.info("-- Dataset Summary ------------------------------------------")
    log.info("  Total tickets     : %d", len(tickets))
    log.info("  Categories        : %s", dict(Counter(t['category'] for t in tickets)))
    log.info("  Priorities        : %s", dict(Counter(t['priority'] for t in tickets)))
    log.info("  Statuses          : %s", dict(Counter(t['status'] for t in tickets)))
    log.info("  SLA breached      : %d", breached)
    if avg_sat is not None:
        log.info("  Avg satisfaction  : %.2f/5.0 (n=%d)", avg_sat, len(resolved))
    log.info("----------------------------------------------------")


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Generate realistic support ticket test data.")
    parser.add_argument("-n", "--count", type=int, default=800, help="Number of tickets to generate (default: 800)")
    parser.add_argument("--csv", type=str, default=None, help="Output CSV file path (default: data-engineering/data/tickets.csv)")
    parser.add_argument("--seed", type=int, default=None, help="Random seed for reproducibility")
    parser.add_argument("--no-csv", action="store_true", help="Skip CSV output")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)
        log.info("Using random seed: %d", args.seed)

    tickets = generate_dataset(args.count)

    if not args.no_csv:
        save_csv(tickets, args.csv)

    print_summary(tickets)