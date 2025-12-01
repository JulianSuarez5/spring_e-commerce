'use client';

import Link from 'next/link';
import { ProductDto } from '@/types/product';
import { motion } from 'framer-motion';

interface ProductCardProps {
  product: ProductDto;
}

export default function ProductCard({ product }: ProductCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      whileHover={{ y: -8 }}
    >
      <Link href={`/products/${product.id}`}>
        <div className="glass p-6 rounded-2xl cursor-pointer h-full flex flex-col">
          <div className="aspect-square mb-4 rounded-xl overflow-hidden bg-white/10">
            {product.imageUrl ? (
              <img
                src={`http://localhost:8081${product.imageUrl}`}
                alt={product.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-white/50">
                Sin imagen
              </div>
            )}
          </div>
          
          <h3 className="text-xl font-semibold text-white mb-2 line-clamp-2">
            {product.name}
          </h3>
          
          {product.description && (
            <p className="text-white/70 text-sm mb-4 line-clamp-2 flex-grow">
              {product.description}
            </p>
          )}
          
          <div className="flex justify-between items-center mt-auto">
            <span className="text-2xl font-bold text-white">
              ${product.price.toFixed(2)}
            </span>
            {product.model3dUrl && (
              <span className="text-xs text-white/60 bg-white/10 px-2 py-1 rounded">
                3D
              </span>
            )}
          </div>
        </div>
      </Link>
    </motion.div>
  );
}

