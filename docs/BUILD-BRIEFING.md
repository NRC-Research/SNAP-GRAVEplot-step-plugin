# Build briefing: GRAVEPlot job-stream step plugin for SNAP

## Mission

Build a SNAP Model Editor plug-in jar that adds a **GRAVEPlot batch step** to
the job-stream system. In a stream, the step sits downstream of an analysis
step (TRACE, TraceForce Piping Force, PARCS...), carries a Python batch script,
and at execution time the calculation server runs GRAVEPlot headlessly against
the parent step's output files, producing PNG/plot artifacts in the step
directory.

Model it on two working references, both fully documented in `docs/reference/`:

1. **`~/SNAP-TraceForce-plugin`** (github.com/NRC-Research/SNAP-TraceForce-plugin)
   — the in-house plugin that already registers a custom step type
   ("PipingForce"). Copy its *packaging*: manifest keys, DescList, wrapper,
   application generator, ant build, install-relative zip layout.
2. **AptPlot Batch step** (`js_aptplot.jar`, decompiled surfaces in
   `docs/reference/aptplot-batch-step-api.md`) — the closest functional
   analogue. Copy its *shape*: a script stored in the step
   (`getScript`/`setScript`/`getInternalScript`), `InputDefinition[]` /
   `OutputDefinition[]`, a script-editor dialog.

## The one-paragraph architecture

`GravePlotDescList extends JSStepDescriptionList` registers step name →
`GravePlotStep extends JSStep` (Category, block type, app type
`"graveplot"`). `GravePlotStepBeanInfo` exposes the editable properties
(script text, inputs, outputs). `GravePlotWrapper extends
com.cafean.jobstream.server.WrapperModule` runs on the calc server: it stages
the script into the step directory, resolves the `graveplot` application
location, and executes `graveplot.sh -batch <script>.py` (or `graveplot.bat`
on Windows) with **cwd = the step directory** so relative input paths work.
`GravePlotAppGenerator extends ApplicationGenerator` auto-registers the
`graveplot` application definition with per-platform locations. All five
classes plus a manifest — that is the entire plugin. See `docs/DESIGN.md`.

## Environment facts you will need

- GRAVEPlot source: `~/grave-plot` = github.com/NRC-Research/grave-plot
  (Maven, Spring Boot PropertiesLauncher, JavaFX 22, GraalPy, Playwright).
- Working GRAVEPlot install (macOS): `~/GRAVEPLOTnew` (`bin/graveplot.sh`).
  Release zip artifact: `~/graveplotLatest.zip`.
- GRAVEPlot ships to RESGC images via the `install_grave` packer roles
  (`~/RESGC-packer`): RHEL runs the ISL installer headlessly into
  **`/opt/grave`**; Windows extracts a pre-baked archive to **`C:\GRAVE`**
  (with a working `bin/graveplot.bat`). The app generator defaults match
  those paths. See "Distribution" in `docs/DESIGN.md`.
- SNAP plugin dev references on this machine: `~/SNAP-TraceForce-plugin`
  (build.xml compiles against a SNAP install's jars), `~/SNAP-TRACE-Plugin`
  (the big TRACE plugin repo), deployed SNAP at `/opt/snap` on
  `ravlympcgg0001` (ssh as cgg, passwordless sudo) for jar inspection and
  headless testing.
- SNAP versions in play: SNAP 5.0.0, job-stream/AptPlot step classes compiled
  in `plugins/js_aptplot.jar` and `lib/job_stream.jar` of any SNAP install.
- A verified end-to-end example lives in `examples/`: 
  `plot_forces_graveplot.py` rendered a real 10-segment TraceForce
  `forces.th` via `~/GRAVEPLOTnew/bin/graveplot.sh -batch` on 2026-08-27.

## Gotchas already paid for

- **`__file__` is undefined** in GRAVEPlot batch scripts (GraalPy exec's the
  script after injecting the `graveplot_api.py` prelude). Scripts must fall
  back to `os.getcwd()` — another reason the wrapper must set cwd to the step
  directory.
- GRAVEPlot renders PNGs through **Playwright headless Chromium**; browsers
  must sit directly in `lib/playwright/` (not an `ms-playwright/` subfolder),
  need `chmod -R +x`, and on macOS need quarantine xattrs cleared. First
  render on a fresh host is the failure hotspot — see
  `docs/reference/graveplot-runtime.md`.
- TraceForce `forces.th` files written before TraceForce commit `7a91cb4`
  have a column-header row *not* prefixed with `#`; Grace-family readers
  abort on it. Current files are fully `#`-commented and parse clean.
- The step must appear for all analysis plugins, not just TRACE:
  `getAllowedPlugins()` in the DescList — TraceForce restricts to
  `{"TRACE"}`; a plot step should likely return null/empty (verify what the
  base class treats as "no restriction").
- SNAP job-step blocks persist in `.med` streams (PibFile constructors,
  `getBlockType()`); pick the block-type string once and never change it, or
  saved streams break.

## Conventions in force

- Follow the user's global git rules : pull before
  work; clean technical commit messages.
- `python/trace_force/` in the TraceForce repo shows the install-relative zip
  convention used by the SNAP distribution's `overlay/` mechanism — package
  this plugin the same way: `plugins/graveplot-step.jar` (+ any helper
  scripts) staged install-relative.
- Version like TraceForce: `MEPluginData-Version` in the manifest, release
  notes per version, tags `vX.Y.Z`.
