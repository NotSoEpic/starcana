function sunAngle(time) {
    return ((time / 24000 - 0.25) % 1 + 1) % 1;
}

function moonAngle(time) {
    return (((time - 6000) / 27000 + 0.5) % 1 + 1) % 1;
}

function starAlpha(time) {
    let a = sunAngle(time);
    let h = 1 - (Math.cos(a * Math.PI * 2) * 2 + 0.25);
    h = Math.min(Math.max(h, 0), 1);
    return h * h * 0.5;
}

function getMoonPhase(time) {
    return ((Math.trunc((time + 7500) / 27000) + 4) % 8 + 8) % 8;
}

function getMoonVisibility(time) {
    return Math.max(0, Math.min(1, 
        Math.sin((moonAngle(time) - 0.25) * Math.PI * 2)
    ));
}

class Constellation {
    constructor(posInSky, phaseVis, moonVis) {
        let r = Math.sqrt(posInSky.x ** 2 + posInSky.y ** 2 + posInSky.z ** 2);
        this.posInSky = {
            x: posInSky.x / r,
            y: posInSky.y / r,
            z: posInSky.z / r
        };
        this.phaseVis = phaseVis;
        this.moonVis = moonVis;
    }

    getTransformedPos(time) {
        let a = - Math.PI / 2;
        // rotation by "a" around y axis
        let transformed = {
            x:  this.posInSky.x * Math.cos(a) + this.posInSky.z * Math.sin(a),
            y: -this.posInSky.x * Math.sin(a) + this.posInSky.z * Math.cos(a),
            z:  this.posInSky.z
        };
        // rotation by "b" around x axis
        let b = sunAngle(time) * Math.PI * 2;
        transformed = {
            x:  transformed.x,
            y:  transformed.y * Math.cos(b) - transformed.z * Math.sin(b),
            z:  transformed.y * Math.sin(b) + transformed.z * Math.cos(b)
        };
        return transformed;
    }

    isVisiblePhase(phase) {
        return (!Array.isArray(this.phaseVis)) || this.phaseVis[phase];
    }

    visbilityMoonPos(time) {
        if (this.moonVis == "visible") {
            return getMoonVisibility(time);
        } else if (this.moonVis == "not_visible") {
            return 1 - getMoonVisibility(time);
        } else if (this.moonVis == "transition") {
            let v = getMoonVisibility(time);
            return Math.max(0, Math.min(1, 
                    5 * v * (1 - v) - 0.25
            ));
        } else {
            return 1;
        }
    }

    visibilityMoonPhase(time) {
        let visibilityTime = 200,
            prePhase = getMoonPhase(time - visibilityTime),
            postPhase = getMoonPhase(time + visibilityTime),
            inPrePhase = this.isVisiblePhase(prePhase),
            inPostPhase = this.isVisiblePhase(postPhase);
        if (inPrePhase && inPostPhase) {
            return 1;
        }
        if (!inPrePhase && !inPostPhase) {
            return 0;
        }
        let lerp = ((time % 27000 + 27000) % 27000 - 19500) / (visibilityTime * 2) + 0.5;
        if (!inPostPhase) {
            return 1 - lerp;
        } else {
            return lerp;
        }
    }

    isVisible(time) {
        return this.visibilityMoonPhase(time) > 0.1 && 
            this.visibilityMoonPos(time) > 0.1 && 
            starAlpha(time) > 0.1;
    }
}

function sampleConstellation(constellation) {
    let moonPhaseVis = [],
        moonPosVis = [],
        starVis = [],
        yPos = []
        canSee = [];
    for (let t = 0; t < 24000 * 9; t += 180) {
        let mphV = constellation.visibilityMoonPhase(t),
            mpoV = constellation.visbilityMoonPos(t),
            sA = starAlpha(t),
            pos = constellation.getTransformedPos(t),
            see = mphV > 0.1 && mpoV > 0.1 && sA > 0.1 && pos.y > -0.2;
        moonPhaseVis.push(mphV);
        moonPosVis.push(mpoV);
        starVis.push(sA);
        yPos.push(pos.y);
        canSee.push(see);
    }
    return { moonPhaseVis, moonPosVis, starVis, yPos, canSee }
}

let canvas = document.getElementById("graph"),
    ctx = canvas.getContext("2d"),
    w = canvas.width,
    h = canvas.height;
function drawGraph(constellation) {
    let data = sampleConstellation(constellation),
        length = data.moonPhaseVis.length,
        xW = w / length,
        yH = h / 5;
    ctx.clearRect(0, 0, w, h);
    for (let i = 0; i < length - 1; i++) {
        let x = i / length * w;

        ctx.beginPath();
        ctx.moveTo(x, (1 - data.moonPhaseVis[i]) * yH);
        ctx.lineTo(x + xW, (1 - data.moonPhaseVis[i + 1]) * yH);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(x, yH + (1 - data.moonPosVis[i]) * yH);
        ctx.lineTo(x + xW, yH + (1 - data.moonPosVis[i + 1]) * yH);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(x, yH * 2 + (1 - data.starVis[i]) * yH);
        ctx.lineTo(x + xW, yH * 2 + (1 - data.starVis[i + 1]) * yH);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(x, yH * 3 + (0.5 - data.yPos[i] * 0.5) * yH);
        ctx.lineTo(x + xW, yH * 3 + (0.5 - data.yPos[i + 1] * 0.5) * yH);
        ctx.stroke();

        if (data.canSee[i]) {
            ctx.fillRect(x, yH * 4, xW, yH);
        }
    }
}
let phaseDiv = document.getElementById("phaseDiv");
function getPhaseList() {
    let vals = [];
    for (let e of phaseDiv.children) {
        vals.push(e.checked);
    }
    return vals;
}
let skyX = document.getElementById("skyX"),
    skyY = document.getElementById("skyY"),
    skyZ = document.getElementById("skyZ"),
    moonVis = document.getElementById("moonVis");
var currentConstellation;
function createConstellation() {
    currentConstellation = new Constellation({
        x: skyX.value,
        y: skyY.value,
        z: skyZ.value
    }, getPhaseList(), moonVis.value);
    return currentConstellation;
}

function randomizeVec() {
    let u = Math.random(),
        v = Math.random(),
        theta = 2 * Math.PI * u,
        phi = Math.acos(2 * v - 1)
        vec = {
            x: Math.sin(phi) * Math.cos(theta),
            y: Math.sin(phi) * Math.sin(theta),
            z: Math.cos(phi)
        }
    skyX.value = vec.x.toFixed(2);
    skyY.value = vec.y.toFixed(2);
    skyZ.value = vec.z.toFixed(2);
}

drawGraph(createConstellation());