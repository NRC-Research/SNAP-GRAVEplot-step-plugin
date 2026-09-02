//*************************************************************************
// A work of the US Nuclear Regulatory Commission. Prepared by NRC staff
// as part of official duties; not subject to copyright protection in the
// United States under 17 U.S.C. 105.
//*************************************************************************
//

package com.cafean.graveplot.jobstream;

import com.apt.xdr.PibFile;
import com.cafean.client.analysis.Category;
import com.cafean.jobstream.client.components.FileTypeRef;
import com.cafean.jobstream.client.components.InputDescrip;
import com.cafean.jobstream.client.components.JSStep;
import com.cafean.jobstream.client.components.OutputDescrip;
import com.cafean.jobstream.client.components.StreamConnection;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.undo.CompoundEdit;

public class GravePlotStep extends JSStep {
    public static final Category CAT_GRAVEPLOT = new Category(
        "GRAVEPlot",
        "/com/cafean/graveplot/images/graveplot16.png"
    );

    public static final String APP_TYPE = "GravePlot";
    public static final String APP_NAME = "graveplot";

    private volatile GravePlotProperties stepProperties;

    public GravePlotStep() {
    }

    public GravePlotStep(PibFile pibFile, int[] blockparm) {
        super(pibFile, blockparm);
    }

    @Override
    public String getBlockType() {
        return getClass().getSimpleName();
    }

    @Override
    public Category getCategory() {
        return CAT_GRAVEPLOT;
    }

    @Override
    public String label() {
        return "GRAVEPlot";
    }

    @Override
    public String getAppType() {
        return APP_TYPE;
    }

    @Override
    public GravePlotProperties getAuxiliaryData() {
        if (stepProperties == null) {
            stepProperties = new GravePlotProperties();
        }
        return stepProperties;
    }

    public void setStepProperties(GravePlotProperties properties) {
        this.stepProperties = properties;
    }

    @Override
    public void initializeNewStep() {
        super.initializeNewStep();
        setApplicationName(APP_NAME);
        // Optional external script file; when unconnected the internal script
        // text is exported instead (see updateStep / the wrapper).
        addInput("Python", "Script", "script", "GravePlot.py", false);
        // Data files pulled from connected parent steps into the step directory.
        // GRAVEPlot reads text data (TH, CSV, generic ASCII) and, since its
        // 1.0.x analysis-code bindings, TRACE XTV plot files directly. Extend
        // this list as further code plot types are verified against GRAVEPlot.
        FileTypeRef[] dataTypes = new FileTypeRef[] {
            new FileTypeRef("Generic", "Ascii"),
            new FileTypeRef("Generic", "TH"),
            new FileTypeRef("TRACE", "XTV"),
        };
        addInputSet(dataTypes, "data", null, false);
        addOutput("Generic", "Ascii", "screen", "GravePlot.screen");
        buildFileNames();
    }

    @Override
    public boolean generatesFileNames() {
        return true;
    }

    @Override
    public void buildFileNames(int taskIndex) {
        String taskName = taskIndex >= 0 ? getTaskName(taskIndex) : getName();
        OutputDescrip screen = findOutputDesc("screen");
        if (screen != null) {
            screen.setName(taskName + ".screen");
        }
        InputDescrip script = findInputDesc("script");
        if (script != null) {
            script.setName(taskName + ".py");
        }
        super.buildFileNames(taskIndex);
    }

    @Override
    public void buildFileNames() {
        buildFileNames(-1);
        buildDataFileNames();
        // Regenerate the plot outputs from the declared output file names,
        // preserving the settings of outputs whose label is unchanged.
        HashMap<String, OutputDescrip> outputMap = new HashMap<>();
        for (int i = getOutputCount() - 1; i >= 1; --i) {
            OutputDescrip output = getOutput(i);
            outputMap.put(output.getLabel(), output);
            removeOutput(i);
        }
        for (String name : getAuxiliaryData().getOutputNames()) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            String fileName = name.trim();
            OutputDescrip newOutput = addOutput(imageTypeSource(fileName), imageTypeName(fileName), fileName, fileName);
            OutputDescrip oldOutput = outputMap.get(fileName);
            if (oldOutput != null) {
                newOutput.setVisible(oldOutput.getVisible());
                newOutput.setAutoCSDelete(oldOutput.getAutoCSDelete());
                newOutput.setAutoStage(oldOutput.getAutoStage());
                newOutput.setReference(oldOutput.getReference());
                newOutput.setStoragePatternActive(oldOutput.getStoragePatternActive());
                newOutput.setStoragePattern(oldOutput.getStoragePattern());
                newOutput.setKeywords(oldOutput.getKeywords());
            }
        }
    }

    private static String imageTypeSource(String fileName) {
        return "Image";
    }

    private static String imageTypeName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "JPG";
        }
        if (lower.endsWith(".svg")) {
            return "SVG";
        }
        if (lower.endsWith(".pdf")) {
            return "PDF";
        }
        return "PNG";
    }

    @Override
    public void updateStep(CompoundEdit edit) {
        ArrayList<StreamConnection> scriptInputs = findInput("script");
        setBoolValue("SCRIPT_INCLUDED", scriptInputs == null || scriptInputs.isEmpty());
        buildDataFileNames();
        super.updateStep(edit);
    }

    // The data input is a set, and the Model Editor derives its expected
    // runtime files from the set-file-name list — an empty list reads as
    // "0 Sources Expected" and fails validation as soon as anything is
    // connected. Stage names are data01, data02, ... in connection order;
    // batch scripts open them by those names (GRAVEPlot's file importers
    // take the file type explicitly, so the missing extension is fine).
    private void buildDataFileNames() {
        InputDescrip data = findInputDesc("data");
        if (data == null) {
            return;
        }
        int count = 0;
        ArrayList<StreamConnection> cons = findInput("data");
        if (cons != null) {
            for (StreamConnection con : cons) {
                count += estimateInputSize(con);
            }
        }
        String[] names = new String[count];
        int digits = Math.max(2, Integer.toString(count).length());
        for (int i = 0; i < count; i++) {
            names[i] = String.format("data%0" + digits + "d", i + 1);
        }
        data.setSetFileNames(names);
    }

    public String getScript() {
        return getAuxiliaryData().getScriptText();
    }

    public void setScript(String script) {
        getAuxiliaryData().setScriptText(script);
        updateStep(null);
    }

    /** Comma-separated plot output file names, exposed as a simple text property. */
    public String getOutputFiles() {
        return String.join(", ", getAuxiliaryData().getOutputNames());
    }

    public void setOutputFiles(String value) {
        ArrayList<String> names = new ArrayList<>();
        if (value != null) {
            for (String part : value.split(",")) {
                String name = part.trim();
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        getAuxiliaryData().setOutputNames(names.toArray(new String[0]));
        buildFileNames();
        updateStep(null);
    }

    @Override
    public boolean isOkayForExport(boolean writeMessages) {
        return isOkayForExport(writeMessages, false);
    }

    @Override
    public boolean isOkayForExport(boolean writeMessages, boolean isSubmitting) {
        healApplicationName();
        boolean okay = super.isOkayForExport(writeMessages, isSubmitting);
        ArrayList<StreamConnection> scriptInputs = findInput("script");
        boolean externalScript = scriptInputs != null && !scriptInputs.isEmpty();
        if (!externalScript && getScript().trim().isEmpty()) {
            okay = false;
            if (writeMessages) {
                addMessage("No GRAVEPlot batch script has been defined.", 3);
            }
        }
        if (getAuxiliaryData().getOutputNames().length == 0) {
            if (writeMessages) {
                addMessage("No plot output files are declared; rendered plots will "
                    + "not be tracked by the stream.", 1);
            }
        }
        return okay;
    }

    private void healApplicationName() {
        String appName = getApplicationName();
        if (appName == null || appName.trim().isEmpty()
                || appName.equals("<No Default>") || appName.equalsIgnoreCase(APP_TYPE)) {
            setApplicationName(APP_NAME);
        }
    }

    @Override
    public boolean readBlock(PibFile pibFile, int[] blockparm) {
        boolean success = super.readBlock(pibFile, blockparm);
        if (success) {
            healApplicationName();
        }
        return success;
    }

    @Override
    public boolean readBlock(java.io.DataInputStream in, int version) {
        boolean success = super.readBlock(in, version);
        if (success) {
            healApplicationName();
        }
        return success;
    }

    @Override
    public Object clone() {
        GravePlotStep clone = (GravePlotStep) super.clone();
        clone.setStepProperties((GravePlotProperties) getAuxiliaryData().clone());
        return clone;
    }
}
