// Convert the NASA 3D Resources "International Space Station (ISS) (B)" GLB from
// Draco to meshopt so the app needs no Draco decoder from a CDN (works offline).
// Source: https://github.com/nasa/NASA-3D-Resources (3D Models/International Space Station (ISS) (B))
// NASA: "These assets are free and without copyright."
// Needs @gltf-transform/core, /extensions, /functions, meshoptimizer, draco3dgltf.
// Usage: node convert_iss_model.mjs <input.glb> <output.glb>

import { NodeIO } from "@gltf-transform/core";
import { ALL_EXTENSIONS } from "@gltf-transform/extensions";
import { meshopt, quantize, prune, dedup } from "@gltf-transform/functions";
import { MeshoptEncoder } from "meshoptimizer";
import draco3d from "draco3dgltf";
const [i, o] = process.argv.slice(2);
const io = new NodeIO().registerExtensions(ALL_EXTENSIONS).registerDependencies({
  "draco3d.decoder": await draco3d.createDecoderModule(),
  "meshopt.encoder": MeshoptEncoder,
});
const doc = await io.read(i);
for (const e of doc.getRoot().listExtensionsUsed()) if (e.extensionName === "KHR_draco_mesh_compression") e.dispose();
await doc.transform(dedup(), prune(), quantize(), meshopt({ encoder: MeshoptEncoder, level: "medium" }));
doc.getRoot().getAsset().copyright = "NASA 3D Resources: International Space Station (ISS) (B). Free and without copyright (NASA).";
await io.write(o, doc);
console.log("wrote", o);
