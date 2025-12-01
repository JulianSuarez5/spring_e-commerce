/**
 * E-Commerce Pro - Custom JavaScript
 * Funcionalidad global del sitio
 */

const ECommerce = {
    /**
     * Agregar producto al carrito con SweetAlert
     */
    addToCart: async function(productId, quantity = 1) {
        console.log('🛒 Agregando producto:', productId, 'Cantidad:', quantity);
        
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (!csrfToken || !csrfHeader) {
            console.error('❌ Token CSRF no encontrado');
            Swal.fire({
                icon: 'error',
                title: 'Error de seguridad',
                text: 'No se pudo verificar la sesión',
                confirmButtonColor: '#dc2626'
            });
            return;
        }

        try {
            const response = await fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body: new URLSearchParams({
                    productId: productId,
                    quantity: quantity
                })
            });

            console.log('📡 Respuesta del servidor:', response.status);

            const data = await response.json();
            console.log('📦 Datos recibidos:', data);

            if (data.status === 'success') {
                // ✅ Mostrar SweetAlert de éxito
                Swal.fire({
                    icon: 'success',
                    title: '¡Producto agregado!',
                    html: `
                        <div style="text-align: center;">
                            <p style="font-size: 1.1rem; margin-bottom: 8px;">
                                <strong>${data.productName}</strong>
                            </p>
                            <p style="color: #64748b; font-size: 0.95rem;">
                                Cantidad: <strong>${data.itemQuantity || quantity}</strong>
                            </p>
                        </div>
                    `,
                    showConfirmButton: true,
                    confirmButtonText: '<i class="bi bi-cart-check"></i> Ver carrito',
                    showCancelButton: true,
                    cancelButtonText: '<i class="bi bi-shop"></i> Seguir comprando',
                    confirmButtonColor: '#667eea',
                    cancelButtonColor: '#6c757d',
                    timer: 5000,
                    timerProgressBar: true
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/cart';
                    }
                });

                // Actualizar contador del carrito en el navbar
                const cartCount = document.getElementById('cartItemCount');
                if (cartCount && data.cartItemCount !== undefined) {
                    cartCount.textContent = data.cartItemCount;
                    
                    // Animación del contador
                    cartCount.classList.add('cart-bounce');
                    setTimeout(() => {
                        cartCount.classList.remove('cart-bounce');
                    }, 600);
                }

            } else if (data.status === 'error' && data.redirectUrl) {
                // ⚠️ Usuario no autenticado
                Swal.fire({
                    icon: 'warning',
                    title: 'Inicia sesión',
                    text: data.message || 'Debes iniciar sesión para agregar productos',
                    confirmButtonColor: '#667eea',
                    confirmButtonText: 'Ir a login'
                }).then(() => {
                    window.location.href = data.redirectUrl;
                });

            } else {
                // ❌ Error general
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: data.message || 'No se pudo agregar el producto',
                    confirmButtonColor: '#dc2626'
                });
            }

        } catch (error) {
            console.error('❌ Error al agregar producto:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error de conexión',
                text: 'No se pudo conectar con el servidor. Por favor, intenta nuevamente.',
                confirmButtonColor: '#dc2626'
            });
        }
    },

    /**
     * Mostrar notificación toast simple
     */
    showNotification: function(message, type = 'success') {
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            didOpen: (toast) => {
                toast.addEventListener('mouseenter', Swal.stopTimer);
                toast.addEventListener('mouseleave', Swal.resumeTimer);
            }
        });

        const iconMap = {
            'success': 'success',
            'danger': 'error',
            'error': 'error',
            'warning': 'warning',
            'info': 'info'
        };

        Toast.fire({
            icon: iconMap[type] || 'info',
            title: message
        });
    }
};

// CSS para animación del contador del carrito
const style = document.createElement('style');
style.textContent = `
    @keyframes cartBounce {
        0%, 100% {
            transform: scale(1);
        }
        25% {
            transform: scale(1.4) rotate(-10deg);
        }
        50% {
            transform: scale(1.2) rotate(5deg);
        }
        75% {
            transform: scale(1.3) rotate(-5deg);
        }
    }

    .cart-bounce {
        animation: cartBounce 0.6s ease-in-out;
        display: inline-block;
    }

    /* Estilo personalizado para SweetAlert */
    .swal2-popup {
        animation: slideInDown 0.3s ease-out;
    }

    @keyframes slideInDown {
        from {
            opacity: 0;
            transform: translateY(-30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Mejorar apariencia de los botones de SweetAlert */
    .swal2-confirm, .swal2-cancel {
        border-radius: 8px !important;
        font-weight: 600 !important;
        padding: 12px 28px !important;
        transition: all 0.3s ease !important;
        font-size: 0.95rem !important;
    }

    .swal2-confirm:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
    }

    .swal2-timer-progress-bar {
        background: #667eea !important;
    }

    .swal2-html-container {
        font-family: 'Segoe UI', system-ui, sans-serif !important;
    }
`;
document.head.appendChild(style);

// Log de inicialización
console.log('✅ E-Commerce Pro JavaScript loaded');
console.log('📦 ECommerce object:', ECommerce);