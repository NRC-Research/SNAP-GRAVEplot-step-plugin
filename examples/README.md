# Examples (all verified 2026-08-27 against a live W4Loop TraceForce run)

- `plot_forces_graveplot.py` — GRAVEPlot batch script; rendered 10 piping-force
  segments from a real `forces.th` via `~/GRAVEPLOTnew/bin/graveplot.sh -batch`.
  This is exactly the kind of script the GRAVEPlot job step will carry and run.
- `plot_forces_aptplot.bat` — the AptPlot Batch step analogue for the same data
  (Grace command language); verified with `aptbatch.sh -batch ... -nowin` on
  `ravlympcgg0001`. Useful for behavior parity checks.
- `forces.th.sample` — header + first data rows of the TraceForce TH format the
  scripts consume. All header lines are `#`-prefixed (TraceForce >= 7a91cb4).
