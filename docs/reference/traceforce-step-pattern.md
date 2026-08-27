# Reference: how the TraceForce plugin registers a job step

Source: `~/SNAP-TraceForce-plugin` (github.com/NRC-Research/SNAP-TraceForce-plugin).
The only in-house, buildable, shipping example of a custom SNAP job-step plugin.
Read the actual files; the essentials are excerpted here.

## File map

```
src/com/cafean/traceforce/
    TraceForcePluginData.java     MEPluginData subclass; LABEL="TRACE Piping Force";
                                  getClassPrereqs() -> {"com.cafean.jobstream.client.StreamPlugin"}
    TraceForcePlugin.java         MEFeaturePlugin subclass; mostly empty lifecycle hooks;
                                  addCurrentToolsItems() adds a Tools-menu action (optional)
    jobstream/
        TraceForceDescList.java   registers the step type (below)
        PipingForceStep.java      extends JSStep; Category CAT_PIPING_FORCE;
                                  getBlockType(); getAppType()
        PipingForceStepBeanInfo.java  property sheet
        PipingForceWrapper.java   extends com.cafean.jobstream.server.WrapperModule;
                                  execute() assembles a ProcessBuilder command:
                                  python -m trace_force -i <xtv> -c <yaml> -o <out.th>
        PipingForceAppGenerator.java  registers the "pipingForce" application (below)
        YamlEditorDialog.java     multi-line text editor dialog — reuse pattern for
                                  the Python script editor
build.xml                         ant build against a SNAP install's jars
META-INF/MANIFEST.MF              the wiring (below)
```

## MANIFEST.MF (verbatim)

```
Manifest-Version: 1.0
MEPluginData-Class: com.cafean.traceforce.TraceForcePluginData
MEPluginData-Version: 1.0.5
JobStepList: com.cafean.traceforce.jobstream.TraceForceDescList
FileTypesList: com/cafean/traceforce/jobstream/FileTypes.xml
ApplicationGenerator: com.cafean.traceforce.jobstream.PipingForceAppGenerator

Name: PipingForce
WrapperModule: com.cafean.traceforce.jobstream.PipingForceWrapper
```

`Name:` is a manifest *section*; its `WrapperModule:` attribute binds the
appType string to the server-side wrapper class.

## Step registration (TraceForceDescList.java, verbatim core)

```java
public class TraceForceDescList extends JSStepDescriptionList {
    @Override
    public List<JSStepDescription> getStepDescriptions() {
        ArrayList<JSStepDescription> list = new ArrayList<>();
        list.add(new JSStepDescription(
            "PipingForce",
            "com.cafean.traceforce.jobstream.PipingForceStep",
            PipingForceStep.CAT_PIPING_FORCE,
            "Estimates fluid-induced dynamic forces on piping segments using TRACE XTV output."
        ));
        return list;
    }

    @Override
    public String[] getAllowedPlugins() {
        return new String[] { "TRACE" };   // GRAVEPlot step: allow all
    }
}
```

## Application generator (PipingForceAppGenerator.java, core)

```java
public class PipingForceAppGenerator extends ApplicationGenerator {
    @Override public String getName() { return "pipingForce"; }

    @Override public void generate() {
        ApplicationDefinition application = findApplication("pipingForce");
        if (application == null) {
            application = createApplication("pipingForce", "PipingForce");
            application.setDescription("...");
            setBuiltinLocations(application, "PipingForce");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                application.setLocation("Local", "${SNAPINSTALL}\\bin\\pipingforce.bat");
            }
            addApplication(application);
        } else { /* idempotent re-check + setLocation if drifted */ }
    }
}
```

Note `${SNAPINSTALL}` variable expansion and the idempotent regenerate-on-start
pattern (the generator runs every calc-server start; it must not duplicate).

## Packaging

The release zip is install-relative: `plugins/trace-force.jar`,
`bin/pipingforce.bat` / `bin/pipingforce.sh`, `python/trace_force/...`. The
SNAP distribution applies it via the `overlay/` folder convention; the install
scripts and packer roles copy everything under `overlay/` onto the SNAP
install dir. A GRAVEPlot step plugin ships the same way (jar only; the
GRAVEPlot application itself is a separate, larger install).
