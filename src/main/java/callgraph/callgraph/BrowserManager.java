package callgraph.callgraph;

import com.intellij.ui.jcef.JBCefBrowser;

public class BrowserManager {
    private static BrowserManager instance;

    private final JBCefBrowser browser;

    private BrowserManager() {
        browser = new JBCefBrowser();
        browser.loadHTML(loadHtmlFile());
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

    private String loadHtmlFile() {
        try {
            return Utils.getResourceFileAsString("callgraph.html");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
