package com.food.ordering.system.payment.service.domain.ports;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    public PaymentRequestHelper(PaymentDomainService paymentDomainService,
                                PaymentDataMapper paymentDataMapper,
                                PaymentRepository paymentRepository,
                                CreditEntryRepository creditEntryRepository,
                                CreditHistoryRepository creditHistoryRepository) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest){
        log.info("Received payment for order id {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.PaymentRequestToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(paymentRequest);
        List<CreditHistory> creditHistories = getCreditHistory(paymentRequest);
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);
        persistDbObjects(payment, creditEntry, creditHistories, failureMessages);
        return paymentEvent;
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest){
        log.info("Received payment rollback for order id {}", paymentRequest.getOrderId());
        Payment payment = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()))
                .orElseThrow(()->new PaymentApplicationServiceException("There is no payment registered for order " + paymentRequest.getOrderId()));
        CreditEntry creditEntry = getCreditEntry(paymentRequest);
        List<CreditHistory> creditHistories = getCreditHistory(paymentRequest);
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent =
                paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages);
        persistDbObjects(payment, creditEntry, creditHistories, failureMessages);
        return paymentEvent;
    }

    private void persistDbObjects(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()){
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
        }
    }

    private List<CreditHistory> getCreditHistory(PaymentRequest paymentRequest) {
        return creditHistoryRepository.findByCustomerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
                .orElseThrow(()-> new PaymentApplicationServiceException("Could not find credit entry for customer " + paymentRequest.getCustomerId()));
    }

    private CreditEntry getCreditEntry(PaymentRequest paymentRequest) {
        return creditEntryRepository.findByCustomerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
                .orElseThrow(()-> new PaymentApplicationServiceException("Could not find credit entry for customer " + paymentRequest.getCustomerId()));
    }
}
