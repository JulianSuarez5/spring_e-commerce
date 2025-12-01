package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Payment;
import ppi.e_commerce.Model.Order;

public interface PaymentService {
    Payment createPayment(Order order, String paymentMethod);
    Payment processPayPalPayment(String paymentId, String payerId);
    Payment processCashOnDelivery(Order order);
    Payment updatePaymentStatus(String paymentId, String status);
    Payment getPaymentById(String paymentId);
}