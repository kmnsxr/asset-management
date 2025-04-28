package com.tanaka.asset.asset_manager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaka.asset.asset_manager.entity.Asset;
import com.tanaka.asset.asset_manager.repository.AssetRepository;
import com.tanaka.asset.asset_manager.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private AssetRepository assetRepository;

    public static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.info("\u30ed\u30b0\u30a4\u30f3\u60c5\u5831\u672a\u8a8d\u8a3c");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<Asset> assetList = assetRepository.findByUserId(userId);
        assetList.sort(Comparator.comparing(Asset::getUpdatedAt));

        List<String> dates = new ArrayList<>();
        List<BigDecimal> balances = new ArrayList<>();

        for (Asset asset : assetList) {
            String formattedDate = asset.getUpdatedAtFormatted();
            if (formattedDate != null) {
                dates.add(formattedDate);
            }
            balances.add(asset.getBalance());
        }

        Map<String, Asset> latestAssetsMap = assetList.stream()
                .collect(Collectors.toMap(
                        Asset::getAssetName,
                        asset -> asset,
                        (existing, replacement) -> existing.getUpdatedAt().isAfter(replacement.getUpdatedAt()) ? existing : replacement
                ));

        List<Asset> latestAssets = new ArrayList<>(latestAssetsMap.values());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        latestAssets.forEach(asset -> {
            asset.setUpdatedAtFormatted(asset.getUpdatedAt().format(formatter));
        });

        model.addAttribute("datesJson", new ObjectMapper().writeValueAsString(dates));
        model.addAttribute("balancesJson", new ObjectMapper().writeValueAsString(balances));
        model.addAttribute("assets", latestAssets);

        return "dashboard";
    }

    @GetMapping("/api/asset-data")
    @ResponseBody
    public Map<String, Object> getAssetData() {
        Long userId = getUserId();
        List<Asset> assetList = assetRepository.findByUserId(userId);

        Map<String, BigDecimal> aggregatedBalancesByDate = new TreeMap<>();
        List<String> formattedDates = new ArrayList<>();

        for (Asset asset : assetList) {
            String formattedDate = asset.getUpdatedAtFormatted();
            if (!formattedDates.contains(formattedDate)) {
                formattedDates.add(formattedDate);
            }
        }
        Collections.sort(formattedDates);

        for (String formattedDate : formattedDates) {
            Map<String, Asset> latestAssetsByName = new HashMap<>();
            for (Asset pastAsset : assetList) {
                String pastFormattedDate = pastAsset.getUpdatedAtFormatted();
                if (pastFormattedDate.equals(formattedDate) || pastAsset.getUpdatedAt().isBefore(LocalDate.parse(formattedDate).atStartOfDay())) {
                    String key = pastAsset.getAssetName();
                    if (!latestAssetsByName.containsKey(key) || pastAsset.getUpdatedAt().isAfter(latestAssetsByName.get(key).getUpdatedAt())) {
                        latestAssetsByName.put(key, pastAsset);
                    }
                }
            }

            BigDecimal totalBalance = BigDecimal.ZERO;
            for (Asset latestAsset : latestAssetsByName.values()) {
                totalBalance = totalBalance.add(latestAsset.getBalance());
            }
            aggregatedBalancesByDate.put(formattedDate, totalBalance);
        }

        List<String> dates = new ArrayList<>(aggregatedBalancesByDate.keySet());
        List<BigDecimal> balances = dates.stream().map(aggregatedBalancesByDate::get).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("dates", dates);
        response.put("balances", balances);
        return response;
    }

    @GetMapping("/api/asset-type-data")
    @ResponseBody
    public Map<String, Object> getAssetTypeData() {
        Long userId = getUserId();
        List<Asset> assetList = assetRepository.findByUserId(userId);

        // 最新だけを取得
        Map<String, Asset> latestAssetsMap = assetList.stream()
                .collect(Collectors.toMap(
                        Asset::getAssetName,
                        asset -> asset,
                        (existing, replacement) -> existing.getUpdatedAt().isAfter(replacement.getUpdatedAt()) ? existing : replacement
                ));

        Map<String, BigDecimal> assetTypeBalances = new HashMap<>();

        for (Asset asset : latestAssetsMap.values()) {
            assetTypeBalances.put(
                    asset.getAssetType(),
                    assetTypeBalances.getOrDefault(asset.getAssetType(), BigDecimal.ZERO).add(asset.getBalance())
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("assetTypeBalances", assetTypeBalances);
        return response;
    }


    @GetMapping("/assets/register")
    public String showRegisterForm(Model model) {
        Long userId = getUserId();
        List<String> assetNames = assetRepository.findDistinctAssetNamesByUserId(userId);
        assetNames.removeIf(name -> name == null || name.trim().isEmpty());
        model.addAttribute("assetNames", assetNames);
        return "register_asset";
    }

    @PostMapping("/assets/register")
    public String registerAsset(@RequestParam String assetType, @RequestParam String assetName, @RequestParam(required = false) String customAssetName, @RequestParam BigDecimal balance) {
        Long userId = getUserId();
        if ("other".equals(assetName) && customAssetName != null && !customAssetName.trim().isEmpty()) {
            assetName = customAssetName;
        }
        Asset asset = Asset.builder()
                .userId(userId)
                .assetType(assetType)
                .assetName(assetName)
                .balance(balance)
                .updatedAt(LocalDateTime.now())
                .build();
        assetRepository.save(asset);
        return "redirect:/dashboard";
    }

    @GetMapping("/assets/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid asset Id:" + id));
        model.addAttribute("asset", asset);
        return "edit_asset";
    }

    @PostMapping("/assets/edit/{id}")
    public String updateAsset(@PathVariable Long id, @RequestParam String assetType, @RequestParam String assetName, @RequestParam(required = false) String customAssetName, @RequestParam BigDecimal balance, @RequestParam(required = false) String note) {
        Asset existingAsset = assetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid asset Id:" + id));
        String finalAssetName = "\u305d\u306e\u4ed6".equals(assetName) && customAssetName != null && !customAssetName.isBlank() ? customAssetName : assetName;

        Asset newAsset = Asset.builder()
                .userId(existingAsset.getUserId())
                .assetType(assetType)
                .assetName(finalAssetName)
                .balance(balance)
                .note(note)
                .updatedAt(LocalDateTime.now())
                .build();

        assetRepository.save(newAsset);
        return "redirect:/dashboard";
    }

    @GetMapping("/assets/delete/{id}")
    public String deleteAsset(@PathVariable Long id) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid asset Id:" + id));
        List<Asset> assetsToDelete = assetRepository.findByAssetName(asset.getAssetName());
        assetRepository.deleteAll(assetsToDelete);
        return "redirect:/dashboard";
    }

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
