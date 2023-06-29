package callgraph.callgraph.browser;

import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;

public abstract class JSQueryHandler {
    protected JBCefBrowserBase browser;

    protected JSQueryHandler(JBCefBrowserBase browser) {
        this.browser = browser;
    }

    public abstract JBCefJSQuery getHandler();

    public abstract String getHandlerName();

    public abstract String getArgName();
}
