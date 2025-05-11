import vis from "vis-network/standalone/umd/vis-network.min.js";
import options from "./vis-options";

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

const network = new vis.Network(networkElement, {}, options);
network.on("click", function (params) {
    if (params.nodes.length === 1) {
        JavaBridge.goToSource(params.nodes[0]);
    }
    if (params.edges.length === 1) {
        JavaBridge.goToSource(params.edges[0]);
    }
});

network.on("stabilizationProgress", function (params) {
    const message = "Stabilization progress: " + Math.round(params.iterations / params.total * 100) + "%";
    showMessage(message);
});

network.on("stabilizationIterationsDone", function () {
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

const MESSAGE_TYPE_SUCCESS = "+";
const MESSAGE_TYPE_ERROR = "-";

/**
 * Sets the message by the GENERATE button.
 * @param {string} message
 */
function setGenerateMessage(message) {
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

/**
 * Determines if a color is light or dark and updates the centeredMessage text color accordingly
 * @param {string} backgroundColor - Color in hex format (#RRGGBB)
 */
function updateMessageTextColor(backgroundColor) {
    // Remove the # if it exists
    backgroundColor = backgroundColor.replace('#', '');
    
    // Convert hex to RGB
    const r = parseInt(backgroundColor.substr(0, 2), 16);
    const g = parseInt(backgroundColor.substr(2, 2), 16);
    const b = parseInt(backgroundColor.substr(4, 2), 16);
    
    // Calculate brightness (using relative luminance formula)
    // https://www.w3.org/TR/WCAG20/#relativeluminancedef
    const brightness = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    
    // If brightness > 0.5, it's a light color, otherwise dark
    if (brightness > 0.5) {
        messageElement.style.color = "black";
    } else {
        messageElement.style.color = "white";
    }
}

window.updateNetwork = updateNetwork;
window.fit = fit;
window.showMessage = showMessage;
window.setGenerateMessage = setGenerateMessage;
window.updateMessageTextColor = updateMessageTextColor;

showMessage("Place your caret on a method and click on GENERATE to generate a call graph.");
setGenerateMessage("-PLACE YOUR CARET ON A METHOD")