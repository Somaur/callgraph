package callgraph.callgraph.browser.handlers;

import callgraph.callgraph.CallGraphGenerator;
import callgraph.callgraph.browser.JSQueryHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.jetbrains.annotations.NotNull;

public class GoToSourceHandler extends JSQueryHandler {
    public GoToSourceHandler(JBCefBrowserBase browser) {
        super(browser);
        jsQuery.addHandler(nodeHashCode -> {
            PsiElement element = CallGraphGenerator.getInstance().getReference(Integer.parseInt(nodeHashCode));
            if (element != null) {
                ApplicationManager.getApplication().invokeLater(() -> NavigationUtil.activateFileWithPsiElement(element));
            }
            return null;
        });
    }

    @Override
    @NotNull
    public String getHandlerName() {
        return "goToSource";
    }

    @Override
    @NotNull
    public String getArgName() {
        return "nodeHashCode";
    }
}
