# AptPlot batch: plot every segment in a TraceForce forces.th
# In an AptPlot batch job step, or:  aptbatch.sh -batch plot_forces.bat -nowin
# Adjust the READ path if not run from the directory containing forces.th,
# e.g. READ NXY "V:\test-snap-runs\DEMO\TRACE\W4Loop-wFull3\Step\forces.th"
READ NXY "forces.th"
TITLE "W4Loop Piping Segment Forces"
SUBTITLE "TraceForce (Watkins solver) - forces.th"
XAXIS LABEL "Time (s)"
YAXIS LABEL "Force (N)"
LEGEND ON
LEGEND 0.60, 0.88
LEGEND CHAR SIZE 0.6
S0 LEGEND "11 int-loop st-gen primary"
S1 LEGEND "12 int-loop pump-suct pipe"
S2 LEGEND "1 bkn-loop hot-leg pipe"
S3 LEGEND "21 int-loop accumulator"
S4 LEGEND "2 bkn-loop st-gen primary"
S5 LEGEND "3 bkn-loop pump-suct pipe"
S6 LEGEND "41 int-loop c-leg vssl c6"
S7 LEGEND "42 int-loop c-leg vssl c7"
S8 LEGEND "43 int-loop c-leg vssl c8"
S9 LEGEND "44 int-loop c-leg vssl c8"
AUTOSCALE
PRINT TO "forces_aptplot.png"
HARDCOPY DEVICE "PNG"
PRINT
