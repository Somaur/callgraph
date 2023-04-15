package callgraph.callgraph;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;

public class CallGraphGenerator {
    private PsiMethod mainMethod;

    public CallGraphGenerator(PsiMethod mainMethod) {
        this.mainMethod = mainMethod;
    }

    public String generate() {
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        JSONObject mainNode = new JSONObject();
        mainNode.put("id", mainMethod.hashCode());
        mainNode.put("label", mainMethod.getName());
        mainNode.put("group", mainMethod.getContainingClass().getName());
        mainNode.put("title", mainMethod.getContainingClass().getQualifiedName());

        nodes.add(mainNode);

        findAndAddCallers(mainMethod, nodes, edges);

        JSONObject graph = new JSONObject();
        graph.put("nodes", nodes);
        graph.put("edges", edges);

        return graph.toJSONString();
    }

    private void findAndAddCallers(PsiMethod method, JSONArray nodes, JSONArray edges) {
        Collection<PsiReference> allReferences = MethodReferencesSearch.search(method).findAll();
        for (PsiReference reference : allReferences) {
            PsiElement element = reference.getElement();
            PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (caller == null) continue;
            if (caller.equals(method)) continue;
            if (!caller.getProject().equals(method.getProject())) continue;

            if (nodes.stream().noneMatch(node -> ((JSONObject) node).get("id").equals(caller.hashCode()))) {
                JSONObject callerNode = new JSONObject();
                callerNode.put("id", caller.hashCode());
                callerNode.put("label", caller.getContainingClass().getName() + "\n" + caller.getName());
                callerNode.put("group", caller.getContainingClass().getName());
                callerNode.put("title", caller.getContainingClass().getQualifiedName());
                nodes.add(callerNode);
            }

            JSONObject edge = new JSONObject();
            edge.put("from", caller.hashCode());
            edge.put("to", method.hashCode());

            edges.add(edge);

            findAndAddCallers(caller, nodes, edges);
        }
    }
}
