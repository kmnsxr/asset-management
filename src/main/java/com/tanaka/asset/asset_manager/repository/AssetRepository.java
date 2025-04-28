package com.tanaka.asset.asset_manager.repository;

import com.tanaka.asset.asset_manager.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// AssetRepositoryインターフェース
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // ログイン中ユーザーの資産を取得するメソッド
    List<Asset> findByUserId(Long userId);

    // ログイン中ユーザーの口座名リストを取得するメソッド
    // ユーザーIDに基づいてasset_nameの重複を削除し、空の値を除去
    @Query("SELECT DISTINCT a.assetName FROM Asset a WHERE a.userId = :userId AND a.assetName IS NOT NULL AND a.assetName != ''")
    List<String> findDistinctAssetNamesByUserId(@Param("userId") Long userId);

    List<Asset> findByAssetName(String assetNameToDelete);
}
