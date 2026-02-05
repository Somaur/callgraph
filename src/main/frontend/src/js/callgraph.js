import vis from "vis-network/standalone/umd/vis-network.min.js";
import options from "./vis-options";
import "@fortawesome/fontawesome-free/css/all.min.css";

window.JavaBridge = {
    goToSource: (referenceHashCode) => {
    },
    saveAsHtml: (unused) => {
    },
    generateGraph: (unused) => {
    },
    openSettings: (unused) => {
    }
}

const messageElement = document.getElementById("message");
const networkElement = document.getElementById("network");
const generateMessage = document.getElementById("generateMessage");
const showAllButton = document.getElementById("showAllButton");
const depthWarningElement = document.getElementById("depthWarning");

const network = new vis.Network(networkElement, {}, options);
let hiddenNodes = new Set();
let selectedNodeId = null;
let isGraphFitted = false;
let isGraphGenerated = false;

network.on("click", function (params) {
    if (params.nodes.length === 1) {
        selectedNodeId = params.nodes[0];
        JavaBridge.goToSource(params.nodes[0]);
    } else {
        selectedNodeId = null;
    }
    if (params.edges.length === 1) {
        JavaBridge.goToSource(params.edges[0]);
    }
    updateButtonVisibility();
    hideContextMenu();
});

network.on("oncontext", function (params) {
    params.event.preventDefault();
    const nodeId = network.getNodeAt(params.pointer.DOM);
    if (nodeId !== undefined) {
        selectedNodeId = nodeId;
        showContextMenu(params.pointer.DOM.x, params.pointer.DOM.y, nodeId);
    } else {
        hideContextMenu();
    }
});

network.on("stabilizationProgress", function (params) {
    const message = "Stabilization progress: " + Math.round(params.iterations / params.total * 100) + "%";
    showMessage(message);
});

network.on("stabilizationIterationsDone", function () {
    hideMessage();
    isGraphGenerated = true;
    fit();
    showGraphControls();
});

network.on("fit", () => {
    isGraphFitted = true;
    updateButtonVisibility();
});

network.on("dragEnd", () => {
    isGraphFitted = false;
    updateButtonVisibility();
});

network.on("zoom", () => {
    isGraphFitted = false;
    updateButtonVisibility();
});

function hideSelectedNode() {
    if (selectedNodeId !== null) {
        hiddenNodes.add(selectedNodeId);
        network.body.data.nodes.update({id: selectedNodeId, hidden: true});
        selectedNodeId = null;
        updateShowAllButton();
        updateButtonVisibility();
        hideContextMenu();
    }
}

function clearGraph() {
    network.setData({nodes: [], edges: []});
    hiddenNodes.clear();
    selectedNodeId = null;
    isGraphFitted = false;
    isGraphGenerated = false;
    hideGraphControls();
    updateShowAllButton();
    hideContextMenu();
    hideDepthWarning();
    resetDefaultMessages();
}

function createContextMenu() {
    const menu = document.createElement('div');
    menu.id = 'contextMenu';
    menu.style.cssText = `
        position: absolute;
        display: none;
        background: #333;
        border: 1px solid #555;
        border-radius: 4px;
        padding: 4px 0;
        z-index: 1000;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    `;
    
    const menuItemStyle = `
        padding: 8px 16px;
        color: #DDD;
        cursor: pointer;
        font-size: 14px;
        white-space: nowrap;
    `;
    
    const hideItem = document.createElement('div');
    hideItem.id = 'contextMenuHide';
    hideItem.innerHTML = '<i class="fas fa-eye-slash" style="margin-right: 8px;"></i>Hide Node';
    hideItem.style.cssText = menuItemStyle;
    hideItem.onmouseover = () => hideItem.style.background = '#555';
    hideItem.onmouseout = () => hideItem.style.background = 'transparent';
    hideItem.onclick = () => {
        hideSelectedNode();
        hideContextMenu();
    };
    
    const hideWithCallersItem = document.createElement('div');
    hideWithCallersItem.id = 'contextMenuHideWithCallers';
    hideWithCallersItem.innerHTML = '<i class="fas fa-project-diagram" style="margin-right: 8px;"></i>Hide Node & All Callers';
    hideWithCallersItem.style.cssText = menuItemStyle;
    hideWithCallersItem.onmouseover = () => hideWithCallersItem.style.background = '#555';
    hideWithCallersItem.onmouseout = () => hideWithCallersItem.style.background = 'transparent';
    hideWithCallersItem.onclick = () => {
        hideNodeWithCallers(selectedNodeId);
        hideContextMenu();
    };
    
    const goToSourceItem = document.createElement('div');
    goToSourceItem.id = 'contextMenuGoToSource';
    goToSourceItem.innerHTML = '<i class="fas fa-code" style="margin-right: 8px;"></i>Go to Source';
    goToSourceItem.style.cssText = menuItemStyle;
    goToSourceItem.onmouseover = () => goToSourceItem.style.background = '#555';
    goToSourceItem.onmouseout = () => goToSourceItem.style.background = 'transparent';
    goToSourceItem.onclick = () => {
        if (selectedNodeId !== null) {
            JavaBridge.goToSource(selectedNodeId);
        }
        hideContextMenu();
    };
    
    menu.appendChild(hideItem);
    menu.appendChild(hideWithCallersItem);
    menu.appendChild(goToSourceItem);
    document.body.appendChild(menu);
    return menu;
}

function hideNodeWithCallers(nodeId) {
    if (nodeId === null) return;
    
    const nodesToHide = new Set();
    collectCallersRecursively(nodeId, nodesToHide);
    
    nodesToHide.forEach(id => {
        hiddenNodes.add(id);
        network.body.data.nodes.update({id: id, hidden: true});
    });
    
    selectedNodeId = null;
    updateShowAllButton();
    updateButtonVisibility();
}

function collectCallersRecursively(nodeId, collected) {
    if (collected.has(nodeId)) return;
    collected.add(nodeId);
    
    const edges = network.body.data.edges.get();
    edges.forEach(edge => {
        if (edge.to === nodeId) {
            collectCallersRecursively(edge.from, collected);
        }
    });
}

const contextMenu = createContextMenu();

function showContextMenu(x, y, nodeId) {
    contextMenu.style.left = x + 'px';
    contextMenu.style.top = y + 'px';
    contextMenu.style.display = 'block';
}

function hideContextMenu() {
    contextMenu.style.display = 'none';
}

document.addEventListener('click', function(event) {
    if (!contextMenu.contains(event.target)) {
        hideContextMenu();
    }
});

function showDepthWarning(maxDepth) {
    depthWarningElement.innerHTML = '<i class="fas fa-exclamation-triangle"></i>Maximum depth (' + maxDepth + ') reached. Some callers may not be shown.';
    depthWarningElement.classList.remove("hidden");
}

function hideDepthWarning() {
    depthWarningElement.classList.add("hidden");
}

function showAllNodes() {
    if (hiddenNodes.size > 0) {
        const hiddenNodesArray = Array.from(hiddenNodes);
        hiddenNodesArray.forEach(nodeId => {
            network.body.data.nodes.update({id: nodeId, hidden: false});
        });
        hiddenNodes.clear();
        updateShowAllButton();
    }
}

function updateShowAllButton() {
    if (hiddenNodes.size > 0) {
        showAllButton.classList.remove("hidden");
    } else {
        showAllButton.classList.add("hidden");
    }
}

function updateButtonVisibility() {
    const fitButton = document.querySelector('button[onclick="fit()"]');
    const hideNodeButton = document.querySelector('button[onclick="hideSelectedNode()"]');
    
    if (fitButton) {
        if (!isGraphGenerated || isGraphFitted) {
            fitButton.classList.add("hidden");
        } else {
            fitButton.classList.remove("hidden");
        }
    }
    
    if (hideNodeButton) {
        if (selectedNodeId !== null) {
            hideNodeButton.classList.remove("hidden");
        } else {
            hideNodeButton.classList.add("hidden");
        }
    }
}

function showMessage(message) {
    if (!message || message.trim() === '') {
        message = "Place your caret on a method, right-click, or use Alt+Shift+E shortcut to generate a call graph.";
    }
    messageElement.innerHTML = message;
    messageElement.classList.remove("hidden");
    networkElement.classList.add("hidden");
}

function hideMessage() {
    messageElement.classList.add("hidden");
    networkElement.classList.remove("hidden");
}

function showGraphControls() {
    for (let generatedGraphController of document.getElementsByClassName("generatedGraphController")) {
        generatedGraphController.classList.remove("hidden");
    }
    updateShowAllButton();
    updateButtonVisibility();
}

function hideGraphControls() {
    for (let generatedGraphController of document.getElementsByClassName("generatedGraphController")) {
        generatedGraphController.classList.add("hidden");
    }
}

function updateNetwork(data) {
    hideGraphControls();
    hiddenNodes.clear();
    selectedNodeId = null;
    isGraphFitted = false;
    isGraphGenerated = false;
    updateShowAllButton();
    hideDepthWarning();
    showMessage("Rendering graph...");
    try {
        options.groups = data.groups;
        network.setOptions(options);
        
        // Mark truncated nodes with special style
        if (data.truncatedNodes && data.truncatedNodes.length > 0) {
            const truncatedSet = new Set(data.truncatedNodes);
            data.nodes.forEach(node => {
                if (truncatedSet.has(node.id)) {
                    node.label = node.label + '\n⚠ [Limited]';
                    node.borderWidth = 3;
                    node.shapeProperties = { borderDashes: [5, 5] };
                }
            });
        }
        
        network.setData(data);
        network.stabilize();
        
        if (data.depthLimitReached) {
            showDepthWarning(data.maxDepth);
        }
    } catch (e) {
        showMessage(e);
    }
}

function fit() {
    network.fit();
    isGraphFitted = true;
    updateButtonVisibility();
}

const MESSAGE_TYPE_SUCCESS = "+";
const MESSAGE_TYPE_ERROR = "-";

function setGenerateMessage(message) {
    if (!message || message.trim() === '') {
        message = "-PLACE YOUR CARET ON A METHOD";
    }
    
    let messageTypeFlag = message.substring(0, 1);
    let classToSet = "navbuttonMessage "
    if (messageTypeFlag === MESSAGE_TYPE_SUCCESS) {
        classToSet += "navbuttonMessage-success";
    } else if (messageTypeFlag === MESSAGE_TYPE_ERROR) {
        classToSet += "navbuttonMessage-error";
    }
    generateMessage.className = classToSet;
    generateMessage.innerHTML = message.substring(1);
}

function updateMessageTextColor(backgroundColor) {
    backgroundColor = backgroundColor.replace('#', '');
    
    const r = parseInt(backgroundColor.substr(0, 2), 16);
    const g = parseInt(backgroundColor.substr(2, 2), 16);
    const b = parseInt(backgroundColor.substr(4, 2), 16);
    
    const brightness = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    
    if (brightness > 0.5) {
        messageElement.style.color = "black";
    } else {
        messageElement.style.color = "white";
    }
}

function resetDefaultMessages() {
    showMessage();
    setGenerateMessage();
}

window.updateNetwork = updateNetwork;
window.fit = fit;
window.showMessage = showMessage;
window.setGenerateMessage = setGenerateMessage;
window.updateMessageTextColor = updateMessageTextColor;
window.resetDefaultMessages = resetDefaultMessages;
window.hideSelectedNode = hideSelectedNode;
window.showAllNodes = showAllNodes;
window.clearGraph = clearGraph;
window.showDepthWarning = showDepthWarning;
window.hideDepthWarning = hideDepthWarning;

resetDefaultMessages();
updateButtonVisibility();