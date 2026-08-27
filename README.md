# SNAP-GRAVEplot-step-plugin

A SNAP Model Editor plug-in that registers a **GRAVEPlot** job-stream step, so a
stream can render plots from step outputs (TRACE XTV extracts, TraceForce
`forces.th` time histories, CSV tables) using GRAVEPlot's headless Python batch
interface — the same way the AptPlot Batch step runs AptPlot batch scripts
today.

**Status: v0.1.0 implemented.** The plugin compiles against a SNAP 5.0.0
install, loads in the Model Editor ("Loading plugin: GRAVEPlot version 0.1.0"),
and registers the step type, file types, and the `graveplot` application
definition. End-to-end stream execution requires GRAVEPlot installed on the
calculation-server host (see Distribution in `docs/DESIGN.md`).

| Path | Contents |
|---|---|
| `src/`, `META-INF/`, `build.xml` | The plugin source, manifest, and ant build |
| `docs/BUILD-BRIEFING.md` | Build briefing: mission, architecture, environment, conventions |
| `docs/DESIGN.md` | Class layout, manifest, wrapper behavior, as-built notes |
| `docs/reference/traceforce-step-pattern.md` | The in-house reference plugin (registration, manifest, wrapper, app generator), with source paths |
| `docs/reference/aptplot-batch-step-api.md` | Decompiled class surfaces of the AptPlot Batch step — the pattern mirrored |
| `docs/reference/graveplot-runtime.md` | GRAVEPlot install layout, launcher contract, batch API and its quirks |
| `examples/` | A verified GRAVEPlot batch script, its AptPlot analogue, and sample input |

## Building

```
ant distribution        # compiles against ${user.home}/run-snap500 by default
ant -Dsnap=/opt/snap distribution
```

Produces `graveplot-step.jar` and the install-relative overlay zip
`build/graveplot-step-plugin-v<version>.zip` (`plugins/graveplot-step.jar`).

## Installing

Copy `graveplot-step.jar` into the SNAP install's `plugins/` folder and
restart the Model Editor and calculation server. The `graveplot` application
definition is generated automatically on calc-server start with a default
Local location of `/opt/graveplot/bin/graveplot.sh`
(`C:\Program Files\graveplot\bin\graveplot.bat` on Windows); point it at the
actual GRAVEPlot install in the Configuration Tool if it lives elsewhere.

## Using the step

1. In a Job Stream, add a **GRAVEPlot** step (available under every analysis
   plugin) downstream of the steps whose outputs it should plot.
2. Connect parent-step output files (ASCII / TH) to the step's **data** input
   set. The wrapper copies them into the step directory, and the batch script
   runs with that directory as the working directory, so scripts open them by
   plain file name.
3. Edit the **Batch Script** property (Python, GRAVEPlot batch API — see
   `examples/plot_forces_graveplot.py`). Alternatively connect a `.py` file to
   the optional **script** input to use an external script.
4. List the plot files the script renders in **Plot Output Files**
   (comma-separated). Each becomes a declared step output; the step fails if
   the script does not produce them.

## Why

The SNAP job-stream plugin set ships an AptPlot Batch step but nothing for
GRAVEPlot, so GRAVEPlot plotting currently runs outside the stream by hand.
Registering a step type closes that gap and makes plot generation part of the
recorded, repeatable stream definition.
