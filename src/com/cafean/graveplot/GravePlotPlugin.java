package com.cafean.graveplot;

import com.cafean.CodePlugins.MEFeaturePlugin;
import com.cafean.client.analysis.AbstractModel;
import java.io.IOException;
import java.util.Vector;

public class GravePlotPlugin extends MEFeaturePlugin {
    public GravePlotPlugin() {
        super(GravePlotPluginData.LABEL);
    }

    @Override
    public String getPluginId() {
        return GravePlotPluginData.LABEL;
    }

    @Override
    public String getPluginInfo() {
        return "GRAVEPlot batch plotting integration for the SNAP Job Stream system.";
    }

    /**
     * Never associated, because this plug-in keeps nothing in a model:
     * {@link #load} and {@link #save} are empty, and its state lives in job
     * streams. Answering true cost real data. Every save wrote an empty
     * 16-byte Plugin section for this plug-in, and because feature plug-ins
     * register alphabetically, "GRAVEPlot" landed before "MDOCS" - in front
     * of the section holding the analyst's notes. CAFEAN's MED loader
     * dispatches only the first Plugin section it meets (MEDUtil.loadModel
     * stops scanning at one; componentReadComplete loads only what was
     * recorded), so from the first resave onward the notes no longer loaded,
     * and on the save after that MDocsPlugin.isAssociated saw no documents
     * and the notes were dropped from the file entirely. A plug-in with
     * nothing to persist must not claim the model.
     */
    @Override
    public boolean isAssociated(AbstractModel model) {
        return false;
    }

    @Override
    public void load(AbstractModel model, com.apt.xdr.PibFile pibFile) {}

    @Override
    public void save(AbstractModel model, com.apt.xdr.PibFile pibFile) {}

    @Override
    public void modelRemoved(AbstractModel model) {}

    @Override
    public void modelAdded(AbstractModel model) {}

    @Override
    public javax.help.HelpSet getPluginHelpSet() {
        return null;
    }

    @Override
    public void processCommand(Vector command) throws IOException {}

    @Override
    public void loadMainMenuItems() {}

    @Override
    public void loadViewMenuItems(com.cafean.client.ui.DrawnView view) {}

    @Override
    public void loadSettings(com.cafean.utils.Configurator config) {}

    @Override
    public void storeSettings(com.cafean.utils.Configurator config) {}
}
