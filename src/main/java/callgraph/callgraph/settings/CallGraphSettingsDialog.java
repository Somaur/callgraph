package callgraph.callgraph.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for configuring CallGraph plugin settings.
 */
public class CallGraphSettingsDialog extends DialogWrapper {
    private final CallGraphSettings settings;
    private final Project project;
    
    private JBRadioButton useCustomColorButton;
    private JBRadioButton useIdeColorButton;
    private JButton colorPickerButton;
    private JPanel colorPreview;
    
    private String selectedBackgroundType;
    private String selectedCustomColor;
    
    // Store original settings to revert if canceled
    private final String originalBackgroundType;
    private final String originalCustomColor;
    
    /**
     * Listener for real-time settings changes
     */
    public interface SettingsChangeListener {
        void onSettingsChanged(String backgroundType, String customBackgroundColor);
    }
    
    private SettingsChangeListener changeListener;

    public CallGraphSettingsDialog(Project project) {
        super(project, true);
        this.project = project;
        this.settings = CallGraphSettings.getInstance(project);
        
        // Store original settings for cancel action
        this.originalBackgroundType = settings.getBackgroundType();
        this.originalCustomColor = settings.getCustomBackgroundColor();
        
        // Initialize current working values
        this.selectedBackgroundType = this.originalBackgroundType;
        this.selectedCustomColor = this.originalCustomColor;
        
        // Set the change listener to update the browser in real-time
        this.changeListener = (backgroundType, customBackgroundColor) -> {
            // Apply changes immediately
            settings.setBackgroundType(backgroundType);
            settings.setCustomBackgroundColor(customBackgroundColor);
            callgraph.callgraph.browser.BrowserManager.getInstance(project).applySettings();
        };
        
        setTitle("Call Graph Settings");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setPreferredSize(new Dimension(400, 180));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel appearancePanel = createAppearancePanel();
        tabbedPane.addTab("Appearance", appearancePanel);
        
        dialogPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return dialogPanel;
    }

    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(5));
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        
        // Background color section
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Background Color", 
                TitledBorder.LEFT, TitledBorder.TOP));
        
        ButtonGroup backgroundGroup = new ButtonGroup();
        
        useIdeColorButton = new JBRadioButton("Use IDE Editor Background");
        useIdeColorButton.setSelected(CallGraphSettings.BACKGROUND_TYPE_IDE.equals(selectedBackgroundType));
        backgroundGroup.add(useIdeColorButton);
        
        useCustomColorButton = new JBRadioButton("Use Custom Color");
        useCustomColorButton.setSelected(CallGraphSettings.BACKGROUND_TYPE_CUSTOM.equals(selectedBackgroundType));
        backgroundGroup.add(useCustomColorButton);
        
        // Color picker panel with color preview
        JPanel colorPickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        colorPickerButton = new JButton("Choose Color...");
        
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(24, 24));
        colorPreview.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        updateColorPreview();
        
        colorPickerButton.addActionListener(e -> {
            Color initialColor;
            try {
                initialColor = Color.decode(selectedCustomColor);
            } catch (NumberFormatException ex) {
                initialColor = Color.BLACK;
            }
            
            Color color = JColorChooser.showDialog(panel, "Choose Background Color", initialColor);
            
            if (color != null) {
                selectedCustomColor = "#" + ColorUtil.toHex(color);
                updateColorPreview();

                useCustomColorButton.setSelected(true);
                selectedBackgroundType = CallGraphSettings.BACKGROUND_TYPE_CUSTOM;
                
                // Notify changes to update the UI immediately
                if (changeListener != null) {
                    changeListener.onSettingsChanged(selectedBackgroundType, selectedCustomColor);
                }
            }
        });
        
        colorPickerPanel.add(colorPickerButton);
        colorPickerPanel.add(colorPreview);
        
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0;
        bc.gridy = 0;
        bc.anchor = GridBagConstraints.WEST;
        bc.insets = JBUI.insets(3, 3, 1, 3);
        bc.fill = GridBagConstraints.HORIZONTAL;
        backgroundPanel.add(useIdeColorButton, bc);
        
        bc.gridx = 0;
        bc.gridy = 1;
        bc.insets = JBUI.insets(1, 3, 1, 3);
        backgroundPanel.add(useCustomColorButton, bc);
        
        bc.gridx = 0;
        bc.gridy = 2;
        bc.insets = JBUI.insets(0, 20, 3, 3);
        backgroundPanel.add(colorPickerPanel, bc);
        
        panel.add(backgroundPanel, c);
        
        // Setup action listeners for radio buttons
        ActionListener radioListener = actionEvent -> {
            if (useCustomColorButton.isSelected()) {
                selectedBackgroundType = CallGraphSettings.BACKGROUND_TYPE_CUSTOM;
                colorPickerButton.setEnabled(true);
            } else {
                selectedBackgroundType = CallGraphSettings.BACKGROUND_TYPE_IDE;
                colorPickerButton.setEnabled(false);
            }
            
            // Notify listener about the change to update the UI immediately
            if (changeListener != null) {
                changeListener.onSettingsChanged(selectedBackgroundType, selectedCustomColor);
            }
        };
        
        useCustomColorButton.addActionListener(radioListener);
        useIdeColorButton.addActionListener(radioListener);
        
        // Initialize enabled state
        colorPickerButton.setEnabled(useCustomColorButton.isSelected());
        
        return panel;
    }
    
    private void updateColorPreview() {
        try {
            colorPreview.setBackground(Color.decode(selectedCustomColor));
        } catch (NumberFormatException e) {
            colorPreview.setBackground(Color.BLACK);
        }
    }

    @Override
    protected void doOKAction() {
        // Final save of settings (should be same as current state since we apply changes immediately)
        settings.setBackgroundType(selectedBackgroundType);
        settings.setCustomBackgroundColor(selectedCustomColor);
        super.doOKAction();
    }

    public void resetToOriginal() {
        // Reset stored values to original values
        selectedBackgroundType = originalBackgroundType;
        selectedCustomColor = originalCustomColor;
        
        // Reset UI to match original values
        useIdeColorButton.setSelected(CallGraphSettings.BACKGROUND_TYPE_IDE.equals(originalBackgroundType));
        useCustomColorButton.setSelected(CallGraphSettings.BACKGROUND_TYPE_CUSTOM.equals(originalBackgroundType));
        colorPickerButton.setEnabled(CallGraphSettings.BACKGROUND_TYPE_CUSTOM.equals(originalBackgroundType));
        updateColorPreview();
        
        // Apply original settings
        settings.setBackgroundType(originalBackgroundType);
        settings.setCustomBackgroundColor(originalCustomColor);
        callgraph.callgraph.browser.BrowserManager.getInstance(project).applySettings();
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action[] defaultActions = super.createActions();
        
        // Replace the cancel action with a new action named "Reset"
        for (int i = 0; i < defaultActions.length; i++) {
            if (defaultActions[i] == getCancelAction()) {
                defaultActions[i] = new DialogWrapperAction("Reset") {
                    @Override
                    protected void doAction(ActionEvent e) {
                        resetToOriginal();
                    }
                };
                break;
            }
        }
        
        return defaultActions;
    }
}
