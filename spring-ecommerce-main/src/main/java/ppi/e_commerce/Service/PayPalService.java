package ppi.e_commerce.Service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    @Value("${app.paypal.business-email}")
    private String businessEmail;

    public Payment createPayment(Double total, String currency, String method, String intent, 
                               String description, String cancelUrl, String successUrl) throws PayPalRESTException {
        
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }

    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
        return Payment.get(apiContext, paymentId);
    }

    public Refund createRefund(String paymentId, Double amount, String currency) throws PayPalRESTException {
        Amount refundAmount = new Amount();
        refundAmount.setCurrency(currency);
        refundAmount.setTotal(String.format("%.2f", amount));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(refundAmount);

        Sale sale = new Sale();
        sale.setId(paymentId);

        return sale.refund(apiContext, refundRequest);
    }

    public String verifyWebhook(String payload, String signature) throws PayPalRESTException {
        // PayPal webhook verification would go here
        // This is a simplified version - in real implementation, you would verify the signature
        return "webhook_verified";
    }
}
