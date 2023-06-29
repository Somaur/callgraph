import vis from "vis-network/standalone/umd/vis-network.min.js";
import options from "./vis-options"; 

window.JavaBridge = {
    goToSource: (referenceHashCode)=>{},
    saveAsHtml: (unused)=>{},
    generateGraph: (unused)=>{}
}

const messageElement = document.getElementById("message");
const networkElement = document.getElementById("network");

const network = new vis.Network(networkElement, {}, options);
network.on("click", function (params) {
    if (params.nodes.length === 1) {
        JavaBridge.goToSource(params.nodes[0]);
    }
    if (params.edges.length === 1) {
        JavaBridge.goToSource(params.edges[0]);
    }
});

network.on("stabilizationProgress", function(params) {
    const message = "Stabilization progress: " + Math.round(params.iterations / params.total * 100) + "%";
    showMessage(message);
});

network.on("stabilizationIterationsDone", function() {
    hideMessage();
    fit();
    showGraphControls();
});


function showMessage(message) {
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
}

function hideGraphControls() {
    for (let generatedGraphController of document.getElementsByClassName("generatedGraphController")) {
        generatedGraphController.classList.add("hidden");
    }
}

showMessage("Click on 'Generate Graph' to generate a call graph.");

function updateNetwork(data) {
    hideGraphControls();
    showMessage("Rendering graph...");
    try {
        options.groups = data.groups;
        network.setOptions(options);
        network.setData(data);
        network.stabilize();
    } catch (e) {
        showMessage(e);
    }
}

function fit() {
    network.fit();
}

window.updateNetwork = updateNetwork;
window.fit = fit;
window.showMessage = showMessage;