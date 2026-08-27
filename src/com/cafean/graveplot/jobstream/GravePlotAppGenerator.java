package com.cafean.graveplot.jobstream;

import com.cafean.jobstream.common.components.ApplicationDefinition;
import com.cafean.jobstream.common.util.ApplicationGenerator;

/**
 * Registers the "graveplot" application definition on calc-server start.
 * GRAVEPlot installs separately from SNAP, so the generated Local location
 * points at the conventional install path per platform; an existing
 * user-configured location is never overwritten.
 */
public class GravePlotAppGenerator extends ApplicationGenerator {
    private static final String DESCRIPTION =
        "GRAVEPlot is a 2D plotting tool with a Python batch scripting interface, "
        + "used by the GRAVEPlot job step to render plots from step outputs.";

    @Override
    public String getName() {
        return GravePlotStep.APP_NAME;
    }

    @Override
    public void generate() {
        ApplicationDefinition application = findApplication(GravePlotStep.APP_NAME);
        if (application == null) {
            debug("Creating a new application definition for graveplot.");
            application = createApplication(GravePlotStep.APP_NAME, GravePlotStep.APP_TYPE);
            application.setDescription(DESCRIPTION);
            application.setLocation("Local", getDefaultLocation());
            addApplication(application);
        } else if (application.findLocation("Local") == null) {
            debug("Adding a default Local location to the graveplot application definition.");
            application.setLocation("Local", getDefaultLocation());
            setModified(application);
        }
    }

    private static String getDefaultLocation() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return "C:\\Program Files\\graveplot\\bin\\graveplot.bat";
        }
        return "/opt/graveplot/bin/graveplot.sh";
    }
}
