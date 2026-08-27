"""GRAVEPlot batch script: plot every segment in a TraceForce forces.th.

Run:  graveplot.sh -batch plot_forces_graveplot.py
Reads forces.th beside this script (override with FORCES_TH env var).
"""
import os
import re

print("GRAVEPlot batch: TraceForce segment forces...")

try:
    script_dir = os.path.dirname(os.path.abspath(__file__))
except NameError:
    script_dir = os.getcwd()
th_path = os.environ.get("FORCES_TH", os.path.join(script_dir, "forces.th"))
output_png = os.path.splitext(th_path)[0] + "_graveplot.png"

# Parse segment names from the '#  N: Segment ...' header comments and
# the whitespace-delimited data rows (comment/blank lines skipped).
names = []
rows = []
with open(th_path) as f:
    for line in f:
        m = re.match(r"#\s*\d+:\s*Segment '(.+)'", line)
        if m:
            names.append(m.group(1))
            continue
        if line.startswith("#") or not line.strip():
            continue
        parts = line.split()
        try:
            rows.append([float(p) for p in parts])
        except ValueError:
            continue

print(f"Parsed {len(rows)} time steps, {len(names)} segments from {th_path}")

xs = [r[0] for r in rows]
datasets = []
for i, name in enumerate(names):
    legend = name.strip("_").replace("__", " ").replace("_", " ")
    datasets.append(build_dataset_from_lists(
        name=legend,
        x_label="Time (s)",
        y_label="Force (N)",
        xs=xs,
        ys=[r[i + 1] for r in rows],
    ))

plot = build_line_plot("W4Loop Piping Segment Forces")
plot.add_datasets(*datasets)
plot.axis.x.label = "Time (s)"
plot.axis.y.label = "Force (N)"
plot.axis.x.scale = True
plot.axis.y.scale = True

palette = ["#1f77b4", "#d62728", "#2ca02c", "#9467bd", "#ff7f0e",
           "#8c564b", "#e377c2", "#17becf", "#bcbd22", "#7f7f7f"]
try:
    for i, s in enumerate(plot.series):
        s.line_color = palette[i % len(palette)]
        s.line_width = 2.0
except Exception as e:
    print(f"Warning styling series: {e}")

print(f"Rendering to {output_png}...")
plot.render(output_png, width=1200, height=800)
print("Done.")
