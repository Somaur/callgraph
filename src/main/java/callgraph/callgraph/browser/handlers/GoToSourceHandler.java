package callgraph.callgraph.browser.handlers;

import callgraph.callgraph.CallGraphGenerator;
import callgraph.callgraph.browser.JSQueryHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GoToSourceHandler extends JSQueryHandler {
    public GoToSourceHandler(JBCefBrowserBase browser) {
        super(browser);
    }

    @Override
    @NotNull
    public Function<? super String, ? extends JBCefJSQuery.Response> getHandler() {
        return nodeHashCode -> {
            PsiElement element = CallGraphGenerator.getInstance().getReference(Integer.parseInt(nodeHashCode));
            if (element != null) {
                ApplicationManager.getApplication().invokeLater(() -> NavigationUtil.activateFileWithPsiElement(element));
            }
            return null;
        };
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
