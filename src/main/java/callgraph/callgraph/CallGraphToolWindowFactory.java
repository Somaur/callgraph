package callgraph.callgraph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public final class CallGraphToolWindowFactory implements ToolWindowFactory {
    private CallGraphGenerateAction generateAction;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BrowserManager browserManager = BrowserManager.getInstance();
        JBCefBrowser browser = browserManager.getJBCefBrowser();

        JComponent component = browser.getComponent();
        Content content = toolWindow.getContentManager().getFactory().createContent(component, null, false);
        toolWindow.getContentManager().addContent(content);

        generateAction = new CallGraphGenerateAction(browserManager, CallGraphGenerator.getInstance());

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(generateAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }
}
