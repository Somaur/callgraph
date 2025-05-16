package callgraph.callgraph;

import callgraph.callgraph.browser.BrowserManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class CallGraphToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BrowserManager browserManager = BrowserManager.getInstance(project);
        JBCefBrowser browser = browserManager.getJBCefBrowser();

        // Add the browser component to the tool window
        JComponent component = browser.getComponent();
        Content content = toolWindow.getContentManager().getFactory().createContent(component, null, false);
        toolWindow.getContentManager().addContent(content);
        
        // Wait for browser to be ready before adding listeners
        browserManager.whenBrowserReady(() -> {
            // Set up caret listener once browser is initialized
            EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
            eventMulticaster.addCaretListener(new CallGraphCaretListener(project, toolWindow));
        });
    }
}
