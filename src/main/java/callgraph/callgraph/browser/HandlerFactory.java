package callgraph.callgraph.browser;

import callgraph.callgraph.browser.handlers.GenerateGraphHandler;
import callgraph.callgraph.browser.handlers.GoToSourceHandler;
import callgraph.callgraph.browser.handlers.SaveAsHtmlHandler;
import com.intellij.ui.jcef.JBCefBrowserBase;

import java.util.ArrayList;
import java.util.List;

public class HandlerFactory {
    public List<JSQueryHandler> getHandlers(JBCefBrowserBase browser) {
        List<JSQueryHandler> handlers = new ArrayList<>();
        handlers.add(new GenerateGraphHandler(browser));
        handlers.add(new GoToSourceHandler(browser));
        handlers.add(new SaveAsHtmlHandler(browser));
        return handlers;
    }
}
