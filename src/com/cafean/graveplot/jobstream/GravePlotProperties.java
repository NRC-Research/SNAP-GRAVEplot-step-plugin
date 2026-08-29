package com.cafean.graveplot.jobstream;

import com.apt.xdr.Xdr;
import com.cafean.jobstream.client.components.JSStepData;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Persisted auxiliary data for the GRAVEPlot step: the Python batch script
 * text and the names of the plot files it produces. Serialized into the
 * step's block in the .med by JSStep, and exported at submit time as a small
 * XML file the server-side wrapper retrieves.
 */
public class GravePlotProperties extends JSStepData {
    public static final int BLOCKVER = 1;
    private static final int MAX_SCRIPT_LENGTH = 4 * 1024 * 1024;

    public static final String DEFAULT_SCRIPT =
        "\"\"\"GRAVEPlot batch script.\n"
        + "\n"
        + "Runs on the calculation server with the step directory as the working\n"
        + "directory. Files connected to the step's data input are staged there\n"
        + "as data01, data02, ... in connection order. __file__ is not defined in\n"
        + "GRAVEPlot batch scripts; use os.getcwd() when a directory path is\n"
        + "needed.\n"
        + "\"\"\"\n"
        + "import os\n"
        + "\n"
        + "# Example: plot the second column of a whitespace-delimited file\n"
        + "# against the first, skipping '#' comment lines.\n"
        + "#\n"
        + "# xs = []\n"
        + "# ys = []\n"
        + "# with open(\"data01\") as f:\n"
        + "#     for line in f:\n"
        + "#         if line.startswith(\"#\") or not line.strip():\n"
        + "#             continue\n"
        + "#         parts = line.split()\n"
        + "#         xs.append(float(parts[0]))\n"
        + "#         ys.append(float(parts[1]))\n"
        + "#\n"
        + "# ds = build_dataset_from_lists(name=\"Series 1\", x_label=\"Time (s)\",\n"
        + "#                               y_label=\"Value\", xs=xs, ys=ys)\n"
        + "#\n"
        + "# Example: import a channel directly from a TRACE plot file (XTV/DMX)\n"
        + "# connected to this step's data input. code_type \"TRACE\" with\n"
        + "# code_file_type \"XTV\" covers .xtv and .dmx files; use_si selects SI\n"
        + "# (True) or British (False) units; channel_name is the TRACE graphics\n"
        + "# variable name from your model (e.g. \"pn-1A01\").\n"
        + "#\n"
        + "# ds = build_dataset_from_file(\"data01\", \"TRACE\", \"XTV\", True,\n"
        + "#                              \"pn-1A01\")\n"
        + "#\n"
        + "# plot = build_line_plot(\"Title\")\n"
        + "# plot.add_datasets(ds)\n"
        + "# plot.axis.x.label = \"Time (s)\"\n"
        + "# plot.axis.y.label = \"Value\"\n"
        + "# plot.render(\"plot.png\", width=1200, height=800)\n";

    private String scriptText = DEFAULT_SCRIPT;
    private String[] outputNames = new String[] { "plot.png" };

    public GravePlotProperties() {
    }

    public GravePlotProperties(DataInputStream in) {
        readBlock(in);
    }

    public String getBlockType() {
        return getClass().getSimpleName();
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText == null ? "" : scriptText;
    }

    public String[] getOutputNames() {
        return outputNames;
    }

    public void setOutputNames(String[] outputNames) {
        this.outputNames = outputNames == null ? new String[0] : outputNames;
    }

    /** Submit-time export: written to the stream stage and retrieved by the wrapper. */
    @Override
    public void write(OutputStream out) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        pw.println("<?xml version=\"1.0\"?>");
        pw.println("<graveplot>");
        pw.println("<script>");
        pw.println(sanitize(scriptText));
        pw.println("</script>");
        pw.println("</graveplot>");
        pw.flush();
    }

    private static String sanitize(String text) {
        return text.replace("&", "&amp;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    @Override
    public boolean writeBlock(DataOutputStream s) {
        try {
            if (!Xdr.writeInt(s, BLOCKVER)) {
                return false;
            }
            byte[] scriptBytes = scriptText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (scriptBytes.length > MAX_SCRIPT_LENGTH || !Xdr.writeBytes(s, scriptBytes)) {
                return false;
            }
            if (!Xdr.writeInt(s, outputNames.length)) {
                return false;
            }
            for (String name : outputNames) {
                if (!Xdr.writeString(s, name, 4096)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean readBlock(DataInputStream in) {
        try {
            int ver = Xdr.readInt(in);
            if (ver < 0 || ver > BLOCKVER) {
                System.err.println("GravePlotProperties: unsupported block version " + ver);
                return false;
            }
            scriptText = new String(Xdr.readBytes(in), java.nio.charset.StandardCharsets.UTF_8);
            int count = Xdr.readInt(in);
            String[] names = new String[count];
            for (int i = 0; i < count; i++) {
                names[i] = Xdr.readString(in);
            }
            outputNames = names;
            return true;
        } catch (Exception e) {
            System.err.println("GravePlotProperties: readBlock error: " + e);
            return false;
        }
    }

    @Override
    public void storeState(String prefix, Hashtable state) {
        String p = prefix == null || prefix.isEmpty() ? "" : prefix.endsWith(".") ? prefix : prefix + ".";
        state.put(p + "scriptText", scriptText);
        state.put(p + "outputNames", outputNames.clone());
    }

    @Override
    public void restoreState(String prefix, Hashtable state) {
        String p = prefix == null || prefix.isEmpty() ? "" : prefix.endsWith(".") ? prefix : prefix + ".";
        Object obj = state.get(p + "scriptText");
        if (obj != null) {
            scriptText = (String) obj;
        }
        obj = state.get(p + "outputNames");
        if (obj != null) {
            outputNames = ((String[]) obj).clone();
        }
    }

    @Override
    public Object clone() {
        GravePlotProperties copy = new GravePlotProperties();
        copy.scriptText = scriptText;
        copy.outputNames = outputNames.clone();
        return copy;
    }

    @Override
    public String toString() {
        return "GRAVEPlot Step Properties";
    }
}
