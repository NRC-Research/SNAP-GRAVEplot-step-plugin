//*************************************************************************
// A work of the US Nuclear Regulatory Commission. Prepared by NRC staff
// as part of official duties; not subject to copyright protection in the
// United States under 17 U.S.C. 105.
//*************************************************************************
//

package com.cafean.graveplot.jobstream;

import com.cafean.jobstream.client.components.JSStep;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.beans.SimpleBeanInfo;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GravePlotStepBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor scriptDesc = new PropertyDescriptor("script", GravePlotStep.class);
            scriptDesc.setDisplayName("Batch Script");
            scriptDesc.setShortDescription(
                "The Python batch script GRAVEPlot executes in the step directory.");
            scriptDesc.setPropertyEditorClass(ScriptEditor.class);

            PropertyDescriptor outputsDesc = new PropertyDescriptor("outputFiles", GravePlotStep.class);
            outputsDesc.setDisplayName("Plot Output Files");
            outputsDesc.setShortDescription(
                "Comma-separated names of the plot files the script renders "
                + "(e.g. forces.png, detail.png). Each becomes a declared step output.");

            return new PropertyDescriptor[] { scriptDesc, outputsDesc };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return new PropertyDescriptor[0];
        }
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                Introspector.getBeanInfo(JSStep.class)
            };
        } catch (IntrospectionException e) {
            return super.getAdditionalBeanInfo();
        }
    }

    @Override
    public Image getIcon(int iconKind) {
        if (iconKind == BeanInfo.ICON_COLOR_16x16) {
            return loadImage("/com/cafean/graveplot/images/graveplot16.png");
        }
        return null;
    }

    public static class ScriptEditor extends PropertyEditorSupport {
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new ScriptEditorPanel(this);
        }

        @Override
        public String getAsText() {
            String value = (String) getValue();
            if (value == null || value.trim().isEmpty()) {
                return "<no script>";
            }
            int lines = value.split("\n").length;
            return "<" + lines + " line script>";
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            // The script is edited through the custom editor only; ignore
            // attempts to overwrite it with the summary text.
        }
    }

    private static class ScriptEditorPanel extends JPanel {
        ScriptEditorPanel(final ScriptEditor editor) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            final JLabel summary = new JLabel(editor.getAsText());
            JButton editButton = new JButton("Edit Script...");
            add(editButton);
            add(summary);

            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Window owner = SwingUtilities.getWindowAncestor(ScriptEditorPanel.this);
                    ScriptEditorDialog dialog = new ScriptEditorDialog(owner, (String) editor.getValue());
                    dialog.setVisible(true);
                    if (dialog.isAccepted()) {
                        editor.setValue(dialog.getScript());
                        summary.setText(editor.getAsText());
                    }
                }
            });
        }
    }
}
