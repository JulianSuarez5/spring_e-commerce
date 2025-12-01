const ECommerce = {
    addToCart: function(productId, quantity) {
        fetch(`/cart/add?productId=${productId}&quantity=${quantity}`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) throw new Error("Error al agregar al carrito");
            return response.json();
        })
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            // ✅ Actualizar el contador del carrito en el navbar
            const cartBadge = document.querySelector('#cart-count');
            if (cartBadge) cartBadge.textContent = data.count;

            // ✅ Animación visual
            Swal.fire({
                title: "¡Agregado!",
                text: data.message,
                icon: "success",
                timer: 1500,
                showConfirmButton: false
            });
        })
        .catch(err => {
            console.error(err);
            Swal.fire({
                title: "Error",
                text: "No se pudo agregar el producto al carrito",
                icon: "error"
            });
        });
    }
};

// Función para actualizar el contador del carrito
function updateCartCount() {
    fetch('/cart/count')
        .then(res => res.json())
        .then(data => {
            document.getElementById('cart-count').textContent = data.count;
        });
}

// Llamar al cargar la página
document.addEventListener("DOMContentLoaded", updateCartCount);

// Función para agregar al carrito
function addToCart(productId, quantity = 1) {
    fetch('/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `productId=${productId}&quantity=${quantity}`
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === "success") {
            // Actualizar contador
            updateCartCount();

            // SweetAlert de éxito
            Swal.fire({
                icon: 'success',
                title: '¡Agregado!',
                text: `${data.productName} se agregó al carrito`,
                timer: 2000,
                showConfirmButton: false
            });
        } else if (data.status === "error") {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: data.message
            });
        }
    })
    .catch(err => {
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo agregar al carrito'
        });
    });
}