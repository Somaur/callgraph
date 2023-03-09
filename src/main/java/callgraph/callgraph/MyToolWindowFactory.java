package callgraph.callgraph;

import com.intellij.lang.jvm.JvmNamedElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.concurrency.SwingWorker;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MyToolWindowFactory implements ToolWindowFactory {

    private mxGraphComponent graphComponent;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create the graph component
        mxGraph graph = new mxGraph();
        graph.setCellsEditable(false);
        graph.setAutoSizeCells(true);
        graphComponent = new mxGraphComponent(graph);
        graphComponent.setGridVisible(true);
        graphComponent.setConnectable(false);
        graphComponent.setDragEnabled(false);
        graphComponent.setPanning(true);

        // Add the graph component to the tool window content pane
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(graphComponent, "", true);
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

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Call Graph") {
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        graph.getModel().beginUpdate();
                        graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
                        ApplicationManager.getApplication().runReadAction(() -> {
                            addCallersToGraph(method, null, graph);
                        });
                        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);

                        layout.execute(graph.getDefaultParent());
                        graph.getModel().endUpdate();
                        graphComponent.refresh();
                        graphComponent.zoomTo(1, true);
                    }
                });
            }
        };

        // Add the action to the tool window toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(updateGraphAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }

    private void addCallersToGraph(PsiMethod method, mxCell methodVertex, mxGraph graph) {
        // Add the current method to the graph
        Object parent = graph.getDefaultParent();

        if (Objects.isNull(methodVertex)) {
            methodVertex = (mxCell) graph.insertVertex(parent, null, method.getContainingClass().getName() + "\n" + method.getName(), 50, 50, 200, 35, "fillColor=#8395a7;fontColor=white;strokeColor=#8395a7");
        }

        // Add the callers of the current method to the graph
        Collection<PsiReference> allReferences = MethodReferencesSearch.search(method).findAll();

        if (allReferences.isEmpty()) {
            PsiMethod[] superMethods = method.findSuperMethods();
            if (superMethods.length > 0) {
                PsiMethod superMethod = superMethods[0];
                if (PsiManager.getInstance(method.getProject()).isInProject(superMethod)) {
                    addCallersToGraph(superMethod, methodVertex, graph);
                    return;
                }
            }
        }

        for (PsiReference reference : allReferences) {
            PsiElement element = reference.getElement();
            if (element instanceof PsiReferenceExpressionImpl) {
                PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
                if (caller != null) {
                    try {
                        String color = "#8395a7";
                        String callerNameLowercase = caller.getContainingClass().getName().toLowerCase();
                        if (callerNameLowercase.contains("controller")) {
                            color = "#EF476F";
                        } else if (callerNameLowercase.contains("service")) {
                            color = "#4DAA57";
                        } else if (callerNameLowercase.contains("facade")) {
                            color = "#ff9f43";
                        } else if (callerNameLowercase.contains("dao")) {
                            color = "#5f27cd";
                        }

                        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
                        int lineNumber = documentManager.getDocument((element).getContainingFile()).getLineNumber(((PsiReferenceExpressionImpl) element).getStartOffset());

                        String label = caller.getContainingClass().getName() + ":" + lineNumber + "\n" + caller.getName();
                        mxCell callerVertex = (mxCell) graph.insertVertex(parent, null, label, 0, 0, 200, 35, "fillColor=" + color + ";fontColor=white;strokeColor="+color+";autosize=1;");

                        //String parameters = Arrays.stream(method.getParameters()).map(JvmNamedElement::getName).collect(Collectors.joining(", "));

                        graph.insertEdge(parent, null, "", callerVertex, methodVertex, "strokeColor=" + color + ";fontColor=white");
                        addCallersToGraph(caller, callerVertex, graph);
                    } catch (Exception e) {

                    }
                }
            }
        }
    }
}
