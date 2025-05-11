package callgraph.callgraph.browser;

import callgraph.callgraph.Utils;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import java.io.IOException;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class BrowserManager {
    private final Project project;
    private final JBCefBrowser browser;

    public BrowserManager(Project project) {
        this.project = project;
        try {
            browser = new JBCefBrowser();
            browser.loadHTML(Utils.getResourceFileAsString("build/callgraph.html"));
            createJavaScriptBridge();
        } catch (IOException e) {
            // TODO: handle this
            throw new RuntimeException(e);
        }
    }

    public static BrowserManager getInstance(Project project) {
        return project.getService(BrowserManager.class);
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

    public void setGenerateMessage(String message) {
        executeJavaScript("setGenerateMessage('" + message + "')");
    }

    private void createJavaScriptBridge() {
        List<JSQueryHandler> handlers = new HandlerFactory().getHandlers(browser, project);
        browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser loadedBrowser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(loadedBrowser, frame, httpStatusCode);
                for (JSQueryHandler handler : handlers) {
                    injectQueryHandler(handler.getHandlerName(), handler.getJsQuery(), handler.getArgName());
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
