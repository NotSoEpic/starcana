var canvas = document.getElementById("chart"),
    ctx = canvas.getContext("2d"),
    cLeft = canvas.offsetLeft + canvas.clientLeft,
    cTop = canvas.offsetTop + canvas.clientTop,
    w = canvas.width,
    h = canvas.height;

var stars = [],
    mode = "add",
    lines = [],
    target = null;

function dist(a, b) {
    return Math.sqrt((a.x - b.x) ** 2 + (a.y - b.y) ** 2);
}

// https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
function lsDist(a, b, p, c) {
    c = c ? c : 0;
    var l = dist(a, b);
    if (l == 0) return dist(a, p);
    let t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / (l * l);
    t = Math.max(c, Math.min(1 - c, t));
    return dist(p, {
        x: a.x + t * (b.x - a.x),
        y: a.y + t * (b.y - a.y)
    });
}

function redraw(pos) {
    ctx.clearRect(0, 0, w, h);
    stars.forEach(s => {
        ctx.beginPath();
        ctx.moveTo(s.x + 10, s.y);
        ctx.arc(s.x, s.y, 10, 0, Math.PI * 2);
        ctx.stroke();
    })
    if (pos != null && mode == "connect" && target != null) {
        ctx.beginPath();
        ctx.moveTo(stars[target].x, stars[target].y);
        ctx.lineTo(pos.x, pos.y);
        ctx.stroke();
    }
    lines.forEach(l => {
        ctx.beginPath();
        ctx.moveTo(stars[l.a].x, stars[l.a].y);
        ctx.lineTo(stars[l.b].x, stars[l.b].y);
        ctx.stroke();
    })
    genJava("constellation");
}

function findStarI(pos) {
    let i = stars.length;
    while (i--) {
        if (dist(pos, stars[i]) < 10) {
            return i;
        }
    }
}

canvas.addEventListener('mousedown', e => {
    var x = e.pageX - cLeft,
        y = e.pageY - cTop,
        pos = {x, y};
    if (mode == "add") {
        stars.push(pos);
        redraw(pos);
    } else if (mode == "delete") {
        let i = findStarI(pos);
        if (i != null) {
            stars.splice(i, 1);
            let j = lines.length;
            while (j--) {
                if (lines[j].a == i || lines[j].b == i) {
                    lines.splice(j, 1);
                }
            }
            for (let l of lines) {
                if (l.a > i) {
                    l.a --;
                }
                if (l.b > i) {
                    l.b --;
                }
            }
        }
        redraw(pos);
    } else if (mode == "connect") {
        if (target == null) {
            let i = findStarI(pos);
            if (i != null) {
                target = i;
            } else {
                i = lines.length;
                while (i--) {
                    if (lsDist(stars[lines[i].a], stars[lines[i].b], pos, 0.1) < 10) {
                        lines.splice(i, 1);
                        break;
                    }
                }
            }
        } else {
            let i = findStarI(pos);
            if (i != null && i != target) {
                if (lines.find(v => 
                        (v.a == target && v.b == i) || (v.b == target && v.a == i)
                        ) == undefined) {
                    lines.push({a: target, b: i});
                    target = i;
                    found = true;
                }
            } else {
                target = null;
            }
        }
        redraw(pos);
    } else if (mode == "drag") {
        let i = findStarI(pos);
        if (i != null) {
            target = i;
        }
    }
}, false);

canvas.addEventListener('mouseup', e => {
    if (mode == "drag") {
        target = null;
    }
})

canvas.addEventListener('mouseleave', e => {
    if (mode == "connect" || mode == "drag") {
        target = null;
        redraw();
    }
})

canvas.addEventListener('mousemove', e => {
    var x = e.pageX - cLeft,
        y = e.pageY - cTop,
        pos = {x, y};
    if (mode == "connect" && target != null) {
        redraw(pos);
    }
    if (mode == "drag" && target != null) {
        stars[target] = pos;
        redraw(pos);
    }
});

var phaseDiv = document.getElementById("phaseDiv");
function getPhaseList() {
    let vals = [],
        modified = false;
    for (let e of phaseDiv.children) {
        vals.push(e.checked);
        if (!e.checked) {
            modified = true;
        }
    }
    return {vals, modified};
}

var nameE = document.getElementById("name");
    moonVis = document.getElementById("moonVis")
    constText = document.getElementById("const"),
    regisText = document.getElementById("registry");
function genJava() {
    let name = nameE.value;
    let const_name = name.toUpperCase();
    let code = `public static Constellation ${const_name} = new Constellation(${moonVis.value})`
    stars.forEach(s => {
        code += `\n\t.addStar(${(s.x * 2 / w - 1).toFixed(2)}f, ${(s.y * 2 / h - 1).toFixed(2)}f)`
    })
    lines.forEach(l => {
        code += `\n\t.addConnection(${l.a}, ${l.b})`
    })
    let phaseData = getPhaseList();
    if (phaseData.modified) {
        code += `\n\t.setPhaseVisibility(new boolean[]{${phaseData.vals.join()}})`;
    }
    code += ";";
    constText.innerText = code;
    regisText.innerText = `Registry.register(CONSTELLATION_REGISTRY, Starcana.id("${name}"), ${const_name});`
}

redraw();