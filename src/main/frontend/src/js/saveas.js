import vis from "vis-network/standalone/umd/vis-network.min.js";
import options from "./vis-options";

const networkElement = document.getElementById("network");

const network = new vis.Network(networkElement, {}, options);

function updateNetwork(data) {
    options.groups = data.groups;
    network.setOptions(options);
    network.setData(data);
    network.stabilize();
}

window.updateNetwork = updateNetwork;