# GRAVEPlot Step Plugin v0.1.0

Initial implementation of the GRAVEPlot job-stream step for SNAP 5.0.0.

## Contents

- **GravePlot step type** registered for all analysis plugins: category
  "GRAVEPlot", block type `GravePlotStep`, app type `GravePlot`.
- **Batch Script property** with a Python syntax-highlighting editor dialog;
  the script persists in the stream (`.med`) and ships to the calculation
  server as auxiliary step data. An optional `script` input allows an
  external `.py` file instead.
- **Data input set** accepting Generic ASCII / TH files from parent steps;
  files are staged into the step directory and the script runs with that
  directory as its working directory.
- **Plot Output Files property**: comma-separated declared artifacts
  (PNG/JPG/SVG/PDF by extension). The step fails when a declared output is
  not produced.
- **GravePlotWrapper** server module: retrieves the script, executes
  `graveplot -batch <script>.py`, streams console output to the screen file,
  fails on non-zero exit.
- **graveplot application definition** generated on calc-server start
  (default Local location `/opt/graveplot/bin/graveplot.sh`; Windows
  `C:\Program Files\graveplot\bin\graveplot.bat`); existing user-configured
  locations are preserved.

## Verified

- Compiles against SNAP 5.0.0 (`ant distribution`, release 11 bytecode).
- Plugin loads in the Model Editor (MEBatch): "Loading plugin: GRAVEPlot
  version 0.1.0"; application definition generated.
- Step description registration, aux-data persistence round-trip, submit-time
  XML export/parse (including XML-hostile script text), BeanInfo properties,
  step initialization, and output rebuilding covered by a standalone smoke
  test against the SNAP jars.

## Not yet covered

- End-to-end stream execution on a calculation server (requires GRAVEPlot
  installed on the host; see Distribution in `docs/DESIGN.md`).
- Windows `graveplot.bat` launcher (part of the GRAVEPlot distribution task).
