export interface ProductDto {
  id: number;
  name: string;
  description: string;
  price: number;
  cantidad: number;
  imageUrl: string | null;
  model3dUrl: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  categoryId: number | null;
  categoryName: string | null;
  brandId: number | null;
  brandName: string | null;
}

export interface Category {
  id: number;
  name: string;
  description: string | null;
  imageUrl: string | null;
  active: boolean;
}

export interface Brand {
  id: number;
  name: string;
  description: string | null;
  logoUrl: string | null;
  website: string | null;
  active: boolean;
}

