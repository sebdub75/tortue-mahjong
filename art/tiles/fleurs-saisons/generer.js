// Génère les symboles fleurs et saisons au format FluffyStuff (300 × 400, symbole seul,
// à poser sur Front.svg) : des illustrations en aplats cernés de noir, sans caractère
// chinois, pour qu'on les distingue au premier coup d'œil des familles ordinaires.
// Les saisons portent en plus un trait vert en haut et en bas, marque de leur famille.
// Usage : node generer.js     (sans dépendance ; écrit Flower1..4.svg et Season1..4.svg)
const fs = require("fs");
const path = require("path");

// Palette FluffyStuff, plus deux verts clairs pour les feuillages frais.
const BLACK = "#000000";
const WHITE = "#ffffff";
const RED = "#ff3737";
const DARK_RED = "#a53c3c";
const ORANGE = "#ff5c00";
const LIGHT_ORANGE = "#ffa040";
const BROWN = "#822600";
const GREEN = "#004900";
const LEAF_GREEN = "#3f8f2a";
const SPROUT_GREEN = "#7cc24a";
const BLUE = "#19328c";

const f = v => +v.toFixed(1);

// Point à la distance r de (cx, cy), angle a en degrés : 0 vers le haut, sens horaire.
function pt(cx, cy, a, r) {
  const t = a * Math.PI / 180;
  return [f(cx + r * Math.sin(t)), f(cy - r * Math.cos(t))];
}

function svg(body, season = false) {
  const marks = season
    ? [36, 364].map(y => `<line x1="40" y1="${y}" x2="260" y2="${y}" stroke="${GREEN}" stroke-width="10" stroke-linecap="round"/>`)
    : [];
  return [
    `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="400" viewBox="0 0 300 400">`,
    ...marks, ...body.flat(), "</svg>", "",
  ].join("\n");
}

// Trait coloré cerné de noir : le même tracé deux fois, le noir plus large dessous.
function outlined(d, width, color, outline = 8) {
  return [[BLACK, width + outline], [color, width]].map(([c, w]) =>
    `<path d="${d}" fill="none" stroke="${c}" stroke-width="${w}" stroke-linecap="round" stroke-linejoin="round"/>`);
}

// Feuille ou pétale en amande, de (x, y) vers l'angle a ; bend la fait pencher.
function leaf(x, y, a, length, width, color, sw = 4, bend = 0) {
  const tip = pt(x, y, a, length);
  const spread = Math.atan2(width, length / 2) * 180 / Math.PI;
  const r = Math.hypot(width, length / 2);
  const left = pt(x, y, a - spread + bend, r);
  const right = pt(x, y, a + spread + bend, r);
  return `<path d="M ${f(x)} ${f(y)} Q ${left} ${tip} Q ${right} ${f(x)} ${f(y)} Z" fill="${color}" stroke="${BLACK}" stroke-width="${sw}" stroke-linejoin="round"/>`;
}

const petals = (cx, cy, n, length, width, color, rot = 0, sw = 4) =>
  Array.from({ length: n }, (_, k) => leaf(cx, cy, rot + 360 * k / n, length, width, color, sw));

const circle = (cx, cy, r, fill, stroke = BLACK, sw = 5) =>
  `<circle cx="${f(cx)}" cy="${f(cy)}" r="${f(r)}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;

const polygon = (points, fill, sw) =>
  `<polygon points="${points.join(" ")}" fill="${fill}" stroke="${BLACK}" stroke-width="${sw}" stroke-linejoin="round"/>`;

// Fleur 1 : prunier, branche et fleurs rouges à cinq pétales.
function plum() {
  const blossom = (cx, cy, r, rot) => {
    const centers = [0, 1, 2, 3, 4].map(k => pt(cx, cy, rot + 72 * k, r * 0.55));
    return [
      centers.map(([x, y]) => circle(x, y, r * 0.5, RED)),
      // Second passage sans contour : efface les traits entre pétales.
      centers.map(([x, y]) => `<circle cx="${x}" cy="${y}" r="${f(r * 0.5 - 2.5)}" fill="${RED}"/>`),
      circle(cx, cy, r * 0.32, WHITE, DARK_RED, 3),
      [0, 1, 2, 3, 4].map(k => {
        const [x, y] = pt(cx, cy, rot + 36 + 72 * k, r * 0.2);
        return `<circle cx="${x}" cy="${y}" r="3.5" fill="${ORANGE}"/>`;
      }),
    ];
  };
  // Tout le noir d'abord : les rameaux se fondent dans la branche sans contour entre eux.
  const branches = [
    ["M 70 372 C 95 310 120 275 160 240 S 225 150 238 52", 20, 30],
    ["M 150 250 C 120 220 95 205 62 150", 13, 22],
    ["M 205 170 C 230 160 252 150 268 120", 9, 18],
  ];
  return svg([
    branches.map(([d, , outer]) => `<path d="${d}" fill="none" stroke="${BLACK}" stroke-width="${outer}" stroke-linecap="round"/>`),
    branches.map(([d, w]) => `<path d="${d}" fill="none" stroke="${BROWN}" stroke-width="${w}" stroke-linecap="round"/>`),
    circle(238, 52, 12, RED), circle(62, 148, 11, RED), circle(268, 118, 10, RED),
    blossom(100, 195, 62, 10), blossom(200, 125, 56, -15), blossom(205, 285, 50, 30),
  ]);
}

// Fleur 2 : orchidée, longues feuilles et fleurs blanches au labelle rouge.
function orchid() {
  const flower = (cx, cy, s, rot) => [
    petals(cx, cy, 3, 52 * s, 15 * s, WHITE, rot + 60),
    petals(cx, cy, 2, 46 * s, 16 * s, WHITE, rot - 60),
    leaf(cx, cy, rot + 180, 36 * s, 16 * s, DARK_RED),
    circle(cx, cy, 8 * s, ORANGE, BLACK, 3),
  ];
  return svg([
    [[-32, 230, -6], [-14, 255, 4], [10, 225, 8], [30, 190, 6], [-44, 160, -4]]
      .map(([a, length, bend]) => leaf(150, 365, a, length, 13, GREEN, 4, bend)),
    outlined("M 150 360 C 140 280 130 230 105 175", 6, LEAF_GREEN, 7),
    outlined("M 150 360 C 165 270 185 210 205 140", 6, LEAF_GREEN, 7),
    flower(100, 150, 1.55, -10), flower(205, 112, 1.55, 15), flower(226, 222, 1.15, 40),
    outlined("M 190 200 C 205 205 215 210 222 218", 4, LEAF_GREEN, 6),
  ]);
}

// Fleur 3 : chrysanthème, trois couronnes de pétales en rayons.
function chrysanthemum() {
  return svg([
    outlined("M 150 200 C 150 260 140 310 132 370", 10, LEAF_GREEN),
    leaf(144, 290, -60, 85, 26, GREEN), leaf(140, 330, 55, 80, 24, GREEN),
    petals(150, 150, 22, 105, 14, ORANGE, 0, 3.5),
    petals(150, 150, 16, 70, 14, LIGHT_ORANGE, 11, 3.5),
    petals(150, 150, 10, 40, 12, RED, 0, 3),
    circle(150, 150, 14, DARK_RED, BLACK, 3),
  ]);
}

// Fleur 4 : bambou, tiges à nœuds et bouquets de feuilles. Les tiges penchent et
// portent des feuilles pour ne pas se confondre avec la famille des bambous.
function bamboo() {
  const stalk = (x0, y0, x1, y1, w, nodes) => {
    const n = Math.hypot(x1 - x0, y1 - y0);
    const px = -(y1 - y0) / n * (w / 2 + 4), py = (x1 - x0) / n * (w / 2 + 4);
    return [
      outlined(`M ${x0} ${y0} L ${x1} ${y1}`, w, LEAF_GREEN),
      Array.from({ length: nodes - 1 }, (_, k) => {
        const t = (k + 1) / nodes, x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
        return `<line x1="${f(x - px)}" y1="${f(y - py)}" x2="${f(x + px)}" y2="${f(y + py)}" stroke="${BLACK}" stroke-width="5" stroke-linecap="round"/>`;
      }),
    ];
  };
  const tufts = [
    [142, 130, [-80, -55, -30]], [124, 215, [-110, -85, -60]], [158, 88, [40, 65, 90]],
    [213, 160, [-25, 0, 25]], [205, 262, [45, 70, 95]],
  ];
  return svg([
    stalk(100, 372, 160, 45, 22, 5),
    stalk(190, 372, 215, 120, 15, 4),
    tufts.map(([x, y, angles]) => angles.map(a => leaf(x, y, a, 78, 14, GREEN))),
  ]);
}

// Saison 1 : printemps, une pousse qui sort de terre.
function spring() {
  return svg([
    `<path d="M 55 340 Q 150 285 245 340 Z" fill="${BROWN}" stroke="${BLACK}" stroke-width="6" stroke-linejoin="round"/>`,
    outlined("M 150 320 C 148 260 152 200 150 150", 12, LEAF_GREEN),
    leaf(150, 225, -58, 125, 42, LEAF_GREEN, 5, -6),
    leaf(150, 205, 62, 115, 40, LEAF_GREEN, 5, 6),
    leaf(150, 155, -8, 85, 24, SPROUT_GREEN, 5),
    `<path d="M 150 228 L 95 158" stroke="${GREEN}" stroke-width="4" stroke-linecap="round"/>`,
    `<path d="M 150 208 L 200 147" stroke="${GREEN}" stroke-width="4" stroke-linecap="round"/>`,
  ], true);
}

// Saison 2 : été, un soleil.
function summer() {
  const rays = Array.from({ length: 12 }, (_, k) => {
    const a = 30 * k;
    return polygon([pt(150, 200, a, 118), pt(150, 200, a - 11, 72), pt(150, 200, a + 11, 72)], ORANGE, 5);
  });
  return svg([
    rays,
    circle(150, 200, 70, RED, BLACK, 6),
    circle(150, 200, 46, "none", DARK_RED, 5),
  ], true);
}

// Saison 3 : automne, une feuille d'érable à cinq lobes dentelés.
function autumn() {
  const cx = 150, cy = 193;
  const lobes = [[-110, 72], [-55, 108], [0, 122], [55, 108], [110, 72]];
  const outline = [pt(cx, cy, 180, 22)];
  lobes.forEach(([a, r], i) => {
    const w = Math.abs(a) < 100 ? 30 : 27;
    const side = [[0.95, 0.55], [0.9, 0.62], [1.05, 0.66], [0.6, 0.8], [0.75, 0.86], [0.3, 0.93]]
      .map(([kw, kr]) => [w * kw, r * kr]);
    if (i > 0) outline.push(pt(cx, cy, (a + lobes[i - 1][0]) / 2, 0.5 * Math.min(r, lobes[i - 1][1])));
    side.forEach(([dw, rr]) => outline.push(pt(cx, cy, a - dw, rr)));
    outline.push(pt(cx, cy, a, r));
    [...side].reverse().forEach(([dw, rr]) => outline.push(pt(cx, cy, a + dw, rr)));
  });
  outline.push(pt(cx, cy, 180, 22));
  const stem = `M ${cx} ${cy + 20} C 150 258 160 288 182 320`;
  return svg([
    outlined(stem, 8, BROWN),
    polygon(outline, ORANGE, 6),
    lobes.map(([a, r]) => {
      const [x, y] = pt(cx, cy, a, r * 0.85);
      return `<line x1="${cx}" y1="${cy}" x2="${x}" y2="${y}" stroke="${DARK_RED}" stroke-width="5" stroke-linecap="round"/>`;
    }),
  ], true);
}

// Saison 4 : hiver, un flocon à six branches.
function winter() {
  let d = "";
  for (let k = 0; k < 6; k++) {
    const a = 60 * k;
    d += `M 150 200 L ${pt(150, 200, a, 120)} `;
    for (const [r, s] of [[62, 30], [92, 22]]) {
      const p = pt(150, 200, a, r);
      for (const sign of [-1, 1]) d += `M ${p} L ${pt(p[0], p[1], a + sign * 45, s)} `;
    }
  }
  const hexagon = [0, 1, 2, 3, 4, 5].map(k => pt(150, 200, 60 * k + 30, 26));
  return svg([outlined(d.trim(), 11, BLUE), polygon(hexagon, WHITE, 5)], true);
}

const TILES = {
  Flower1: plum, Flower2: orchid, Flower3: chrysanthemum, Flower4: bamboo,
  Season1: spring, Season2: summer, Season3: autumn, Season4: winter,
};

for (const [name, draw] of Object.entries(TILES)) {
  fs.writeFileSync(path.join(__dirname, `${name}.svg`), draw());
}
