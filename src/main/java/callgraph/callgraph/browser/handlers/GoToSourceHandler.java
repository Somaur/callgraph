package callgraph.callgraph.browser.handlers;

import callgraph.callgraph.CallGraphGenerator;
import callgraph.callgraph.browser.JSQueryHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;

public class GoToSourceHandler extends JSQueryHandler {
    public GoToSourceHandler(JBCefBrowserBase browser) {
        super(browser);
    }

    @Override
    public JBCefJSQuery getHandler() {
        JBCefJSQuery goToSourceQuery = JBCefJSQuery.create(browser);
        goToSourceQuery.addHandler(nodeHashCode -> {
            PsiElement element = CallGraphGenerator.getInstance().getReference(Integer.parseInt(nodeHashCode));
            if (element != null) {
                ApplicationManager.getApplication().invokeLater(() -> NavigationUtil.activateFileWithPsiElement(element));
            }
            return null;
        });
        return goToSourceQuery;
    }

    @Override
    public String getHandlerName() {
        return "goToSource";
    }

    @Override
    public String getArgName() {
        return "nodeHashCode";
    }
}
