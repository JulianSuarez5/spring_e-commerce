package ppi.e_commerce.Service;

// Stripe integration removed for now; imports cleaned up
import ppi.e_commerce.Model.Payment;
import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// removed unused util imports

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    // constructor removed because Stripe initialization is not used here

    @Override
    public Payment createPayment(Order order, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(order.getTotalPrice());
        payment.setCurrency("COP");
        payment.setStatus("pending");
        payment.setDescription("Payment for Order #" + order.getNumber());
        
        return paymentRepository.save(payment);
    }

    // Stripe payment method removed - only PayPal is supported

    @Override
    public Payment processPayPalPayment(String paymentId, String payerId) {
        try {
            // This would typically use PayPalService to execute the payment
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setPaymentMethod("paypal");
            payment.setStatus("completed");
            payment.setTransactionId(payerId);
            
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Error processing PayPal payment: " + e.getMessage());
        }
    }

    @Override
    public Payment processCashOnDelivery(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("cash_on_delivery");
        payment.setAmount(order.getTotalPrice());
        payment.setCurrency("COP");
        payment.setStatus("pending");
        payment.setDescription("Cash on Delivery for Order #" + order.getNumber());
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePaymentStatus(String paymentId, String status) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);
        if (payment != null) {
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }
        return null;
    }

    // Stripe methods removed - only PayPal is supported

    @Override
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }
}
