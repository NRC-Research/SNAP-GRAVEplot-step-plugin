# GRAVEPlot 1.0.0/1.0.1 defect report: batch mode fails for any script over 30 seconds

**Product**: GRAVEPlot — NRC-owned codebase, built and distributed by ISL.
Defect confirmed in **both** builds available from the ISL customer portal:
1.0.0 (downloaded 2026-05-26, classes dated 2026-02-24) and **1.0.1**
(downloaded 2026-08-27 via the portal installer, classes dated 2026-07-28 —
served behind the portal page still labeled "Revision 1.0"). The
`com.plot.grave.batch` package in these builds is not present in any
NRC-held source; the current source snapshot should be delivered to NRC
alongside the fix.
**Severity**: blocks all unattended/batch use on typical server hardware
**Reported by**: NRC (RESGC infrastructure integration)

## Summary

`graveplot -batch <script>.py` exits with code 3 whenever the script takes
longer than 30 seconds, even though the script actually completes and its
outputs (e.g. rendered PNGs) are written. The batch front end submits the
script to the embedded backend over HTTP with a hard-coded 30-second request
timeout; when it fires, the launcher reports failure and begins shutdown
while the script worker is still running.

## Reproduction (any platform)

```python
# sleep35.py
import time
print("start")
time.sleep(35)
print("finished after 35s")
```

```
$ graveplot.sh -batch sleep35.py ; echo $?
3
```

A 25-second variant of the same script exits 0 and prints its output,
bracketing the boundary at the 30-second mark. Verified 2026-08-27 on
macOS (Apple Silicon) and RHEL 9.8 (x86_64) against 1.0.0, and on macOS
against a fresh 1.0.1 install; the decompiled 1.0.1
`BatchModeRunner.executeScriptRemotely` carries the identical hard-coded
30-second request timeout.

## Real-world impact

On server-class Linux hosts, cold-start overhead alone (JVM + GraalPy venv
extraction + Playwright Chromium launch) exceeds 30 seconds, so **every**
real plotting script fails there — observed on RHEL 9.8 where a simple
sine-wave render took ~55 s end to end: the PNG was written, but the
launcher had already timed out and exited 3. Truly cold first runs can be
killed before the render completes. Any automation that checks the exit
code (CI, job schedulers, the SNAP job-stream system) treats these runs
as failures.

## Diagnosis

`com.plot.grave.batch.BatchModeRunner.executeScriptRemotely` performs a
single synchronous POST to `/api/scripting/python/run`:

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .timeout(Duration.ofSeconds(30L))   // <-- hard-coded
    ...
```

On timeout, `java.net.http.HttpTimeoutException: request timed out`
propagates out of `BatchModeRunner` (observed at line 938), the
`RuntimeException` handler exits with code 3, and the Spring graceful
shutdown races the still-running script worker. A secondary crash in the
Playwright driver teardown (`Cannot find module './../../package.json'`)
can appear in the console when the shutdown interrupts the driver, which
obscures the root cause.

## Suggested fix

Batch scripts legitimately run for minutes. Either:

1. remove the per-request timeout for `/api/scripting/python/run` (leave the
   5-second connect timeout), or
2. make it configurable (system property / environment variable) with a
   default of hours rather than seconds, or
3. switch to an async submit-and-poll protocol.

In all cases the launcher's exit code should reflect the script's actual
outcome.
