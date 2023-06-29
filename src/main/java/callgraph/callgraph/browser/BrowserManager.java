package callgraph.callgraph.browser;

import callgraph.callgraph.Utils;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import java.io.IOException;
import java.util.List;

public class BrowserManager {
    private static BrowserManager instance;

    private final JBCefBrowser browser;

    private BrowserManager() {
        try {
            browser = new JBCefBrowser();
            browser.loadHTML(Utils.getResourceFileAsString("build/callgraph.html"));
            createJavaScriptBridge();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BrowserManager getInstance() {
        if (instance == null) {
            instance = new BrowserManager();
        }
        return instance;
    }

    public JBCefBrowser getJBCefBrowser() {
        return browser;
    }

    public void executeJavaScript(String script) {
        browser.getCefBrowser().executeJavaScript(script, browser.getCefBrowser().getURL(), 0);
    }

    public void showMessage(String message) {
        executeJavaScript("showMessage('" + message + "')");
    }

    public void updateNetwork(String json) {
        executeJavaScript("updateNetwork(" + json + ")");
    }

    private void createJavaScriptBridge() {
        browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(browser, frame, httpStatusCode);
                List<JSQueryHandler> handlers = new HandlerFactory().getHandlers((JBCefBrowserBase) browser);
                for (JSQueryHandler handler : handlers) {
                    injectQueryHandler(handler.getHandlerName(), handler.getHandler(), handler.getArgName());
                }
            }
        }, browser.getCefBrowser());
    }

    private void injectQueryHandler(String handlerName, JBCefJSQuery handler, String argName) {
        executeJavaScript("window.JavaBridge." + handlerName + " = function(" + argName + ") {" +
                handler.inject(argName) +
                "}");
    }
}
