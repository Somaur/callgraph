package callgraph.callgraph;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.JavaFindUsagesHandler;
import com.intellij.find.findUsages.JavaFindUsagesOptions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class MyToolWindowFactory implements ToolWindowFactory {

    private mxGraphComponent graphComponent;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create the graph component
        mxGraph graph = new mxGraph();
        graph.setAutoSizeCells(true);

        mxStylesheet stylesheet = new mxStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_AUTOSIZE, mxConstants.ALIGN_CENTER);
        style.put(mxConstants.STYLE_FONTSIZE, 12);
        style.put(mxConstants.STYLE_FONTFAMILY, "Arial");
        style.put(mxConstants.STYLE_SPACING, 10);
        stylesheet.putCellStyle("vertexStyle", style);
        graph.setStylesheet(stylesheet);

        graphComponent = new mxGraphComponent(graph);

        // Add the graph component to the tool window content pane
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(graphComponent, "", false);
        toolWindow.getContentManager().addContent(content);

        // Add an action to update the graph
        AnAction updateGraphAction = new AnAction("Update Graph") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                // Get the current method
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                int offset = editor.getCaretModel().getOffset();
                PsiElement element = psiFile.findElementAt(offset);
                PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

                // Update the graph with the callers of the current method
                graph.getModel().beginUpdate();
                try {
                    graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
                    addCallersToGraph(method, null, graph);
                } finally {
                    mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
                    layout.execute(graph.getDefaultParent());
                    graph.getModel().endUpdate();
                    graphComponent.refresh();
                }
            }
        };

        // Add the action to the tool window toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(updateGraphAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }

    private void addCallersToGraph(PsiMethod method, Object methodVertex, mxGraph graph) {
        // Add the current method to the graph
        Object parent = graph.getDefaultParent();

        if (Objects.isNull(methodVertex)) {
            methodVertex = graph.insertVertex(parent, null, method.getName(), 50, 50, 80, 30);
        }

        // Add the callers of the current method to the graph
        for (PsiReference reference : ReferencesSearch.search(method)) {
            PsiElement element = reference.getElement();
            if (element instanceof PsiReferenceExpression) {
                PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
                if (caller != null) {
                    Object callerVertex = graph.insertVertex(parent, null,  caller.getContainingClass().getName() + "\n" + caller.getName(), 0, 0, 200, 35);

                    graph.insertEdge(parent, null, "", callerVertex, methodVertex);
                    addCallersToGraph(caller, callerVertex, graph);
                }
            }
        }
    }
}
