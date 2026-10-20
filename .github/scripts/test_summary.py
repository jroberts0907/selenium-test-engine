"""Writes a Markdown summary of the Surefire test results to the GitHub Actions job summary.

Usage: python3 test_summary.py <title> [--known-defects]

With --known-defects, every test is expected to fail (each one checks for a bug that's still there).
A test that passes means the bug may be fixed, so it's flagged with a warning.
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET

title = sys.argv[1]
known_defects = "--known-defects" in sys.argv[2:]

suites, cases = [], []
for path in sorted(glob.glob("target/surefire-reports/TEST-*.xml")):
    suite = ET.parse(path).getroot()
    counts = {k: int(suite.get(k, 0)) for k in ("tests", "failures", "errors", "skipped")}
    suites.append((suite.get("name").rsplit(".", 1)[-1], counts, float(suite.get("time", 0))))
    for case in suite.iter("testcase"):
        failed = case.find("failure") is not None or case.find("error") is not None
        cases.append((case.get("classname").rsplit(".", 1)[-1].replace("$", " › "), case.get("name"), failed))

lines = [f"## {title}", ""]
if not suites:
    lines.append("No test results: the build failed before the tests ran. See the job log.")
elif known_defects:
    still_failing = sum(failed for _, _, failed in cases)
    lines.append(f"**{still_failing} of {len(cases)} known defects still present.**")
    lines += ["", "| Account | Expected behavior | Status |", "|---|---|---|"]
    for group, name, failed in cases:
        status = "❌ bug still present" if failed else "⚠️ passed: bug may be fixed"
        lines.append(f"| {group.split(' › ')[-1]} | {name} | {status} |")
        if not failed:
            print(f"::warning title=Known defect passed::{group}: '{name}' passed. "
                  "If the bug is fixed, move the test into the main suite.")
else:
    total = {k: sum(c[k] for _, c, _ in suites) for k in ("tests", "failures", "errors", "skipped")}
    broken = total["failures"] + total["errors"]
    passed = total["tests"] - broken - total["skipped"]
    lines.append(f"**{'✅' if broken == 0 else '❌'} {passed} passed, {broken} failed, "
                 f"{total['skipped']} skipped** in {sum(t for _, _, t in suites):.1f} s")
    lines += ["", "| Test class | Tests | Failed | Time |", "|---|---:|---:|---:|"]
    for name, c, seconds in suites:
        lines.append(f"| {name} | {c['tests']} | {c['failures'] + c['errors']} | {seconds:.1f} s |")
    failing = [(group, name) for group, name, failed in cases if failed]
    if failing:
        lines += ["", "**Failing tests** (screenshots are in the job's artifacts):", ""]
        lines += [f"- {group}: {name}" for group, name in failing]

summary = "\n".join(lines) + "\n"
target = os.environ.get("GITHUB_STEP_SUMMARY")
if target:
    with open(target, "a", encoding="utf-8") as f:
        f.write(summary)
else:
    sys.stdout.reconfigure(encoding="utf-8")  # emoji in the summary; Windows consoles default to cp1252
    print(summary)
