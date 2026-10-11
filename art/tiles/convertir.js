// Convertit les symboles SVG des tuiles en VectorDrawable Android.
// Usage (depuis art/tiles/) : npm install && node convertir.js
// Sortie : drawable/tile_<nom>.xml, à déplacer dans app/src/main/res/drawable/.
//
// svg2vectordrawable seul perd une partie du dessin des fichiers Inkscape (transformations
// imbriquées, clones <use>, styles en ligne, pointillés). On commence donc par aplatir
// chaque SVG : une liste de <path> en coordonnées finales, styles résolus, pointillés
// découpés en segments. Le convertisseur n'a plus qu'une traduction directe à faire.
const fs = require("fs");
const path = require("path");
const svg2vectordrawable = require("svg2vectordrawable");
const svgpath = require("svgpath");
const { svgPathProperties } = require("svg-path-properties");
const { DOMParser } = require("@xmldom/xmldom");

const HERE = __dirname;
const OUT = path.join(HERE, "drawable");

// Face (Front.svg) et dos (Back.svg) ne sont pas convertis : ils utilisent filtres et
// masques, que VectorDrawable ne gère pas, et le corps de la tuile est dessiné en Compose.
const SOURCES = {
  fluffystuff: [
    ...[1, 2, 3, 4, 5, 6, 7, 8, 9].flatMap(i => [`Man${i}`, `Pin${i}`, `Sou${i}`]),
    "Ton", "Nan", "Shaa", "Pei", "Chun", "Hatsu", "Haku",
  ],
  "fleurs-saisons": [1, 2, 3, 4].flatMap(i => [`Flower${i}`, `Season${i}`]),
};

// Remplace chaque <use> par une copie de sa cible : Inkscape enchaîne des clones
// (<use> vers <use> ou vers un groupe).
function inlineUses(doc) {
  for (let pass = 0; pass < 20; pass++) {
    const uses = Array.from(doc.getElementsByTagName("use"));
    if (uses.length === 0) return;
    const byId = new Map();
    for (const el of Array.from(doc.getElementsByTagName("*"))) {
      if (el.getAttribute("id")) byId.set(el.getAttribute("id"), el);
    }
    for (const use of uses) {
      const ref = (use.getAttribute("xlink:href") || use.getAttribute("href")).slice(1);
      const target = byId.get(ref);
      if (!target) throw new Error(`<use> vers #${ref} introuvable`);
      const g = doc.createElementNS("http://www.w3.org/2000/svg", "g");
      const x = parseFloat(use.getAttribute("x") || 0);
      const y = parseFloat(use.getAttribute("y") || 0);
      const transform = [use.getAttribute("transform"), x || y ? `translate(${x},${y})` : ""];
      if (transform.join("").trim()) g.setAttribute("transform", transform.join(" ").trim());
      if (use.getAttribute("style")) g.setAttribute("style", use.getAttribute("style"));
      // Un autre <use> peut viser celui-ci : le groupe reprend son id.
      if (use.getAttribute("id")) g.setAttribute("id", use.getAttribute("id"));
      const copy = target.cloneNode(true);
      // Ids en double : on les retire de la copie (les <use> imbriqués gardent leur href).
      copy.removeAttribute("id");
      for (const el of Array.from(copy.getElementsByTagName("*"))) el.removeAttribute("id");
      g.appendChild(copy);
      use.parentNode.replaceChild(g, use);
    }
  }
  throw new Error("<use> imbriqués trop profondément");
}

// --- Matrices affines [a, b, c, d, e, f], comme en SVG ---

const IDENTITY = [1, 0, 0, 1, 0, 0];
const multiply = (m, n) => [
  m[0] * n[0] + m[2] * n[1], m[1] * n[0] + m[3] * n[1],
  m[0] * n[2] + m[2] * n[3], m[1] * n[2] + m[3] * n[3],
  m[0] * n[4] + m[2] * n[5] + m[4], m[1] * n[4] + m[3] * n[5] + m[5],
];

function parseTransform(text) {
  let m = IDENTITY;
  for (const [, op, args] of (text || "").matchAll(/(\w+)\s*\(([^)]*)\)/g)) {
    const v = args.split(/[\s,]+/).filter(Boolean).map(Number);
    const rad = a => (a * Math.PI) / 180;
    let t;
    switch (op) {
      case "matrix": t = v; break;
      case "translate": t = [1, 0, 0, 1, v[0], v[1] || 0]; break;
      case "scale": t = [v[0], 0, 0, v.length > 1 ? v[1] : v[0], 0, 0]; break;
      case "rotate": {
        const [c, s] = [Math.cos(rad(v[0])), Math.sin(rad(v[0]))];
        t = [c, s, -s, c, 0, 0];
        if (v.length === 3) t = multiply(multiply([1, 0, 0, 1, v[1], v[2]], t), [1, 0, 0, 1, -v[1], -v[2]]);
        break;
      }
      case "skewX": t = [1, 0, Math.tan(rad(v[0])), 1, 0, 0]; break;
      case "skewY": t = [1, Math.tan(rad(v[0])), 0, 1, 0, 0]; break;
      default: throw new Error(`transformation inconnue : ${op}`);
    }
    m = multiply(m, t);
  }
  return m;
}

// --- Styles ---

const INHERITED = [
  "fill", "fill-opacity", "fill-rule", "stroke", "stroke-width", "stroke-opacity",
  "stroke-linecap", "stroke-linejoin", "stroke-miterlimit", "stroke-dasharray",
  "stroke-dashoffset", "visibility",
];
const INITIAL = {
  fill: "#000000", "fill-opacity": "1", "fill-rule": "nonzero", stroke: "none",
  "stroke-width": "1", "stroke-opacity": "1", "stroke-linecap": "butt",
  "stroke-linejoin": "miter", "stroke-miterlimit": "4", "stroke-dasharray": "none",
  "stroke-dashoffset": "0", visibility: "visible", opacity: "1",
};

function ownStyle(el) {
  const s = {};
  for (const k of [...INHERITED, "opacity", "display"]) {
    if (el.getAttribute(k)) s[k] = el.getAttribute(k).trim();
  }
  for (const decl of (el.getAttribute("style") || "").split(";")) {
    const i = decl.indexOf(":");
    if (i > 0) s[decl.slice(0, i).trim()] = decl.slice(i + 1).trim();
  }
  return s;
}

const length = v => parseFloat(String(v).replace(/(px|pt)$/, "")) * (String(v).endsWith("pt") ? 4 / 3 : 1);

// --- Formes ---

function shapeToPath(el) {
  const n = k => parseFloat(el.getAttribute(k) || 0);
  switch (el.tagName) {
    case "path": return el.getAttribute("d");
    case "rect": {
      const [x, y, w, h] = [n("x"), n("y"), n("width"), n("height")];
      let rx = el.getAttribute("rx") ? n("rx") : n("ry");
      let ry = el.getAttribute("ry") ? n("ry") : rx;
      rx = Math.min(rx, w / 2); ry = Math.min(ry, h / 2);
      if (!rx || !ry) return `M${x},${y}H${x + w}V${y + h}H${x}Z`;
      return `M${x + rx},${y}H${x + w - rx}A${rx},${ry} 0 0 1 ${x + w},${y + ry}V${y + h - ry}` +
        `A${rx},${ry} 0 0 1 ${x + w - rx},${y + h}H${x + rx}A${rx},${ry} 0 0 1 ${x},${y + h - ry}` +
        `V${y + ry}A${rx},${ry} 0 0 1 ${x + rx},${y}Z`;
    }
    case "circle": case "ellipse": {
      const [cx, cy] = [n("cx"), n("cy")];
      const rx = el.tagName === "circle" ? n("r") : n("rx");
      const ry = el.tagName === "circle" ? n("r") : n("ry");
      return `M${cx - rx},${cy}A${rx},${ry} 0 1 0 ${cx + rx},${cy}A${rx},${ry} 0 1 0 ${cx - rx},${cy}Z`;
    }
    case "line":
      return `M${n("x1")},${n("y1")}L${n("x2")},${n("y2")}`;
    case "polyline": case "polygon":
      return "M" + el.getAttribute("points").trim() + (el.tagName === "polygon" ? "Z" : "");
    default: return null;
  }
}

// Découpe un tracé en tirets : VectorDrawable ne connaît pas stroke-dasharray.
// Chaque tiret devient une polyligne finement échantillonnée ; le motif repart à chaque
// sous-tracé, comme dans les navigateurs.
function dash(d, pattern, offset) {
  const subpaths = [];
  svgpath(d).abs().iterate((seg, i) => {
    if (seg[0] === "M" || i === 0) subpaths.push([]);
    subpaths[subpaths.length - 1].push(seg);
  });
  const total = pattern.reduce((a, b) => a + b, 0);
  let out = "";
  for (const segs of subpaths) {
    const sub = segs.map(s => s.join(" ")).join(" ");
    const props = new svgPathProperties(sub);
    const L = props.getTotalLength();
    let pos = -(((offset % total) + total) % total);
    for (let k = 0; pos < L; k = (k + 1) % pattern.length) {
      const start = pos, end = pos + pattern[k];
      pos = end;
      if (k % 2 === 1 || end <= 0) continue;
      const a = Math.max(0, start), b = Math.min(L, end);
      const steps = Math.max(1, Math.ceil((b - a) / 1.5));
      for (let j = 0; j <= steps; j++) {
        const p = props.getPointAtLength(a + ((b - a) * j) / steps);
        out += `${j === 0 ? "M" : "L"}${p.x.toFixed(2)},${p.y.toFixed(2)}`;
      }
    }
  }
  return out;
}

// --- Aplatissement ---

const SKIPPED = new Set([
  "defs", "clipPath", "mask", "metadata", "title", "desc", "sodipodi:namedview",
  "linearGradient", "radialGradient", "filter", "pattern", "symbol", "style", "script",
]);

function flatten(svgText) {
  const doc = new DOMParser().parseFromString(svgText, "image/svg+xml");
  inlineUses(doc);
  const root = doc.documentElement;
  const [vx, vy, vw, vh] = (root.getAttribute("viewBox") || "0 0 300 400").split(/[\s,]+/).map(Number);
  const paths = [];

  function walk(el, matrix, inherited, opacity) {
    if (el.nodeType !== 1 || SKIPPED.has(el.tagName)) return;
    for (const attr of ["clip-path", "mask", "filter"]) {
      const v = el.getAttribute(attr) || ownStyle(el)[attr];
      if (v && v !== "none") throw new Error(`${attr} non géré sur <${el.tagName}>`);
    }
    const own = ownStyle(el);
    if (own.display === "none") return;
    const style = { ...inherited };
    for (const k of INHERITED) if (own[k] !== undefined && own[k] !== "inherit") style[k] = own[k];
    const m = multiply(matrix, parseTransform(el.getAttribute("transform")));
    const op = opacity * parseFloat(own.opacity || 1);

    if (el.tagName === "g" || el.tagName === "svg") {
      for (const child of Array.from(el.childNodes)) walk(child, m, style, op);
      return;
    }
    const d = shapeToPath(el);
    if (!d || style.visibility === "hidden") return;
    if (/url\(/.test(style.fill) || /url\(/.test(style.stroke)) throw new Error("dégradé ou motif non géré");

    const scale = Math.sqrt(Math.abs(m[0] * m[3] - m[1] * m[2]));
    const transformed = svgpath(d).matrix(m).toString();
    const fill = style.fill !== "none" && parseFloat(style["fill-opacity"]) > 0;
    const stroke = style.stroke !== "none" && parseFloat(style["stroke-opacity"]) > 0 &&
      length(style["stroke-width"]) > 0;
    const strokeAttrs = () =>
      `stroke="${style.stroke}" stroke-opacity="${parseFloat(style["stroke-opacity"]) * op}" ` +
      `stroke-width="${(length(style["stroke-width"]) * scale).toFixed(3)}" ` +
      `stroke-linecap="${style["stroke-linecap"]}" stroke-linejoin="${style["stroke-linejoin"]}" ` +
      `stroke-miterlimit="${style["stroke-miterlimit"]}"`;

    if (fill) {
      paths.push(`<path d="${transformed}" fill="${style.fill}" ` +
        `fill-opacity="${parseFloat(style["fill-opacity"]) * op}" fill-rule="${style["fill-rule"]}"/>`);
    }
    if (stroke) {
      const pattern = style["stroke-dasharray"] === "none" ? [] :
        style["stroke-dasharray"].split(/[\s,]+/).map(v => length(v) * scale);
      const dashed = pattern.length > 0 && pattern.some(v => v > 0);
      const strokeD = dashed
        ? dash(transformed, pattern.length % 2 ? [...pattern, ...pattern] : pattern,
            length(style["stroke-dashoffset"]) * scale)
        : transformed;
      if (strokeD) paths.push(`<path d="${strokeD}" fill="none" ${strokeAttrs()}/>`);
    }
  }

  const viewBoxShift = [1, 0, 0, 1, -vx, -vy];
  for (const child of Array.from(root.childNodes)) walk(child, viewBoxShift, { ...INITIAL }, 1);
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${vw}" height="${vh}" viewBox="0 0 ${vw} ${vh}">` +
    paths.join("") + `</svg>`;
}

async function main() {
  fs.mkdirSync(OUT, { recursive: true });
  const flatDir = process.env.FLAT_DIR; // pour vérifier l'étape d'aplatissement seule
  if (flatDir) fs.mkdirSync(flatDir, { recursive: true });
  let count = 0;
  for (const [dir, names] of Object.entries(SOURCES)) {
    for (const name of names) {
      let svg;
      try {
        svg = flatten(fs.readFileSync(path.join(HERE, dir, `${name}.svg`), "utf8"));
      } catch (e) {
        throw new Error(`${dir}/${name}.svg : ${e.message}`);
      }
      if (flatDir) fs.writeFileSync(path.join(flatDir, `tile_${name.toLowerCase()}.svg`), svg);
      const xml = await svg2vectordrawable(svg, { floatPrecision: 2, xmlTag: true });
      fs.writeFileSync(path.join(OUT, `tile_${name.toLowerCase()}.xml`), xml);
      count++;
    }
  }
  console.log(`${count} VectorDrawable écrits dans ${OUT}`);
}

main().catch(e => { console.error(e.message); process.exit(1); });
