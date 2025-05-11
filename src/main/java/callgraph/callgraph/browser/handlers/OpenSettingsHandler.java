package callgraph.callgraph.browser.handlers;

import callgraph.callgraph.browser.JSQueryHandler;
import callgraph.callgraph.settings.CallGraphSettingsDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Handler for opening the settings dialog from the UI.
 */
public class OpenSettingsHandler extends JSQueryHandler {
    public OpenSettingsHandler(JBCefBrowserBase browser, Project project) {
        super(browser, project);
    }

    @Override
    @NotNull
    public Function<? super String, ? extends JBCefJSQuery.Response> getHandler() {
        return unused -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                CallGraphSettingsDialog dialog = new CallGraphSettingsDialog(project);
                // Just show the dialog - changes are applied in real-time
                // The dialog's doOKAction will save the final state
                dialog.showAndGet();
                // No need for additional update as changes are already applied
            });
            return null;
        };
    }

    @Override
    @NotNull
    public String getHandlerName() {
        return "openSettings";
    }

    @Override
    @NotNull
    public String getArgName() {
        return "unused";
    }
}
