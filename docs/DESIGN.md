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

## Distribution (sibling task, required for the step to run anywhere)

- GRAVEPlot itself must be installed on every calc-server host: RESGC RHEL
  images (`/opt/graveplot`) and Windows images (`C:\Program Files\graveplot`),
  via the packer roles + the same manifest/sha256 provenance discipline as the
  SNAP payloads (see `~/RESGC-packer`, `~/SNAP-distribution`).
- The plugin jar rides the existing SNAP overlay convention:
  `overlay/plugins/graveplot-step.jar` in the SNAP-Distribution release zip.
- Playwright browser staging is the fragile part of a headless install; bake
  it into the image build, never first-run download (no egress on RESGC).

## Open decisions

1. `JSStep` directly (TraceForce style, simple) vs `AbstractPlotStep`
   (AptPlot style, drags in `com.cafean.aptplot.components.*` and
   plot-definition machinery we do not need). **Recommendation: `JSStep`.**
2. v0 scope: internal script + N inputs + N outputs, no GUI plot builder.
   A later version could generate script boilerplate from the parent step's
   known outputs (AppGenerator-style templating).
3. Whether the step should also expose GRAVEPlot's report/HTML outputs as
   declared artifacts (GRAVEPlot can emit more than PNG).
4. Naming: user's folder name is SNAP-GRAVEplot-step-plugin; class/app naming
   above uses GravePlot/graveplot — settle capitalization before first commit
   of code (block-type and appType strings are permanent).
