package callgraph.callgraph.browser.handlers;

import callgraph.callgraph.CallGraphGenerator;
import callgraph.callgraph.Utils;
import callgraph.callgraph.browser.JSQueryHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SaveAsHtmlHandler extends JSQueryHandler {
    public SaveAsHtmlHandler(JBCefBrowserBase browser) {
        super(browser);
        jsQuery.addHandler(unused -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            ApplicationManager.getApplication().invokeLater(() -> FileChooser.chooseFile(descriptor, project, null, (VirtualFile file) -> {
                try {
                    String saveAsTemplate = Utils.getResourceFileAsString("build/saveas.html");
                    PsiMethod lastGeneratedMethod = CallGraphGenerator.getInstance().getLastGeneratedMethod();
                    String className = lastGeneratedMethod.getContainingClass().getName();
                    String methodName = lastGeneratedMethod.getName();
                    String methodPath = className + "." + methodName;
                    String title = "Call Graph of " + project.getName() + " - " + methodPath;
                    saveAsTemplate = saveAsTemplate.replace("${title}", title);
                    saveAsTemplate += "<script>updateNetwork(" + CallGraphGenerator.getInstance().getJson() + ")</script>";
                    Utils.writeToFile(file.getPath() + "/callgraph_" + project.getName() + "_" + className + "_" + methodName + ".html", saveAsTemplate);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            return null;
        });
    }

    @Override
    @NotNull
    public String getHandlerName() {
        return "saveAsHtml";
    }

    @Override
    @NotNull
    public String getArgName() {
        return "unused";
    }
}
