package com.tanaka.asset.asset_manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;

import static com.tanaka.asset.asset_manager.controller.DashboardController.logger;

@Entity
@Table(name = "asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "asset_type")
    private String assetType;

    @Column(name = "asset_name")
    private String assetName;

    private BigDecimal balance;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String note;

    // フォーマット済みのupdatedAtを格納するフィールド
    @Transient
    private String updatedAtFormatted;

    @PrePersist
    public void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getUpdatedAtFormatted() {
        if (this.updatedAt != null) {
            // logger.info("UpdatedAt before formatting: {}", this.updatedAt);  // フォーマット前のupdatedAtをログ出力
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = this.updatedAt.format(formatter);
            // logger.info("Formatted UpdatedAt: {}", formattedDate);  // フォーマット後の日付をログ出力
            return formattedDate;
        }
        return null; // updatedAtがnullの場合はnullを返す
    }
}
