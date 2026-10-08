package com.taogueyuan.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status; // 待处理、已付款、已发货、已完成、已取消

    @Column(nullable = false)
    private String shippingAddress;
    
    private String contactPhone;
    
    private String contactName;
    
    private String refundStatus; // NONE (无), REFUND_APPLIED (已申请), REFUND_APPROVED (已批准), REFUND_REJECTED (已拒绝), REFUND_SUCCESS (退款成功)
    
    private String refundType; // ONLY_REFUND (仅退款), RETURN_AND_REFUND (退货退款)
    
    private String refundReason;
    
    private String returnLogisticsCompany;
    
    private String returnTrackingNumber;
    
    private String returnStatus; // WAITING_SHIPMENT (待寄件), SHIPPED (已寄件), RECEIVED (已收货)

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (refundStatus == null) {
            refundStatus = "NONE";
        }
    }
}
