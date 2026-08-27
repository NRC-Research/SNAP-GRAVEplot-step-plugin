# Reference: GRAVEPlot runtime — install layout, launcher, batch API

GRAVEPlot is the in-house Java plotting application (source:
github.com/NRC-Research/grave-plot, local clone `~/grave-plot`; Maven,
JavaFX 22, GraalPy scripting, Playwright PNG rendering). Verified working
install on this Mac: `~/GRAVEPLOTnew`. Release zip: `~/graveplotLatest.zip`.

## Install layout

```
<install>/
  bin/graveplot.sh        POSIX launcher (contract below); launch.cfg; grave_32.ico
  lib/graveplot.jar       Spring Boot fat jar, PropertiesLauncher entry
  lib/*.jar               javafx per-platform, gson, netcdf, graalpy_numpy_binaries,
                          driver-1.47.0 / driver-bundle-1.47.0 (Playwright)
  lib/playwright/         headless Chromium + headless-shell — browsers must sit
                          DIRECTLY here, not in an ms-playwright/ subfolder
  .bvm/                   optional bundled JVM (launcher prefers it, falls back to PATH java)
  install/ license/ manuals/ Uninstall_GRAVEPLOT.jar
```

## Launcher contract (bin/graveplot.sh)

```sh
export PLAYWRIGHT_BROWSERS_PATH="$INSTALL_DIR/lib/playwright"
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
export GRAVEPLOT_INSTALL_DIR="$INSTALL_DIR"
exec "$JAVA_EXEC" \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --enable-native-access=ALL-UNNAMED \
  -Dorg.graalvm.python.vfs.allow_multiple=true \
  $GRAVEPLOT_JVM_ARGS \
  -Dloader.path="$INSTALL_DIR/lib/" \
  -cp "$INSTALL_DIR/lib/graveplot.jar" \
  org.springframework.boot.loader.launch.PropertiesLauncher "$@"
```

`GRAVEPLOT_JVM_ARGS` is the user hook. There is no Windows launcher yet —
writing `graveplot.bat` (same env vars + args) is part of the distribution
task for the step plugin.

## Batch mode

```
graveplot.sh -batch <script>.py
```

- The script runs under GraalPy after GRAVEPlot injects the
  `graveplot_api.py` prelude (canonical copy:
  `~/TRACE-pipe-force-tool/graveplot_api.py`), which wraps the Java
  `PlotScriptingApi` host object (global `api`) in Python builders.
- **`__file__` is NOT defined** in batch scripts. Use `os.getcwd()`; run the
  batch with cwd = the directory holding the data files.
- `plot.render(path, width, height)` dispatches on the output extension:
  `.png` (and other bitmap types) go through Playwright headless Chromium
  screenshots; **`.svg` produces true vector output** via the chart's native
  `window.exportSVG()` (validated/normalized in `ScreenshotService`, which
  also carries a write-HTML fallback when no screenshot engine is available).
  Verified 2026-08-27: a 10-series x 1376-point batch render to `.svg`
  emitted genuine path geometry, no embedded bitmap.
- Interpreter noise on stderr is normal (JVMCI "interpreter only" warning,
  Truffle log redirect hints); exit code is the success signal.

### API idioms (from the working examples)

```python
ds = build_dataset_from_lists(name=..., x_label="Time (s)", y_label="Force (N)",
                              xs=[...], ys=[...])
plot = build_line_plot("Title")
plot.add_datasets(ds1, ds2, ...)
plot.axis.x.label = "Time (s)"        # snake_case bridged to Java camelCase
plot.axis.y.label = "Force (N)"
plot.series[i].line_color = "#1f77b4" # also line_width, point_symbol, symbol_size
plot.render(output_png, width=1200, height=800)
```

Also available: `build_dataset_from_file`, transforms/resampling helpers —
read `graveplot_api.py` for the full facade.

## Fresh-host setup traps (all hit before, documented in
`~/TRACE-pipe-force-tool/graveplot_local_run_report.md`)

1. macOS Gatekeeper: `xattr -r -d com.apple.quarantine <install>` after unzip.
2. `chmod -R +x <install>/lib/playwright` (zip transport strips exec bits).
3. Browsers must live directly in `lib/playwright/` — if the zip stages them
   under `ms-playwright/`, move them up one level.
4. No first-run browser download on RESGC (no egress) — browsers must ship in
   the artifact; `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1` is already set.

## Deployment status (2026-08-27)

The RESGC packer roles (`~/RESGC-packer`, `install_grave` in both provision
playbooks) bake GRAVEPlot into the images: RHEL9 runs the ISL installer
headlessly into `/opt/grave` from `graveplotLatest.zip` in the packer S3
bucket; Windows extracts a pre-baked `graveWindowsLatest.zip` (a zipped
known-good install with a relocatable `bin\graveplot.bat`) to `C:\GRAVE`.
Verified working on this Mac at `~/GRAVEPLOTnew`. Artifact provenance follows
the same S3-bucket discipline as the SNAP payloads (`~/SNAP-distribution`).
