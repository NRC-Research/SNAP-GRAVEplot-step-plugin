# Design: GRAVEPlot batch job step

## Class layout (package `com.cafean.graveplot`)

```
src/com/cafean/graveplot/
    GravePlotPluginData.java        extends MEPluginData  (LABEL = "GRAVEPlot")
    GravePlotPlugin.java            extends MEFeaturePlugin (mostly empty hooks)
    jobstream/
        GravePlotDescList.java      extends JSStepDescriptionList
        GravePlotStep.java          extends JSStep
        GravePlotStepBeanInfo.java  property sheet for the step
        GravePlotWrapper.java       extends com.cafean.jobstream.server.WrapperModule
        GravePlotAppGenerator.java  extends com.cafean.jobstream.common.util.ApplicationGenerator
        ScriptEditorDialog.java     multi-line Python editor (TraceForce's YamlEditorDialog is the precedent)
        FileTypes.xml               declares .py script + .png output types (see manifest key)
META-INF/MANIFEST.MF
build.xml
```

## Manifest (mirrors TraceForce exactly)

```
Manifest-Version: 1.0
MEPluginData-Class: com.cafean.graveplot.GravePlotPluginData
MEPluginData-Version: 0.1.0
JobStepList: com.cafean.graveplot.jobstream.GravePlotDescList
FileTypesList: com/cafean/graveplot/jobstream/FileTypes.xml
ApplicationGenerator: com.cafean.graveplot.jobstream.GravePlotAppGenerator

Name: GravePlot
WrapperModule: com.cafean.graveplot.jobstream.GravePlotWrapper
```

The `Name:` section header is the **appType** string: `GravePlotStep.getAppType()`,
`GravePlotWrapper.getAppType()`, and this manifest entry must all agree — that is
how the calc server finds the wrapper for the step.

## Step definition

`GravePlotDescList.getStepDescriptions()` returns one
`JSStepDescription("GravePlot", "com.cafean.graveplot.jobstream.GravePlotStep",
GravePlotStep.CAT_GRAVEPLOT, "Renders plots from step outputs using GRAVEPlot's
Python batch interface.")`. `getAllowedPlugins()`: return the value meaning
"any" (TraceForce restricts to `{"TRACE"}`; check the base class for the
unrestricted convention) — plotting is not analysis-code-specific.

## Step properties (persisted block)

Keep the AptPlot Batch step's shape, minus the plot-definition machinery:

- `script` (String) + `internalScript` (boolean) — the Python batch text lives
  in the stream by default; `internalScript=false` can point at an external
  file instead. See `BatchProperties.getScript/setScript` in the AptPlot dump.
- `inputs` — file references resolved from parent-step outputs (the AptPlot
  step's `InputDefinition[]` pattern; simplest v0: a list of file names the
  wrapper pulls from connected parent steps into the step directory).
- `outputs` — declared artifact names (e.g. `forces_graveplot.png`) so
  downstream steps and the Job Status file viewer know what to expect.
- `getBlockType()` — a fresh, permanent string (e.g. `"gravejs"`); streams
  persist it in `.med`.

## Wrapper behavior (`execute()`)

1. `retrieveFiles()` / `processInputs()`: copy declared inputs from parent
   step directories into this step's working directory.
2. Write the script text to `<step dir>/graveplot_batch.py` (LF endings).
3. Resolve the `graveplot` application location from the calc server's
   application definitions (what `findApplication("graveplot")` registered).
4. `ProcessBuilder(location, "-batch", "graveplot_batch.py")`, **directory =
   step dir**, redirect output to the step log. Non-zero exit → step failure.
5. Declared outputs missing after exit → step failure with a clear message.

TraceForce's `PipingForceWrapper.execute()` (ProcessBuilder, PYTHONPATH staging,
arg assembly) is the working in-house model.

## Application generator

`GravePlotAppGenerator.generate()` mirrors `PipingForceAppGenerator`:
`createApplication("graveplot", "GravePlot")`, `setBuiltinLocations(...)`, and
platform-specific locations —
Linux/mac `${GRAVEPLOT_INSTALL_DIR}/bin/graveplot.sh` or an absolute install
path; Windows a `graveplot.bat` analogue (does not exist yet — the Windows
launcher is part of the distribution task). Keep the location overridable in
the Job Status tool so a pinned/newer GRAVEPlot can be selected per host.

## Distribution (as surveyed 2026-08-27)

- **GRAVEPlot on the images is already handled** by the `install_grave` roles
  in `~/RESGC-packer`, wired into both provision playbooks:
  - RHEL9: pulls `s3://resgc-packer-artifacts-east/Software/graveplotLatest.zip`
    and runs the ISL installer headlessly into **`/opt/grave`** (recursive
    0755 covers the Playwright exec-bit trap; fapolicyd trust included).
  - Windows: pulls `Software/graveWindowsLatest.zip`, a pre-baked archive of a
    known-good install, extracted to **`C:\GRAVE`** — including a working,
    relocatable `bin/graveplot.bat` (the ISL installer hangs on a native COM
    call under SYSTEM/WinRM, hence the pre-baked approach). The Windows
    launcher task from earlier drafts of this doc is therefore already done.
- The app generator's default Local locations match those paths
  (`/opt/grave/bin/graveplot.sh`, `C:\GRAVE\bin\graveplot.bat`).
- **The plugin jar rides the SNAP overlay convention**:
  `overlay/plugins/graveplot-step.jar` in the next SNAP-Distribution release
  zip. The overlay applier in every install door copies `overlay/` files
  install-relative, so no packer change is needed for the jar itself. The
  release procedure lives in `~/RESGC-Admin-Notes/SNAP/release-process.md`
  (build on the previous release zip, never a staging area).
- Per-user application definitions on existing analyst homes come from the
  app generator at calc-server start — the site-config merge script never
  adds applications, so the generator defaults are the load-bearing path.
- Playwright browser staging is the fragile part of a headless install; it is
  baked into the artifacts (no first-run download — no egress on RESGC). A
  build-time headless render smoke test in the install_grave roles is the
  remaining hardening opportunity.

### Shipped 2026-08-28: GRAVEPlot 1.0.1-nrc1 interim patch

The ISL 1.0.0/1.0.1 builds fail every batch run over 30 seconds (see
`docs/isl-graveplot-batch-timeout-bug.md`), which fails every GRAVEPlot job
step on RESGC-class hosts. Pending ISL's fix, NRC ships an interim patch:

- **Patch source**: grave-plot repo, branch `nrc-1.0.1-nrc1` (diff against
  the `recovered-1.0.0-decompiled` snapshot branch is exactly the patch);
  timeout default 24 h, `GRAVEPLOT_BATCH_TIMEOUT_SECONDS` override; jar
  manifest marked `Implementation-Version: 1.0.1-nrc1`.
- **RHEL images**: the RESGC-packer `install_grave` role (branch
  `feat/graveplot-step-appdef`) applies the patch after the ISL installer —
  timeout classes rebuilt from recovered source plus ISL's own 1.0.1 acbind
  bytecode over the 1.0.0 appfile — then normalizes the ms-playwright
  browser nesting and runs a headless render smoke test that fails the
  image build on regressions. Validated on RAVLYMPCGG0001.
- **Windows images**: `graveWindowsLatest.zip` in the packer bucket carries
  the same jar patch applied at artifact level (the role is a plain unzip;
  the pre-patch artifact is preserved as `graveWindowsLatest-pre-nrc1.zip`).
- **Removal criteria**: when ISL ships a build whose batch runner waits for
  script completion, drop the role patch step and restore the Windows
  artifact from a fresh ISL install; confirm with the 35-second sleep
  bracket first.

## Open decisions (resolved in v0.1.0 — see "As built" below)

1. `JSStep` directly (TraceForce style, simple) vs `AbstractPlotStep`
   (AptPlot style, drags in `com.cafean.aptplot.components.*` and
   plot-definition machinery we do not need). **Resolved: `JSStep`.**
2. v0 scope: internal script + N inputs + N outputs, no GUI plot builder.
   A later version could generate script boilerplate from the parent step's
   known outputs (AppGenerator-style templating). **Resolved: v0 as scoped.**
3. Whether the step should also expose GRAVEPlot's report/HTML outputs as
   declared artifacts. **Deferred: the Plot Output Files list maps extensions
   to Image:PNG/JPG/SVG/PDF types; other artifact types can be added later.**
4. Naming. **Resolved (permanent): appType `GravePlot`, application name
   `graveplot`, block type `GravePlotStep`.**

## As built (v0.1.0 implementation notes)

Mechanisms confirmed by decompiling SNAP 5.0.0 (`job_stream.jar`,
`js_python.jar`, `js_aptplot.jar`) — the Python Script step
(`com.cafean.jobstream.python.PythonScriptStep`) turned out to be the closest
model and is what the implementation mirrors:

- **Script persistence**: `GravePlotProperties extends JSStepData` (script
  text + plot output names). `JSStep.writeBlock`/`readBlock` automatically
  embed `getAuxiliaryData()` in the step's `.med` block via
  `writeBlock(DataOutputStream)`/`readBlock(DataInputStream)` — no DDT/PibFile
  machinery required. `Xdr.writeBytes/readBytes` carry the script text
  (`Xdr.readString` strips trailing newlines).
- **Script transport to the server**: at submit, `ExportUtility` writes
  `getAuxiliaryData().write(OutputStream)` to the stream stage as
  `.res/Steps/Step_<n>.dat` for any step with auxiliary data. The wrapper
  fetches it with the inherited `retrieveAuxiliaryData(File)`, parses the
  small XML (`<graveplot><script>…`), and writes `<task>.py` (LF, UTF-8) into
  the step directory.
- **Execution**: the inherited `WrapperModule.executeApplication(String[],
  File)` already runs with cwd = the step working directory (the `__file__`
  gotcha's requirement) and merged output to the screen file. Command:
  `<launcher> [app args] -batch <task>.py`. Non-zero exit or a missing
  declared plot output fails the step via the `sendFinalNotifications`
  override.
- **Internal vs external script**: an optional `script` input slot
  (`Python:Script`, a built-in file type). `updateStep` publishes
  `SCRIPT_INCLUDED` as a module option (the Python step's pattern); the
  wrapper uses the connected file when present, the shipped aux script
  otherwise.
- **Inputs**: one optional input *set* labeled `data` accepting
  `Generic:Ascii` and `Generic:TH` (TH is declared in this plugin's
  FileTypes.xml so the step works without the TraceForce plugin present).
- **Outputs**: `screen` plus one declared output per entry in the
  comma-separated Plot Output Files property; extensions map to the built-in
  `Image:PNG/JPG/SVG/PDF` file types. `buildFileNames()` regenerates the
  output descriptors, preserving per-label settings (the Python step's
  rebuild pattern).
- **Unrestricted plugins**: `getAllowedPlugins()` returns an empty array —
  `StreamModuleLoader.StreamStepTable.isAllowedPlugin` treats null/empty as
  "allowed everywhere".
- **App generator**: registers `graveplot` with a default Local location of
  `/opt/grave/bin/graveplot.sh` (Windows: `C:\GRAVE\bin\graveplot.bat`) —
  the RESGC image install paths; it never overwrites an existing
  user-configured location.
