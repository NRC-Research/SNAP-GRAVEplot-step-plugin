# Reference: AptPlot Batch step class surfaces (the functional analogue)

Extracted with `javap` from `/opt/snap/plugins/js_aptplot.jar` and
`/opt/snap/lib/job_stream.jar` on `ravlympcgg0001` (SNAP 5.0.0, jar dated
2025-10-20). The AptPlot Batch step is the closest existing thing to the
GRAVEPlot step: a script stored in the stream, executed headlessly by the
calc server against parent-step files. Re-extract any other class the same
way: `unzip` the jar, `javap -classpath . <fqcn>`.

Related UI classes in the same jar worth decompiling when building the editor:
`com.cafean.batch.editors.ScriptDialog`, `ScriptEditor`,
`AptplotScriptDocument`, `ScriptTypeEditor`; also
`com.cafean.jobstream.common.util.AptBatchAppGenerator` (the app generator
analogue) and `com.cafean.batch.AptplotBatchStepBeanInfo`.

## com.cafean.batch.AptplotBatchStep

```
public class com.cafean.batch.AptplotBatchStep extends com.cafean.AbstractPlotStep {
  public static java.lang.String DESCRIPTION;
  public static final com.cafean.client.analysis.Category CAT_APP_APTPLOT;
  public static final java.lang.String[] INPUT_LABELS;
  public com.cafean.batch.AptplotBatchStep();
  public com.cafean.batch.AptplotBatchStep(com.apt.xdr.PibFile, int[]);
  public java.lang.String getBlockType();
  public com.cafean.client.analysis.Category getCategory();
  public javax.swing.ImageIcon getCurrentIcon();
  public java.lang.String label();
  public java.lang.String getAppType();
  public void initializeNewStep();
  public com.cafean.aptplot.components.BatchProperties getAuxiliaryData();
  public java.lang.Object clone();
  public java.lang.String[] getRestrictedInputNames();
  public java.lang.String[] getRestrictedOutputNames();
  public boolean supportsPlotDefinitions();
  public boolean generatesFileNames();
  public void buildFileNames();
  public com.cafean.aptplot.components.BatchProperties getBatch();
  public void setBatch(com.cafean.aptplot.components.BatchProperties);
  public com.cafean.aptplot.components.InputDefinition[] getInputDefinitions();
  public void setInputDefinitions(com.cafean.aptplot.components.InputDefinition[]);
  public com.cafean.aptplot.components.OutputDefinition[] getOutputDefinitions();
  public void setOutputDefinitions(com.cafean.aptplot.components.OutputDefinition[]);
  public boolean getInternalScript();
  public void setInternalScript(boolean);
  public java.lang.String getScript();
  public void setScript(java.lang.String);
  public int getAttributeIndex(java.lang.String);
}
```

## com.cafean.batch.AptplotBatchWrapper (server side)

```
public class com.cafean.batch.AptplotBatchWrapper extends com.cafean.jobstream.server.WrapperModule {
  public com.cafean.batch.AptplotBatchWrapper(com.cafean.jobstream.server.ExecutionWrapper);
  public java.lang.String getAppType();
  public boolean retrieveFiles();
  public void processInputs() throws java.io.IOException;
  public void execute() throws java.io.IOException;
}
```

The wrapper lifecycle visible here — `retrieveFiles()` → `processInputs()` →
`execute()` — is the contract the GRAVEPlot wrapper implements. The
constructor taking `ExecutionWrapper` is how the server hands over step
context (step directory, parent outputs, logging).

## com.cafean.AbstractPlotStep (what AptPlot builds on — probably skip it)

Extends `com.cafean.jobstream.client.components.JSStep`; adds the
plot-definition machinery (`PlotProperties[]`, `SetProperties`,
`InputDefinition`/`OutputDefinition` ident bookkeeping, `findPlotByName`,
`getNewPlotIdent`, ...). The GRAVEPlot step needs the script + input/output
subset only; extending `JSStep` directly (TraceForce-style) avoids a
compile-time dependency on `com.cafean.aptplot.components.*`.

## com.cafean.aptplot.components.BatchProperties (persisted step data)

DDT-generated persisted block (PibFile/DataInputStream constructors,
`getBlockType()`, `getAttributeIndex`, FK plumbing) holding exactly the
properties the GRAVEPlot step also needs:

```
  setInternalScript(boolean) / getInternalScript() / getInternalScriptDefault()
  setScript(String) / getScript() / getScriptDefault()
  setInputs(InputDefinition[]) / getInputs()
  setOutputs(OutputDefinition[]) / getOutputs()
  setPlots(PlotProperties[]) / getPlots()          // not needed for GRAVEPlot
```

If the GRAVEPlot step persists its own block, follow the DDT contract used
across SNAP (see `attributeOrder` discussion in `~/SNAP-TRACE-Plugin`
components): PibFile constructor, `getBlockType()`, `getAttributeIndex()`,
write/read symmetry.

## com.cafean.jobstream.server.WrapperModule (base class, from job_stream.jar)

The full protected surface the GRAVEPlot wrapper inherits — step directory,
console/screen readers, process management helpers:

```
Compiled from "WrapperModule.java"
public abstract class com.cafean.jobstream.server.WrapperModule {
  public abstract java.lang.String getAppType();
  protected com.cafean.jobstream.server.WrapperModule(com.cafean.jobstream.server.ExecutionWrapper);
  public boolean initialize(java.util.Map<java.lang.String, java.lang.String>);
  void createDefaultConsoleReader() throws java.io.FileNotFoundException;
  protected void setScreenReader(com.cafean.jobstream.server.util.screen.PipedScreenReader);
  public java.io.File getDefaultScreenFile();
  public boolean retrieveFiles();
  public void processInputs() throws java.io.IOException;
  protected final void processSubstitutions(com.cafean.jobstream.server.components.InputFileHandle, java.io.File, java.util.List<com.cafean.jobstream.server.components.InputSubstitution>, java.util.List<java.lang.String>) throws java.io.IOException;
  protected int getSubstitutions(com.cafean.jobstream.server.components.InputFileHandle, java.util.List<com.cafean.jobstream.server.components.InputSubstitution>, java.util.List<java.lang.String>);
  protected java.lang.String getSubstitutionPath(com.cafean.jobstream.server.components.InputFileHandle);
  protected final void convertLineEndings(java.io.File) throws java.io.IOException;
  public abstract void execute() throws java.io.IOException;
  final synchronized void postExecute();
  public final synchronized boolean getConsoleHistory(com.cafean.jobstream.server.util.screen.ScreenListener);
  public void processOutputs();
  public void updateKeywords(com.cafean.jobstream.server.components.Task);
  public void updateTaskDefinition(com.cafean.jobstream.server.components.Task);
  public boolean sendFiles();
  protected boolean sendIntermediateFile(com.cafean.jobstream.server.components.AbstractFileHandle);
  protected boolean retrieveAuxiliaryData(java.io.File);
  protected boolean retrieveAuxiliaryStreamData(java.lang.String, java.io.File);
  protected void sendFileStoredNotification(com.cafean.jobstream.server.components.AbstractFileHandle, java.net.URI, boolean);
  protected void sendFileRetrievedNotification(com.cafean.jobstream.server.components.AbstractFileHandle, java.net.URI, boolean);
  public void sendStreamLog(int, java.lang.String);
  public void sendFinalNotifications(com.cafean.jobstream.server.rpc.StreamRequester, boolean);
  public void dispose();
  public com.cafean.jobstream.server.wrapper.AbstractInteractivePortHandler getPortHandler();
  public void terminate();
  public final boolean isTerminated();
  public boolean isGraphicsSupported();
  public com.cafean.jobstream.server.wrapper.GraphicsFile createGraphicsFile(java.lang.String);
  public void createGraphicsFiles();
  public com.cafean.jobstream.server.wrapper.GraphicsFile getGraphicsFile(java.lang.String);
  public com.cafean.jobstream.server.wrapper.GraphicsFile[] getGraphicsFiles();
  protected java.lang.String[] getEnvParameters();
  protected void setEnvParameterMap(java.util.Map<java.lang.String, java.lang.String>);
  public boolean getRestartInfo(java.lang.String, com.cafean.Number.IntegerArray, com.cafean.Number.DoubleArray);
  protected final java.lang.Process executeApplication(java.lang.String[], java.io.File);
  protected final java.lang.Process executeApplication(java.lang.String[], java.io.File, boolean);
  protected final java.lang.Process executeApplication(java.lang.String[], java.io.File, boolean, boolean);
  protected java.lang.Process getProcess();
  protected void putFile(com.cafean.jobstream.server.components.AbstractFileHandle, java.net.URI) throws java.io.IOException;
```
