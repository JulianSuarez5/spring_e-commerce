'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import ProductCard from '@/components/ProductCard';
import { ProductDto } from '@/types/product';

export default function Home() {
  const [products, setProducts] = useState<ProductDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch('/api/products');
      const data = await response.json();
      setProducts(data);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen">
      {/* Header Glassmorphism */}
      <header className="glass-strong fixed top-0 left-0 right-0 z-50 p-4">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold text-white">E-Commerce Pro</h1>
          <nav className="flex gap-4">
            <Link href="/products" className="btn-apple">
              Productos
            </Link>
            <Link href="/login" className="btn-apple">
              Iniciar Sesión
            </Link>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <h2 className="text-6xl font-bold text-white mb-6">
            Descubre Productos en 3D
          </h2>
          <p className="text-xl text-white/80 mb-8">
            Experiencia de compra inmersiva con visualización interactiva
          </p>
          <Link href="/products" className="btn-apple btn-apple-primary inline-block">
            Explorar Productos
          </Link>
        </div>
      </section>

      {/* Products Grid */}
      <section className="px-4 pb-20">
        <div className="max-w-7xl mx-auto">
          <h3 className="text-4xl font-bold text-white mb-8 text-center">
            Productos Destacados
          </h3>
          
          {loading ? (
            <div className="text-center text-white">Cargando productos...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.slice(0, 8).map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}

