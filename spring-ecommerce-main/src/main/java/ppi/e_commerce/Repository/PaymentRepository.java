package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByPaymentId(String paymentId);
    Payment findByTransactionId(String transactionId);
}
