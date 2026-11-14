import { Component, Suspense, type ReactNode } from "react";
import { Canvas } from "@react-three/fiber";
import { Bounds, OrbitControls, useGLTF } from "@react-three/drei";

/** NASA 3D Resources "International Space Station (ISS) (B)", free and without copyright. */
const ISS_URL = "/models/iss.glb";

function Iss() {
  const { scene } = useGLTF(ISS_URL);
  return <primitive object={scene} />;
}

class Boundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  render() {
    return this.state.failed ? null : this.props.children;
  }
}

/** Small, slowly turning overview of the station (orientation only, no crew pins). */
export function IssOverview3D() {
  return (
    <div className="relative h-full w-full" aria-label="3D view of the ISS (NASA model)">
      <Boundary>
        <Canvas camera={{ position: [60, 30, 60], fov: 35 }} dpr={[1, 2]} gl={{ alpha: true, antialias: true }}>
          <ambientLight intensity={0.8} />
          <directionalLight position={[50, 80, 40]} intensity={1.6} />
          <Suspense fallback={null}>
            <Bounds fit clip observe margin={1.05}>
              <Iss />
            </Bounds>
          </Suspense>
          <OrbitControls autoRotate autoRotateSpeed={0.6} enablePan={false} enableZoom={false} makeDefault />
        </Canvas>
      </Boundary>
      <p className="pointer-events-none absolute bottom-1 left-2 text-[10px] text-muted-foreground">NASA 3D Resources ISS model</p>
    </div>
  );
}

useGLTF.preload(ISS_URL);
