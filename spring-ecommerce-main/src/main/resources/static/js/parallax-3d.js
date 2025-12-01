/**
 * Parallax 3D Interactive - E-Commerce Pro
 * Efecto parallax 3D que responde al movimiento del mouse
 * Técnica: CSS 3D Transforms + JavaScript para tracking del mouse
 */

(function() {
    'use strict';

    class Parallax3D {
        constructor(containerSelector = '.hero-3d-area') {
            this.container = document.querySelector(containerSelector);
            if (!this.container) return;
            
            this.elements = this.container.querySelectorAll('.object-3d');
            this.mouseX = 0;
            this.mouseY = 0;
            this.targetX = 0;
            this.targetY = 0;
            this.currentX = 0;
            this.currentY = 0;
            
            // Configuración de fricción (0.1 = suave, 0.9 = rápido)
            this.friction = 0.1;
            
            // Intensidad del efecto (ajustable)
            this.intensity = 15;
            
            this.init();
        }

        init() {
            if (this.elements.length === 0) return;
            
            // Event listeners
            this.container.addEventListener('mousemove', (e) => this.handleMouseMove(e));
            this.container.addEventListener('mouseleave', () => this.handleMouseLeave());
            
            // Animación loop con requestAnimationFrame
            this.animate();
        }

        handleMouseMove(e) {
            const rect = this.container.getBoundingClientRect();
            const centerX = rect.left + rect.width / 2;
            const centerY = rect.top + rect.height / 2;
            
            // Calcular posición relativa al centro (-1 a 1)
            this.mouseX = (e.clientX - centerX) / (rect.width / 2);
            this.mouseY = (e.clientY - centerY) / (rect.height / 2);
            
            // Actualizar target con intensidad
            this.targetX = this.mouseX * this.intensity;
            this.targetY = this.mouseY * this.intensity;
        }

        handleMouseLeave() {
            // Reset suave cuando el mouse sale
            this.targetX = 0;
            this.targetY = 0;
        }

        animate() {
            // Interpolación suave con fricción (easing)
            this.currentX += (this.targetX - this.currentX) * this.friction;
            this.currentY += (this.targetY - this.currentY) * this.friction;
            
            // Aplicar transformaciones a cada elemento
            this.elements.forEach((element, index) => {
                // Diferentes profundidades para cada elemento
                const depth = parseFloat(element.dataset.depth) || 1;
                const rotationX = this.currentY * depth;
                const rotationY = this.currentX * depth;
                
                // Aplicar transformación 3D
                element.style.transform = `
                    perspective(1000px) 
                    rotateX(${rotationX}deg) 
                    rotateY(${rotationY}deg)
                    translateZ(${this.currentX * depth * 0.5}px)
                `;
            });
            
            // Continuar animación
            requestAnimationFrame(() => this.animate());
        }
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            new Parallax3D();
        });
    } else {
        new Parallax3D();
    }

    // Exportar para uso global si es necesario
    window.Parallax3D = Parallax3D;
})();

