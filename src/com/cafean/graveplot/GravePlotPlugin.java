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

    @Override
    public boolean isAssociated(AbstractModel model) {
        return true;
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
