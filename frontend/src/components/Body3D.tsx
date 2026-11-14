import { Component, Suspense, useEffect, useMemo, useRef, type ReactNode } from "react";
import { Canvas, useFrame, useThree, type ThreeEvent } from "@react-three/fiber";
import { OrbitControls, useGLTF } from "@react-three/drei";
import * as THREE from "three";
import type { OrbitControls as OrbitControlsImpl } from "three-stdlib";
import type { Region, RegionId } from "@/data/personal";
import { LEVEL_HEX } from "@/components/regionStyle";

/**
 * 3D model: HuBMAP "3D Reference Organ Set for United, Male, v1.7"
 * (doi:10.48539/HBM933.WSQW.966), CC BY 4.0, slimmed by scripts/slim_body_model.mjs
 * into one mesh per region (public/models/body.glb).
 */
const MODEL_URL = "/models/body.glb";
export const MODEL_CREDIT =
  "3D anatomy: HuBMAP 3D Reference Organ Set (United, Male, v1.7), CC BY 4.0, simplified for this app.";

/** Camera distance (m) for the whole body and for one organ. */
const FULL_BODY_DISTANCE = 3.6;
const ORGAN_DISTANCE = 1.5;

/** Base tissue colours: muted, so the status colour reads on top. */
const BASE_COLOR: Record<RegionId, string> = {
  whole: "#00d2ff",
  head: "#d9a9a3",
  eyes: "#e8e4dc",
  heart: "#b8333a",
  lungs: "#d98f9a",
  blood: "#9c2f35",
  musculo: "#d8ccb4",
};

interface Part {
  geometry: THREE.BufferGeometry;
  // Quantized meshes keep their scale and offset in the node transform.
  position: THREE.Vector3;
  quaternion: THREE.Quaternion;
  scale: THREE.Vector3;
  center: THREE.Vector3;
}

function Organ({
  region,
  part,
  selected,
  onSelect,
}: {
  region: Region;
  part: Part;
  selected: boolean;
  onSelect: (id: RegionId) => void;
}) {
  const ref = useRef<THREE.MeshStandardMaterial>(null);
  const isSkin = region.id === "whole";
  const alerting = region.level !== "L0";
  const statusColor = useMemo(() => new THREE.Color(LEVEL_HEX[region.level]), [region.level]);

  // Slow pulse for alerting regions (2.4 s cycle, far below 3 flashes per second).
  useFrame(({ clock }) => {
    const m = ref.current;
    if (!m) return;
    const pulse = alerting ? 0.35 + 0.35 * Math.sin((clock.getElapsedTime() * 2 * Math.PI) / 2.4) : 0;
    const target = selected ? 0.55 : alerting ? 0.2 + pulse : 0;
    m.emissiveIntensity += (target - m.emissiveIntensity) * 0.15;
    if (isSkin) m.opacity += ((selected ? 0.28 : 0.1) - m.opacity) * 0.15;
  });

  const onClick = (e: ThreeEvent<MouseEvent>) => {
    // The skin wraps everything: only pick it when no organ is under the pointer.
    if (isSkin && e.intersections.some((i) => i.object.name !== "whole")) return;
    e.stopPropagation();
    onSelect(region.id);
  };

  return (
    <mesh
      name={region.id}
      geometry={part.geometry}
      position={part.position}
      quaternion={part.quaternion}
      scale={part.scale}
      onClick={onClick}
      onPointerOver={(e) => {
        e.stopPropagation();
        document.body.style.cursor = "pointer";
      }}
      onPointerOut={() => (document.body.style.cursor = "")}
      renderOrder={isSkin ? 2 : 1}
    >
      <meshStandardMaterial
        ref={ref}
        color={isSkin ? BASE_COLOR.whole : BASE_COLOR[region.id]}
        emissive={statusColor}
        emissiveIntensity={0}
        roughness={isSkin ? 0.2 : 0.55}
        metalness={0}
        transparent={isSkin}
        opacity={isSkin ? 0.1 : 1}
        depthWrite={!isSkin}
        side={isSkin ? THREE.DoubleSide : THREE.FrontSide}
      />
    </mesh>
  );
}

function BodyModel({
  regions,
  selected,
  onSelect,
  controls,
}: {
  regions: Region[];
  selected: RegionId | null;
  onSelect: (id: RegionId) => void;
  controls: React.RefObject<OrbitControlsImpl | null>;
}) {
  const { nodes } = useGLTF(MODEL_URL);
  const camera = useThree((s) => s.camera);

  const parts = useMemo(() => {
    const out = {} as Record<RegionId, Part>;
    for (const r of regions) {
      const node = nodes[r.id] as THREE.Mesh | undefined;
      if (!node?.geometry) continue;
      const g = node.geometry;
      if (!g.getAttribute("normal")) g.computeVertexNormals();
      g.computeBoundingBox();
      node.updateMatrix();
      const center = g.boundingBox!.getCenter(new THREE.Vector3()).applyMatrix4(node.matrix);
      out[r.id] = { geometry: g, position: node.position, quaternion: node.quaternion, scale: node.scale, center };
    }
    return out;
  }, [nodes, regions]);

  // On a new selection, glide to the region: whole body far, an organ close.
  // The glide stops once it arrives, so the user's own zoom is kept.
  const target = useMemo(() => new THREE.Vector3(0, 0, 0), []);
  const distance = useRef(FULL_BODY_DISTANCE);
  const gliding = useRef(true);
  useEffect(() => {
    const p = selected ? parts[selected] : undefined;
    const organ = p && selected !== "whole" && selected !== "musculo";
    if (organ) target.copy(p.center);
    else target.set(0, 0, 0);
    distance.current = organ ? ORGAN_DISTANCE : FULL_BODY_DISTANCE;
    gliding.current = true;
  }, [selected, parts, target]);
  // Hand control back to the user as soon as they drag or zoom.
  useEffect(() => {
    const c = controls.current;
    if (!c) return;
    const stop = () => (gliding.current = false);
    c.addEventListener("start", stop);
    return () => c.removeEventListener("start", stop);
  }, [controls]);
  const dir = useMemo(() => new THREE.Vector3(), []);
  useFrame(() => {
    const c = controls.current;
    if (!c || !gliding.current) return;
    c.target.lerp(target, 0.08);
    dir.subVectors(camera.position, c.target);
    const d = dir.length();
    const next = d + (distance.current - d) * 0.08;
    camera.position.copy(c.target).addScaledVector(dir.normalize(), next);
    c.update();
    if (c.target.distanceTo(target) < 0.002 && Math.abs(next - distance.current) < 0.005) gliding.current = false;
  });

  return (
    <group>
      {regions.map((r) =>
        parts[r.id] ? (
          <Organ key={r.id} region={r} part={parts[r.id]} selected={selected === r.id} onSelect={onSelect} />
        ) : null,
      )}
    </group>
  );
}

/** Shows a plain message if WebGL or the model fails (e.g. an old GPU driver). */
class ViewerBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  render() {
    if (this.state.failed)
      return (
        <div className="flex h-full items-center justify-center p-6 text-center text-sm text-muted-foreground">
          The 3D body could not load on this computer. Use the region buttons below.
        </div>
      );
    return this.props.children;
  }
}

/** 3D anatomy viewer: drag to rotate, click an organ to select its region. */
export function Body3D({
  regions,
  selected,
  onSelect,
}: {
  regions: Region[];
  selected: RegionId | null;
  onSelect: (id: RegionId) => void;
}) {
  const controls = useRef<OrbitControlsImpl | null>(null);
  return (
    <div className="relative h-full w-full" aria-label="3D body. Drag to rotate, click an organ, or use the region buttons.">
      <ViewerBoundary>
        <Canvas camera={{ position: [0, 0, FULL_BODY_DISTANCE], fov: 32 }} dpr={[1, 2]} gl={{ antialias: true, alpha: true }}>
          <ambientLight intensity={0.55} />
          <directionalLight position={[1.5, 2, 2.5]} intensity={1.4} />
          <directionalLight position={[-2, 1, -2]} intensity={0.7} color="#00d2ff" />
          <Suspense fallback={null}>
            <BodyModel regions={regions} selected={selected} onSelect={onSelect} controls={controls} />
          </Suspense>
          <OrbitControls
            ref={controls}
            enablePan={false}
            enableDamping
            minDistance={1.4}
            maxDistance={4}
            minPolarAngle={Math.PI * 0.2}
            maxPolarAngle={Math.PI * 0.8}
          />
        </Canvas>
      </ViewerBoundary>
      <p className="pointer-events-none absolute left-3 top-2 text-xs text-muted-foreground">Drag to rotate · scroll to zoom · click an organ</p>
    </div>
  );
}

useGLTF.preload(MODEL_URL);
