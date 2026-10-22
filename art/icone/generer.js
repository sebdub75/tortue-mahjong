// Génère l'icône adaptative de l'application : une paire de dragons rouges 中 en
// relief, dessinée comme dans le jeu (voir app/.../TileRenderer.kt), sur le tapis vert.
// Usage (depuis la racine du dépôt) : node art/icone/generer.js
// Sortie : app/src/main/res/drawable/ic_launcher_{background,foreground,monochrome}.xml
//
// Pas de dépendance : le symbole 中 est repris tel quel de tile_chun.xml et placé sur la
// face par une transformation de groupe.
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..", "..");
const RES = path.join(ROOT, "app", "src", "main", "res", "drawable");

// Icône adaptative : 108 × 108 dp, dont seul le disque central de 66 dp est toujours
// visible quel que soit le masque du lanceur.
const SIZE = 108;
const CENTER = SIZE / 2;

// Proportions de TileMetrics : face 3 × 4, épaisseur et arrondi relatifs à la largeur.
const W = 26;
const H = W * 4 / 3;
const T = W * 0.14;
const R = W * 0.12;
const STROKE = W * 0.012;

// Les deux tuiles côte à côte, l'ensemble (faces et tranches) centré.
const X0 = CENTER - (2 * W + T) / 2 + T;
const Y0 = CENTER - (H + T) / 2;
const TILES = [X0 + W, X0]; // de droite à gauche, comme dans le jeu

const n = v => +v.toFixed(2);
const pt = (x, y) => `${n(x)},${n(y)}`;

// Volume balayé par la face de coin (x, y) translatée de (-t, t) : même construction que
// extrusion() dans TileRenderer.kt, contour parcouru dans le sens horaire.
function extrusion(x, y, t) {
  const k = R / Math.SQRT2;
  const arc = (px, py) => `A${n(R)},${n(R)} 0 0 1 ${pt(px, py)}`;
  return [
    `M${pt(x + R, y)}`,
    `H${n(x + W - R)}`,
    arc(x + W, y + R),
    `V${n(y + H - R)}`,
    arc(x + W - R + k, y + H - R + k),
    `L${pt(x + W - R + k - t, y + H - R + k + t)}`,
    arc(x + W - R - t, y + H + t),
    `H${n(x + R - t)}`,
    arc(x - t, y + H - R + t),
    `V${n(y + R + t)}`,
    arc(x + R - k - t, y + R - k + t),
    `L${pt(x + R - k, y + R - k)}`,
    arc(x + R, y),
    "Z",
  ].join(" ");
}

const face = (x, y) => extrusion(x, y, 0);

// Demi-plan sous la diagonale qui part du coin bas-gauche de la face : la tranche du bas.
function belowDiagonal(x, y) {
  const far = 100;
  const cx = x, cy = y + H;
  return `M${pt(cx + far, cy - far)} L${pt(cx - far, cy + far)} L${pt(cx + far, cy + far)} Z`;
}

// Ce que la tuile de gauche laisse voir de celle de droite : à droite de sa face, puis
// sous la diagonale de sa tranche. La tuile de droite y est découpée, ce qui la place
// derrière sans dépendre de l'ordre de dessin (nécessaire pour l'icône monochrome).
function rightOfTile(x, y) {
  const k = R / Math.SQRT2;
  const ex = x + W - R + k, ey = y + H - R + k;
  const far = 100;
  return `M${pt(x + W, -far)} L${pt(x + W, y + H - R)} L${pt(ex, ey)} ` +
    `L${pt(ex - far, ey + far)} L${pt(SIZE + far, ey + far)} L${pt(SIZE + far, -far)} Z`;
}

const chun = fs.readFileSync(path.join(RES, "tile_chun.xml"), "utf8")
  .match(/android:pathData="[^"]+"/g);

// Symbole 300 × 400 posé sur la face avec la même marge que dans le jeu.
function symbol(x, y, color) {
  const inset = 0.06;
  const paths = chun.map(d => `
        <path
            android:fillColor="${color}"
            android:fillType="evenOdd"
            ${d} />`).join("");
  return `
    <group
        android:translateX="${n(x + W * inset)}"
        android:translateY="${n(y + H * inset)}"
        android:scaleX="${+(W * (1 - 2 * inset) / 300).toFixed(5)}"
        android:scaleY="${+(H * (1 - 2 * inset) / 400).toFixed(5)}">${paths}
    </group>`;
}

// Tuile en couleurs : mêmes teintes que TileRenderer.kt.
function colorTile(x, y) {
  const hull = extrusion(x, y, T);
  return `
    <path
        android:fillColor="#A53C3C"
        android:pathData="${hull}" />
    <path
        android:fillColor="#DCD2C4"
        android:pathData="${extrusion(x, y, T * 0.55)}" />
    <group>
        <clip-path android:pathData="${belowDiagonal(x, y)}" />
        <path
            android:fillColor="#2E000000"
            android:pathData="${hull}" />
    </group>
    <path
        android:strokeColor="#59000000"
        android:strokeWidth="${n(STROKE)}"
        android:pathData="${hull}" />
    <path android:pathData="${face(x, y)}">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="${n(x + W)}"
                android:startY="${n(y)}"
                android:endX="${n(x)}"
                android:endY="${n(y + H)}"
                android:startColor="#FBF8F4"
                android:endColor="#E9E1D6" />
        </aapt:attr>
    </path>
    <path
        android:strokeColor="#40000000"
        android:strokeWidth="${n(STROKE)}"
        android:pathData="${face(x, y)}" />${symbol(x, y, "#FFB93C3C")}`;
}

// Tuile monochrome : le lanceur teinte d'une seule couleur, seul l'alpha compte. La
// tranche est en demi-teinte, la face vide, son contour et le symbole pleins.
function monoTile(x, y) {
  return `
    <path
        android:fillColor="#80FFFFFF"
        android:fillType="evenOdd"
        android:pathData="${extrusion(x, y, T)} ${face(x, y)}" />
    <path
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="${n(STROKE * 3)}"
        android:pathData="${face(x, y)}" />${symbol(x, y, "#FFFFFFFF")}`;
}

// Ombre portée : la silhouette prolongée, en aplat translucide (pas de flou en vectoriel).
function shadow(x, y) {
  return `
    <path
        android:fillColor="#33000000"
        android:pathData="${extrusion(x + T * 0.3, y + T * 0.3, T * 1.6)}" />`;
}

function vector(body, aapt = false) {
  return `<?xml version="1.0" encoding="utf-8"?>
<!-- Généré par art/icone/generer.js : ne pas modifier à la main. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"${aapt ? `
    xmlns:aapt="http://schemas.android.com/aapt"` : ""}
    android:width="${SIZE}dp"
    android:height="${SIZE}dp"
    android:viewportWidth="${SIZE}"
    android:viewportHeight="${SIZE}">${body}
</vector>
`;
}

function tiles(draw) {
  const [right, left] = TILES;
  return `
    <group>
        <clip-path android:pathData="${rightOfTile(left, Y0)}" />${draw(right, Y0).replace(/\n/g, "\n    ")}
    </group>${draw(left, Y0)}`;
}

const background = `
    <path android:pathData="M0,0 H${SIZE} V${SIZE} H0 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="radial"
                android:centerX="${CENTER}"
                android:centerY="${CENTER}"
                android:gradientRadius="${n(SIZE * 0.75)}"
                android:startColor="#2B6E55"
                android:endColor="#0D2B21" />
        </aapt:attr>
    </path>`;

const outputs = {
  "ic_launcher_background.xml": vector(background, true),
  "ic_launcher_foreground.xml": vector(tiles(shadow) + tiles(colorTile), true),
  "ic_launcher_monochrome.xml": vector(tiles(monoTile)),
};
for (const [name, xml] of Object.entries(outputs)) {
  fs.writeFileSync(path.join(RES, name), xml);
  console.log(`${name} : ${xml.length} octets`);
}
