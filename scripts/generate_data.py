"""Generate realistic fallback CSV data for MFHE (Jan 2018 – Sep 2024)."""

import csv, math, random, os

random.seed(42)

OUT_DIR = os.path.join(os.path.dirname(__file__),
                       '..', 'backend', 'src', 'main', 'resources', 'data')

months = []
for y in range(2018, 2025):
    end_m = 9 if y == 2024 else 12
    for m in range(1, end_m + 1):
        months.append(f"{y}-{m:02d}")

INDUSTRIES = [
    ("Goods-producing sector",                         365, 82),
    ("Construction",                                   198, 42),
    ("Manufacturing",                                  153, 38),
    ("Wholesale and retail trade",                     479, 65),
    ("Transportation and warehousing",                 185, 29),
    ("Finance, insurance, real estate",                413, 35),
    ("Professional, scientific and technical services", 390, 32),
    ("Educational services",                           245, 29),
    ("Health care and social assistance",              312, 53),
    ("Accommodation and food services",                229, 32),
]

DWELLING_TYPES = ["Single", "Semi-detached", "Row", "Apartment"]

# Toronto base starts/completions per month
TOR_STARTS  = {"Single": 410, "Semi-detached": 185, "Row": 620, "Apartment": 3200}
TOR_COMPS   = {"Single": 380, "Semi-detached": 160, "Row": 550, "Apartment": 2900}
# Hamilton base
HAM_STARTS  = {"Single": 125, "Semi-detached": 68, "Row": 140, "Apartment": 380}
HAM_COMPS   = {"Single": 115, "Semi-detached": 55, "Row": 125, "Apartment": 310}


def trend(i: int, n: int) -> float:
    """Gradual growth over the full period, with a COVID dip around month 27-33 (Mar-Sep 2020)."""
    base = 1.0 + 0.08 * (i / n)
    if 26 <= i <= 32:
        dip = 1.0 - 0.12 * math.exp(-0.5 * ((i - 29) / 2) ** 2)
        base *= dip
    return base


def seasonal(month_idx: int) -> float:
    """Construction seasonality: peaks in summer, dips in winter."""
    m = month_idx % 12
    return 1.0 + 0.10 * math.sin(math.pi * (m - 1) / 5.5)


def jitter(scale: float = 0.04) -> float:
    return 1.0 + random.gauss(0, scale)


# ── Employment CSV ──────────────────────────────────────
emp_path = os.path.join(OUT_DIR, 'employment.csv')
with open(emp_path, 'w', newline='') as f:
    w = csv.writer(f)
    w.writerow(["REF_DATE", "GEO", "NAICS", "VALUE"])
    for i, ref in enumerate(months):
        t = trend(i, len(months))
        for name, tor_base, ham_base in INDUSTRIES:
            tor_val = round(tor_base * t * jitter(0.03), 1)
            ham_val = round(ham_base * t * jitter(0.04), 1)
            w.writerow([ref, "Toronto CMA", name, tor_val])
            w.writerow([ref, "Hamilton CMA", name, ham_val])

# ── Housing CSV ─────────────────────────────────────────
hou_path = os.path.join(OUT_DIR, 'housing.csv')
with open(hou_path, 'w', newline='') as f:
    w = csv.writer(f)
    w.writerow(["REF_DATE", "GEO", "DWELLING_TYPE", "METRIC", "VALUE"])
    for i, ref in enumerate(months):
        t = trend(i, len(months))
        s = seasonal(i)
        for dt in DWELLING_TYPES:
            ts = round(TOR_STARTS[dt] * t * s * jitter(), 0)
            tc = round(TOR_COMPS[dt]  * t * s * jitter(), 0)
            hs = round(HAM_STARTS[dt] * t * s * jitter(), 0)
            hc = round(HAM_COMPS[dt]  * t * s * jitter(), 0)
            w.writerow([ref, "Toronto CMA", dt, "Starts",      int(ts)])
            w.writerow([ref, "Toronto CMA", dt, "Completions", int(tc)])
            w.writerow([ref, "Hamilton CMA", dt, "Starts",      int(hs)])
            w.writerow([ref, "Hamilton CMA", dt, "Completions", int(hc)])

tor_emp = sum(b for _, b, _ in INDUSTRIES)
ham_emp = sum(b for _, _, b in INDUSTRIES)
print(f"Generated {len(months)} months of data (Jan 2018 – Sep 2024)")
print(f"  employment.csv  → {len(months) * len(INDUSTRIES) * 2} rows")
print(f"  housing.csv     → {len(months) * len(DWELLING_TYPES) * 4} rows")
