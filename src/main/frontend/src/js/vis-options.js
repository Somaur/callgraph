const options = {
    nodes: {
        shape: "box",
        font: {
            multi: "md"
        }
    },
    edges: {
        arrows: {
            to: {
                enabled: true
            },
        },
        font: {
            strokeWidth: 5,
            size: 12,
            align: "middle"
        },
    },
    interaction: {
        zoomSpeed: 0.25,
        hover: true
    },
    physics: {
        enabled: false,
        barnesHut: {
            theta: 0.5,
            gravitationalConstant: -3000,
            springLength: 120,
            springConstant: 0.04,
            damping: 0.09,
            avoidOverlap: 0.5
        },
        solver: 'barnesHut'
    },
    layout: {
        randomSeed: 0
    }
};

module.exports = options;