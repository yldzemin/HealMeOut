// Slim the NIH HuBMAP "3D Reference Organ Set for United, Male, v1.7" (CC BY 4.0)
// into one small GLB with one mesh per ASTRA-TWIN body region.
//
// Source:  https://cdn.humanatlas.io/digital-objects/ref-organ/united-male/v1.7/assets/3d-vh-m-united.glb
// DOI:     https://doi.org/10.48539/HBM933.WSQW.966  (licence: CC BY 4.0, DataCite)
//
// Run with a Node project that has @gltf-transform/core, @gltf-transform/functions,
// @gltf-transform/extensions and meshoptimizer installed (exact versions in the PR):
//   node slim_body_model.mjs <input.glb> <output.glb>
//
// Adaptations: only the groups below are kept; each group's meshes are baked
// into world space and joined into one mesh named after the region; geometry is
// welded, simplified to a triangle budget, quantized and meshopt-compressed.
// Normals are not kept; the viewer computes them.

import { Document, NodeIO } from "@gltf-transform/core";
import { EXTMeshoptCompression, KHRMeshQuantization } from "@gltf-transform/extensions";
import { meshopt, quantize } from "@gltf-transform/functions";
import { MeshoptEncoder, MeshoptSimplifier } from "meshoptimizer";

const [input, output] = process.argv.slice(2);
if (!input || !output) {
  console.error("usage: node slim_body_model.mjs <input.glb> <output.glb>");
  process.exit(1);
}

/** Region id -> source group node names and triangle budget. */
const REGIONS = {
  whole: { groups: ["VH_M_skin"], budget: 45000 },
  head: { groups: ["Allen_brain", "VH_M_spinal_cord"], budget: 26000 },
  eyes: { groups: ["VH_M_eyes"], budget: 8000 },
  heart: { groups: ["VH_M_heart"], budget: 22000 },
  lungs: { groups: ["VH_M_lungs", "VH_M_tracheobronchial_tree"], budget: 22000 },
  blood: { groups: ["VH_M_liver", "VH_M_upper_urinary_tract", "VH_M_blood_vasculature"], budget: 34000 },
  musculo: { groups: ["VH_M_skeletal_system"], budget: 40000 },
};

function mul(m, x, y, z, w) {
  return [
    m[0] * x + m[4] * y + m[8] * z + m[12] * w,
    m[1] * x + m[5] * y + m[9] * z + m[13] * w,
    m[2] * x + m[6] * y + m[10] * z + m[14] * w,
  ];
}

function collect(node, out) {
  const mesh = node.getMesh();
  if (mesh) {
    const world = node.getWorldMatrix();
    for (const prim of mesh.listPrimitives()) {
      if (prim.getMode() !== 4) continue; // triangles only
      out.push({ prim, world });
    }
  }
  for (const child of node.listChildren()) collect(child, out);
}

await MeshoptSimplifier.ready;

/** Weld equal positions, simplify to a triangle budget, drop unused vertices. */
function slimRegion(pos, idx, budget) {
  const key = new Map();
  const remap = new Uint32Array(pos.length / 3);
  const welded = [];
  for (let i = 0; i < pos.length / 3; i++) {
    const k = `${Math.round(pos[3 * i] * 1e5)},${Math.round(pos[3 * i + 1] * 1e5)},${Math.round(pos[3 * i + 2] * 1e5)}`;
    let j = key.get(k);
    if (j === undefined) {
      j = welded.length / 3;
      key.set(k, j);
      welded.push(pos[3 * i], pos[3 * i + 1], pos[3 * i + 2]);
    }
    remap[i] = j;
  }
  const wpos = new Float32Array(welded);
  const widx = idx.map((i) => remap[i]);
  const target = Math.min(widx.length, Math.floor(budget * 3));
  const [simple] = MeshoptSimplifier.simplify(widx, wpos, 3, target, 0.05, []);
  const used = new Map();
  const outPos = [];
  const outIdx = new Uint32Array(simple.length);
  for (let i = 0; i < simple.length; i++) {
    let j = used.get(simple[i]);
    if (j === undefined) {
      j = outPos.length / 3;
      used.set(simple[i], j);
      const v = simple[i] * 3;
      outPos.push(wpos[v], wpos[v + 1], wpos[v + 2]);
    }
    outIdx[i] = j;
  }
  return { positions: new Float32Array(outPos), indices: outIdx };
}

const io = new NodeIO().registerExtensions([EXTMeshoptCompression, KHRMeshQuantization]).registerDependencies({
  "meshopt.encoder": MeshoptEncoder,
});
const src = await io.read(input);
const byName = new Map(src.getRoot().listNodes().map((n) => [n.getName(), n]));

const doc = new Document();
const buffer = doc.createBuffer();
const scene = doc.createScene("ASTRA-TWIN body");

for (const [region, { groups }] of Object.entries(REGIONS)) {
  const parts = [];
  for (const g of groups) {
    const n = byName.get(g);
    if (!n) throw new Error(`missing group ${g}`);
    collect(n, parts);
  }
  const pos = [];
  const idx = [];
  for (const { prim, world } of parts) {
    const p = prim.getAttribute("POSITION");
    const base = pos.length / 3;
    const v = [0, 0, 0];
    for (let i = 0; i < p.getCount(); i++) {
      p.getElement(i, v);
      pos.push(...mul(world, v[0], v[1], v[2], 1));
    }
    const ind = prim.getIndices();
    if (ind) for (let i = 0; i < ind.getCount(); i++) idx.push(base + ind.getScalar(i));
    else for (let i = 0; i < p.getCount(); i++) idx.push(base + i);
  }
  const { positions, indices: slimIdx } = slimRegion(new Float32Array(pos), new Uint32Array(idx), REGIONS[region].budget);
  const position = doc.createAccessor().setType("VEC3").setArray(positions).setBuffer(buffer);
  const indices = doc
    .createAccessor()
    .setType("SCALAR")
    .setArray(positions.length / 3 > 65535 ? slimIdx : new Uint16Array(slimIdx))
    .setBuffer(buffer);
  const prim = doc.createPrimitive().setAttribute("POSITION", position).setIndices(indices);
  const mesh = doc.createMesh(region).addPrimitive(prim);
  scene.addChild(doc.createNode(region).setMesh(mesh));
  console.log(region, "triangles", idx.length / 3, "->", slimIdx.length / 3);
}

for (const a of doc.getRoot().listAccessors()) if (a.listParents().length <= 1) a.dispose();
doc.getRoot().getAsset().copyright =
  "Derived from HuBMAP 3D Reference Organ Set for United, Male, v1.7 (CC BY 4.0), doi:10.48539/HBM933.WSQW.966";
await doc.transform(quantize(), meshopt({ encoder: MeshoptEncoder, level: "medium" }));
await io.write(output, doc);
console.log("wrote", output);
