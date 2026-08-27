package com.cafean.graveplot.jobstream;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/** Modal editor for the GRAVEPlot Python batch script. */
public class ScriptEditorDialog extends JDialog {
    private RSyntaxTextArea textArea;
    private boolean accepted = false;

    public ScriptEditorDialog(Window parent, String script) {
        super(parent, "GRAVEPlot Batch Script", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(script);
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents(String script) {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setText(script == null ? "" : script);
        textArea.setCaretPosition(0);

        add(new RTextScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accepted = true;
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getScript() {
        return textArea.getText();
    }
}
