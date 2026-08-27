# SNAP-GRAVEplot-step-plugin

A SNAP Model Editor plug-in that registers a **GRAVEPlot** job-stream step, so a
stream can render plots from step outputs (TRACE XTV extracts, TraceForce
`forces.th` time histories, CSV tables) using GRAVEPlot's headless Python batch
interface — the same way the AptPlot Batch step runs AptPlot batch scripts
today.

**Status: design/reference stage.** No plugin code exists yet. This repository
currently holds the complete reference material needed to build it:

| Path | Contents |
|---|---|
| `docs/BUILD-BRIEFING.md` | Build briefing: mission, architecture, environment, conventions |
| `docs/DESIGN.md` | Proposed class layout, manifest, wrapper behavior, open decisions |
| `docs/reference/traceforce-step-pattern.md` | The in-house reference plugin (registration, manifest, wrapper, app generator), with source paths |
| `docs/reference/aptplot-batch-step-api.md` | Decompiled class surfaces of the AptPlot Batch step — the pattern to mirror |
| `docs/reference/graveplot-runtime.md` | GRAVEPlot install layout, launcher contract, batch API and its quirks |
| `examples/` | A verified GRAVEPlot batch script, its AptPlot analogue, and sample input |

## Why

The SNAP job-stream plugin set ships an AptPlot Batch step but nothing for
GRAVEPlot, so GRAVEPlot plotting currently runs outside the stream by hand.
Registering a step type closes that gap and makes plot generation part of the
recorded, repeatable stream definition.
