# 3D anatomy model attribution

`body.glb` is derived from:

- **HuBMAP, 3D Reference Organ Set for United, Male, v1.7** (Human Reference Atlas, NIH Common Fund HuBMAP).
- DOI: https://doi.org/10.48539/HBM933.WSQW.966
- Source file: https://cdn.humanatlas.io/digital-objects/ref-organ/united-male/v1.7/assets/3d-vh-m-united.glb
- Licence: **CC BY 4.0**, https://creativecommons.org/licenses/by/4.0/ (as listed in the DataCite record, checked 2026-11-14).

Adaptations (by `frontend/scripts/slim_body_model.mjs`): kept only the skin, brain and spinal cord, eyes, heart, lungs and airways, liver, kidneys and urinary tract, blood vessels and skeleton; joined each group into one mesh per app region; welded and simplified (about 4.0 M to 0.2 M triangles); quantized and meshopt-compressed; dropped original materials and normals.
