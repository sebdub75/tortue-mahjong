// Génère les symboles fleurs et saisons au format FluffyStuff (300 × 400, symbole seul,
// à poser sur Front.svg). Voir README.md pour la police à télécharger.
// Usage : npm install && node generer.js <YujiBoku-Regular.ttf> .
const fs = require("fs");
const path = require("path");
const opentype = require("opentype.js");

const [fontFile, outDir] = process.argv.slice(2);
const font = opentype.loadSync(fontFile);

const RED = "#b93c3c";
const GREEN = "#004900";
const BLACK = "#000000";

const TILES = [
  { file: "Flower1", char: "梅", num: "一", color: RED },
  { file: "Flower2", char: "蘭", num: "二", color: RED },
  { file: "Flower3", char: "菊", num: "三", color: RED },
  { file: "Flower4", char: "竹", num: "四", color: RED },
  { file: "Season1", char: "春", num: "一", color: GREEN },
  { file: "Season2", char: "夏", num: "二", color: GREEN },
  { file: "Season3", char: "秋", num: "三", color: GREEN },
  { file: "Season4", char: "冬", num: "四", color: GREEN },
];

// Tracé d'un glyphe mis à l'échelle pour remplir la boîte [x, y, w, h], centré.
function fitted(ch, box) {
  const raw = font.getPath(ch, 0, 0, 1000);
  const b = raw.getBoundingBox();
  const s = Math.min(box.w / (b.x2 - b.x1), box.h / (b.y2 - b.y1));
  const ox = box.x + (box.w - (b.x2 - b.x1) * s) / 2 - b.x1 * s;
  const oy = box.y + (box.h - (b.y2 - b.y1) * s) / 2 - b.y1 * s;
  const p = font.getPath(ch, 0, 0, 1000);
  p.commands.forEach(c => {
    for (const [kx, ky] of [["x", "y"], ["x1", "y1"], ["x2", "y2"]]) {
      if (c[kx] !== undefined) { c[kx] = c[kx] * s + ox; c[ky] = c[ky] * s + oy; }
    }
  });
  return p.toPathData(2);
}

const CHAR_BOX = { x: 18, y: 104, w: 264, h: 282 };
const NUM_BOX = { x: 20, y: 14, w: 112, h: 78 };

fs.mkdirSync(outDir, { recursive: true });
for (const t of TILES) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="400" viewBox="0 0 300 400">
  <path fill="${BLACK}" stroke="${BLACK}" stroke-width="5" stroke-linejoin="round" d="${fitted(t.num, NUM_BOX)}"/>
  <path fill="${t.color}" stroke="${t.color}" stroke-width="7" stroke-linejoin="round" d="${fitted(t.char, CHAR_BOX)}"/>
</svg>
`;
  fs.writeFileSync(path.join(outDir, `${t.file}.svg`), svg);
}
console.log(`${TILES.length} tuiles écrites dans ${outDir}`);
