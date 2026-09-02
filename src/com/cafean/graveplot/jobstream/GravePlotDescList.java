//*************************************************************************
// A work of the US Nuclear Regulatory Commission. Prepared by NRC staff
// as part of official duties; not subject to copyright protection in the
// United States under 17 U.S.C. 105.
//*************************************************************************
//

package com.cafean.graveplot.jobstream;

import com.cafean.jobstream.client.util.JSStepDescription;
import com.cafean.jobstream.client.util.JSStepDescriptionList;
import java.util.ArrayList;
import java.util.List;

public class GravePlotDescList extends JSStepDescriptionList {
    public static final String DESCRIPTION =
        "GRAVEPlot is a 2D plotting tool with a Python batch scripting interface. "
        + "The GRAVEPlot step renders plots (PNG images) from the output files of "
        + "upstream job steps by executing a Python batch script headlessly on the "
        + "calculation server.";

    @Override
    public List<JSStepDescription> getStepDescriptions() {
        ArrayList<JSStepDescription> list = new ArrayList<>();
        list.add(new JSStepDescription(
            "GravePlot",
            "com.cafean.graveplot.jobstream.GravePlotStep",
            GravePlotStep.CAT_GRAVEPLOT,
            DESCRIPTION
        ));
        return list;
    }

    @Override
    public String[] getAllowedPlugins() {
        // Empty means the step is available in streams for every analysis plugin.
        return new String[0];
    }
}
