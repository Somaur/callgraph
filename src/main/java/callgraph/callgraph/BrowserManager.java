package callgraph.callgraph;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import java.io.IOException;

public class BrowserManager {
    private static BrowserManager instance;

    private final JBCefBrowser browser;

    private BrowserManager() {
        try {
            browser = new JBCefBrowser();
            browser.loadHTML(Utils.getResourceFileAsString("callgraph.html"));
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
        JBCefJSQuery goToSourceQuery = JBCefJSQuery.create((JBCefBrowserBase) browser);
        goToSourceQuery.addHandler(nodeHashCode -> {
            PsiElement element = CallGraphGenerator.getInstance().getReference(Integer.parseInt(nodeHashCode));
            if (element != null) {
                ApplicationManager.getApplication().invokeLater(() -> NavigationUtil.activateFileWithPsiElement(element));
            }
            return null;
        });

        JBCefJSQuery saveAsHtmlQuery = JBCefJSQuery.create((JBCefBrowserBase) browser);
        saveAsHtmlQuery.addHandler(unused -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            ApplicationManager.getApplication().invokeLater(() -> FileChooser.chooseFile(descriptor, project, null, (VirtualFile file) -> {
                try {
                    String saveAsTemplate = Utils.getResourceFileAsString("saveas_template.html");
                    PsiMethod lastGeneratedMethod = CallGraphGenerator.getInstance().getLastGeneratedMethod();
                    String className = lastGeneratedMethod.getContainingClass().getName();
                    String methodName = lastGeneratedMethod.getName();
                    String methodPath = className + "." + methodName;
                    String title = "Call Graph of " + project.getName() + " - " + methodPath;
                    saveAsTemplate = saveAsTemplate.replace("${title}", title);
                    saveAsTemplate = saveAsTemplate.replace("${data}", CallGraphGenerator.getInstance().getJson());
                    Utils.writeToFile(file.getPath() + "/callgraph_" + project.getName() + "_" + className + "_" + methodName + ".html", saveAsTemplate);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            return null;
        });

        browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(browser, frame, httpStatusCode);
                injectQueryHandler("goToSource", goToSourceQuery, "nodeHashCode");
                injectQueryHandler("saveAsHtml", saveAsHtmlQuery, "unused");
            }
        }, browser.getCefBrowser());
    }

    private void injectQueryHandler(String handlerName, JBCefJSQuery handler, String argName) {
        executeJavaScript("window.JavaBridge." + handlerName + " = function(" + argName + ") {" +
                handler.inject(argName) +
                "}");
    }
}
