//*************************************************************************
// A work of the US Nuclear Regulatory Commission. Prepared by NRC staff
// as part of official duties; not subject to copyright protection in the
// United States under 17 U.S.C. 105.
//*************************************************************************
//

package com.cafean.graveplot.jobstream;

import com.cafean.jobstream.server.ExecutionWrapper;
import com.cafean.jobstream.server.WrapperModule;
import com.cafean.jobstream.server.components.InputFileHandle;
import com.cafean.jobstream.server.components.OutputFileHandle;
import com.cafean.jobstream.server.rpc.StreamRequester;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Server-side execution module for the GRAVEPlot step. Retrieves the step's
 * auxiliary data (the batch script), writes it into the step directory, and
 * runs the graveplot launcher in batch mode with the step directory as the
 * working directory so scripts can open parent-step files by plain name.
 */
public class GravePlotWrapper extends WrapperModule {
    private final AtomicBoolean scriptIncluded = new AtomicBoolean(true);
    private final AtomicBoolean failed = new AtomicBoolean(false);
    private int exitCode = -999;

    public GravePlotWrapper(ExecutionWrapper parent) {
        super(parent);
    }

    @Override
    public String getAppType() {
        return GravePlotStep.APP_TYPE;
    }

    @Override
    public boolean initialize(Map<String, String> moduleOptions) {
        if (!super.initialize(moduleOptions)) {
            return false;
        }
        String included = moduleOptions.get("SCRIPT_INCLUDED");
        if (included != null) {
            scriptIncluded.set(Boolean.parseBoolean(included));
        }
        return true;
    }

    @Override
    public boolean retrieveFiles() {
        if (!super.retrieveFiles()) {
            return false;
        }
        File auxFile = getAuxFile();
        if (!retrieveAuxiliaryData(auxFile)) {
            return false;
        }
        try {
            if (scriptIncluded.get()) {
                String script = readScriptFromAuxFile(auxFile);
                if (script == null) {
                    return false;
                }
                if (!writeScript(script)) {
                    return false;
                }
                createScriptFileHandle(getScriptFile());
            }
        } finally {
            auxFile.delete();
        }
        return true;
    }

    @Override
    public void execute() throws IOException {
        File scriptFile = getScriptFile();
        if (!scriptFile.exists()) {
            error("GRAVEPlot batch script not found: " + scriptFile.getName());
            failed.set(true);
            return;
        }

        File launcher = getApplicationExecutable();
        if (launcher == null) {
            error("The GRAVEPlot application executable is unavailable: "
                + String.valueOf(getParent().getApplication()));
            failed.set(true);
            return;
        }

        ArrayList<String> command = new ArrayList<>();
        command.add(launcher.getAbsolutePath());
        if (getParent().getApplication().getArguments() != null) {
            for (Object arg : getParent().getApplication().getArguments()) {
                command.add(String.valueOf(arg));
            }
        }
        command.add("-batch");
        command.add(scriptFile.getName());

        Process proc = executeApplication(command.toArray(new String[0]), getDefaultScreenFile());
        if (proc == null) {
            failed.set(true);
            return;
        }
        try {
            exitCode = proc.waitFor();
        } catch (InterruptedException ex) {
            error("GRAVEPlot execution interrupted: " + ex.getMessage());
            failed.set(true);
            return;
        }
        if (exitCode != 0) {
            error("GRAVEPlot exited with code " + exitCode + ". See the screen file for details.");
            failed.set(true);
            return;
        }
        checkDeclaredOutputs();
    }

    private void checkDeclaredOutputs() {
        for (OutputFileHandle output : getParent().getOutputs()) {
            if ("screen".equals(output.getLabel())) {
                continue;
            }
            File file = new File(getParent().getWorkingDir(), output.getName());
            if (!file.exists()) {
                error("Declared plot output '" + output.getName()
                    + "' was not produced by the GRAVEPlot script.");
                failed.set(true);
            }
        }
    }

    @Override
    public void sendFinalNotifications(StreamRequester requester, boolean success) {
        super.sendFinalNotifications(requester, success && !failed.get());
    }

    private File getAuxFile() {
        return new File(getParent().getWorkingDir(), getParent().getTaskName() + ".aux.xml");
    }

    protected File getScriptFile() {
        if (!scriptIncluded.get()) {
            for (InputFileHandle input : findInputs("script")) {
                File script = new File(getParent().getWorkingDir(), input.getName());
                if (script.exists()) {
                    return script;
                }
            }
        }
        return new File(getParent().getWorkingDir(), getParent().getTaskName() + ".py");
    }

    private String readScriptFromAuxFile(File auxFile) {
        try (FileInputStream in = new FileInputStream(auxFile)) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(in);
            Element root = document.getDocumentElement();
            NodeList scripts = root.getElementsByTagName("script");
            if (scripts.getLength() < 1) {
                error("Auxiliary step data contains no script element.");
                return null;
            }
            return scripts.item(0).getTextContent().trim();
        } catch (Exception ex) {
            error("Unable to parse GRAVEPlot step data: " + ex);
            return null;
        }
    }

    private boolean writeScript(String script) {
        File scriptFile = getScriptFile();
        // LF endings regardless of platform; GraalPy accepts either but LF is canonical.
        String normalized = script.replace("\r\n", "\n").replace("\r", "\n");
        try (Writer out = new OutputStreamWriter(new FileOutputStream(scriptFile), StandardCharsets.UTF_8)) {
            out.write(normalized);
            if (!normalized.endsWith("\n")) {
                out.write("\n");
            }
            return true;
        } catch (IOException ex) {
            error("Failed to write script file: " + scriptFile, ex);
            return false;
        }
    }

    private void createScriptFileHandle(File file) {
        InputFileHandle script = new InputFileHandle();
        script.setLabel("script");
        script.setName(file.getName());
        script.setAscii(true);
        script.setIntermediate(false);
        script.setAutoStage(true);
        script.setType("Python:Script");
        getParent().addInput(script);
    }
}
