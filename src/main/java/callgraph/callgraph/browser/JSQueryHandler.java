package callgraph.callgraph.browser;

import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

public abstract class JSQueryHandler {
    protected JBCefJSQuery jsQuery;

    protected JSQueryHandler(JBCefBrowserBase browser) {
        this.jsQuery = JBCefJSQuery.create(browser);
    }

    public JBCefJSQuery getJsQuery() {
        return jsQuery;
    }

    @NotNull
    public abstract String getHandlerName();

    @NotNull
    public abstract String getArgName();
}
