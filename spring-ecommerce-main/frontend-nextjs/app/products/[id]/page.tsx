'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import Product3DViewer from '@/components/Product3DViewer';
import { ProductDto } from '@/types/product';

export default function ProductDetailPage() {
  const params = useParams();
  const [product, setProduct] = useState<ProductDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (params.id) {
      fetchProduct(Number(params.id));
    }
  }, [params.id]);

  const fetchProduct = async (id: number) => {
    try {
      const response = await fetch(`/api/products/${id}`);
      if (response.ok) {
        const data = await response.json();
        setProduct(data);
      }
    } catch (error) {
      console.error('Error fetching product:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-white">
        Cargando producto...
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex items-center justify-center text-white">
        Producto no encontrado
      </div>
    );
  }

  return (
    <main className="min-h-screen pt-20 px-4 pb-20">
      <div className="max-w-7xl mx-auto">
        <Link href="/products" className="text-white/80 hover:text-white mb-6 inline-block">
          ← Volver a productos
        </Link>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* 3D Viewer */}
          <div>
            <Product3DViewer product={product} />
          </div>

          {/* Product Info */}
          <div className="glass p-8 rounded-2xl">
            <h1 className="text-4xl font-bold text-white mb-4">{product.name}</h1>
            
            {product.description && (
              <p className="text-white/80 mb-6 text-lg">{product.description}</p>
            )}

            <div className="mb-6">
              <span className="text-5xl font-bold text-white">
                ${product.price.toFixed(2)}
              </span>
            </div>

            {product.categoryName && (
              <div className="mb-4">
                <span className="text-white/60">Categoría: </span>
                <span className="text-white">{product.categoryName}</span>
              </div>
            )}

            {product.brandName && (
              <div className="mb-6">
                <span className="text-white/60">Marca: </span>
                <span className="text-white">{product.brandName}</span>
              </div>
            )}

            <div className="mb-6">
              <span className="text-white/60">Stock disponible: </span>
              <span className="text-white font-semibold">{product.cantidad} unidades</span>
            </div>

            <button className="btn-apple btn-apple-primary w-full py-4 text-lg">
              Agregar al Carrito
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}

