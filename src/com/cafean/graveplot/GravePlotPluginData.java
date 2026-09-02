//*************************************************************************
// A work of the US Nuclear Regulatory Commission. Prepared by NRC staff
// as part of official duties; not subject to copyright protection in the
// United States under 17 U.S.C. 105.
//*************************************************************************
//

package com.cafean.graveplot;

import com.cafean.CodePlugins.MEPlugin;
import com.cafean.CodePlugins.MEPluginData;

public class GravePlotPluginData extends MEPluginData {
    public static final String LABEL = "GRAVEPlot";

    @Override
    public String getPluginClass() {
        return "com.cafean.graveplot.GravePlotPlugin";
    }

    @Override
    public String getPluginId() {
        return LABEL;
    }

    @Override
    public MEPlugin loadPlugin() {
        return new GravePlotPlugin();
    }

    @Override
    public String[] getClassPrereqs() {
        return new String[] {
            "com.cafean.jobstream.client.StreamPlugin"
        };
    }
}
