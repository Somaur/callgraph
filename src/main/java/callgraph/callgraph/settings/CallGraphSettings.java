package callgraph.callgraph.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service for storing and retrieving user settings for Call Graph plugin.
 * Settings are stored per-project.
 */
@Service(Service.Level.PROJECT)
@State(
        name = "CallGraphSettings",
        storages = {@Storage("callgraph-settings.xml")}
)
public class CallGraphSettings implements PersistentStateComponent<CallGraphSettings> {
    public static final String BACKGROUND_TYPE_CUSTOM = "custom";
    public static final String BACKGROUND_TYPE_IDE = "ide";
    
    public static final int DEFAULT_MAX_DEPTH = 10;
    public static final int MIN_MAX_DEPTH = 1;
    public static final int MAX_MAX_DEPTH = 100;
    
    // Default settings
    private String backgroundType = BACKGROUND_TYPE_CUSTOM;
    private String customBackgroundColor = "#000000"; // Default to black
    private int maxDepth = DEFAULT_MAX_DEPTH; // Maximum recursion depth for call graph generation

    public static CallGraphSettings getInstance(Project project) {
        return project.getService(CallGraphSettings.class);
    }

    @Nullable
    @Override
    public CallGraphSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CallGraphSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getBackgroundType() {
        return backgroundType;
    }

    public void setBackgroundType(String backgroundType) {
        this.backgroundType = backgroundType;
    }

    public String getCustomBackgroundColor() {
        return customBackgroundColor;
    }

    public void setCustomBackgroundColor(String customBackgroundColor) {
        this.customBackgroundColor = customBackgroundColor;
    }

    /**
     * Get the effective background color based on current settings.
     * @param ideEditorBackgroundColor The IDE editor background color
     * @return The background color to use
     */
    public String getEffectiveBackgroundColor(String ideEditorBackgroundColor) {
        if (BACKGROUND_TYPE_IDE.equals(backgroundType)) {
            return ideEditorBackgroundColor;
        }
        return customBackgroundColor;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        // Clamp value to valid range
        if (maxDepth < MIN_MAX_DEPTH) {
            this.maxDepth = MIN_MAX_DEPTH;
        } else if (maxDepth > MAX_MAX_DEPTH) {
            this.maxDepth = MAX_MAX_DEPTH;
        } else {
            this.maxDepth = maxDepth;
        }
    }
}
