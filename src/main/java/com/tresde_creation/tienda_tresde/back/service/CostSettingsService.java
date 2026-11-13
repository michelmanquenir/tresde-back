package com.tresde_creation.tienda_tresde.back.service;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.CostSettingsDto;
import com.tresde_creation.tienda_tresde.back.model.CostSettings;
import com.tresde_creation.tienda_tresde.back.model.Material;
import com.tresde_creation.tienda_tresde.back.repository.CostSettingsRepository;

@Service
public class CostSettingsService {

	private final CostSettingsRepository repository;

	public CostSettingsService(CostSettingsRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public CostSettingsDto get() {
		return toDto(find());
	}

	@Transactional
	public CostSettingsDto update(CostSettingsDto dto) {
		CostSettings s = find();
		s.setMachineHourCost(dto.machineHourCost());
		s.setMachineReservePercent(dto.machineReservePercent());
		s.setPriceMultiplier(dto.priceMultiplier());
		// Los materiales que no vengan conservan su precio actual
		dto.filamentPricePerKg().forEach(s::setFilamentPrice);
		return toDto(s);
	}

	private CostSettings find() {
		return repository.findById(CostSettings.SINGLETON_ID)
				.orElseThrow(() -> new IllegalStateException("Falta la fila de cost_settings (migración V4)."));
	}

	private static CostSettingsDto toDto(CostSettings s) {
		Map<Material, Integer> prices = new EnumMap<>(Material.class);
		for (Material m : Material.values()) prices.put(m, s.getFilamentPrice(m));
		return new CostSettingsDto(s.getMachineHourCost(), s.getMachineReservePercent(), s.getPriceMultiplier(), prices);
	}
}
