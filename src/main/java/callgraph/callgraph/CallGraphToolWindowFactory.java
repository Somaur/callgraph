package callgraph.callgraph;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public final class CallGraphToolWindowFactory implements ToolWindowFactory {
    private CallGraphGenerator generator;
    private CallGraphGenerateAction generateAction;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BrowserManager browserManager = BrowserManager.getInstance();
        JBCefBrowser browser = browserManager.getJBCefBrowser();

        JBCefJSQuery goToSourceQuery = JBCefJSQuery.create((JBCefBrowserBase) browser);
        goToSourceQuery.addHandler(nodeHashCode -> {
            PsiElement element = generator.getReference(Integer.parseInt(nodeHashCode));
            if (element != null) {
                ApplicationManager.getApplication().invokeLater(() -> NavigationUtil.activateFileWithPsiElement(element));
            }
            return null;
        });

        browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(browser, frame, httpStatusCode);
                browserManager.executeJavaScript("window.JavaBridge = {" +
                            "goToSource : function(nodeHashCode) {" +
                                goToSourceQuery.inject("nodeHashCode") +
                            "}" +
                        "};");
            }
        }, browser.getCefBrowser());

        JComponent component = browser.getComponent();
        Content content = toolWindow.getContentManager().getFactory().createContent(component, null, false);
        toolWindow.getContentManager().addContent(content);

        generator = new CallGraphGenerator();
        generateAction = new CallGraphGenerateAction(project, browserManager, generator);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(generateAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }
}
