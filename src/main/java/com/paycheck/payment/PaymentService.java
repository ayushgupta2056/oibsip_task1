package com.paycheck.payment;

import com.paycheck.common.CardUtils;
import com.paycheck.common.NotFoundException;
import com.paycheck.fraud.FraudRule;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final List<FraudRule> fraudRules;   // Spring injects every FraudRule bean
    private final Clock clock;

    public PaymentService(PaymentRepository repository, List<FraudRule> fraudRules, Clock clock) {
        this.repository = repository;
        this.fraudRules = fraudRules;
        this.clock = clock;
    }

    /** Runs every fraud rule; the first rule that fails declines the payment. */
    public PaymentResponse submit(PaymentRequest request) {
        Optional<String> declineReason = fraudRules.stream()
                .map(rule -> rule.check(request))
                .flatMap(Optional::stream)
                .findFirst();

        PaymentStatus status = declineReason.isPresent() ? PaymentStatus.DECLINED : PaymentStatus.APPROVED;

        Payment payment = new Payment(
                CardUtils.hash(request.cardNumber()),
                CardUtils.mask(request.cardNumber()),
                request.amount(),
                request.country().toUpperCase(),
                status,
                declineReason.orElse(null),
                Instant.now(clock));

        return PaymentResponse.from(repository.save(payment));
    }

    public List<PaymentResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    public PaymentResponse findById(Long id) {
        return repository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new NotFoundException("Payment " + id + " not found"));
    }
}
