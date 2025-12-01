'use client';

import { Suspense, useRef } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Environment, useGLTF } from '@react-three/drei';
import { ProductDto } from '@/types/product';

interface Product3DViewerProps {
  product: ProductDto;
}

function Model({ url }: { url: string }) {
  const { scene } = useGLTF(url);
  return <primitive object={scene} />;
}

export default function Product3DViewer({ product }: Product3DViewerProps) {
  if (!product.model3dUrl) {
    return (
      <div className="glass p-8 rounded-2xl text-center text-white/60">
        Este producto no tiene modelo 3D disponible
      </div>
    );
  }

  return (
    <div className="glass rounded-2xl overflow-hidden aspect-square">
      <Canvas
        camera={{ position: [0, 0, 5], fov: 50 }}
        gl={{ antialias: true, alpha: true }}
      >
        <Suspense fallback={null}>
          <ambientLight intensity={0.5} />
          <directionalLight position={[10, 10, 5]} intensity={1} />
          <pointLight position={[-10, -10, -5]} intensity={0.5} />
          
          <Model url={`http://localhost:8081${product.model3dUrl}`} />
          
          <OrbitControls
            enableZoom={true}
            enablePan={false}
            minDistance={3}
            maxDistance={10}
            autoRotate
            autoRotateSpeed={1}
          />
          
          <Environment preset="sunset" />
        </Suspense>
      </Canvas>
    </div>
  );
}

