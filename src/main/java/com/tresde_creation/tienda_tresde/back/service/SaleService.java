package com.tresde_creation.tienda_tresde.back.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.SaleRequest;
import com.tresde_creation.tienda_tresde.back.dto.SaleResponse;
import com.tresde_creation.tienda_tresde.back.model.CostSettings;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.model.Sale;
import com.tresde_creation.tienda_tresde.back.model.SaleCost;
import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.CostSettingsRepository;
import com.tresde_creation.tienda_tresde.back.repository.ProductRepository;
import com.tresde_creation.tienda_tresde.back.repository.SaleRepository;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/** Registro de ventas hechas a mano desde el panel (descuenta y devuelve stock). */
@Service
public class SaleService {

	private static final Instant MIN = Instant.parse("2000-01-01T00:00:00Z");
	private static final Instant MAX = Instant.parse("9999-01-01T00:00:00Z");

	private final SaleRepository sales;
	private final ProductRepository products;
	private final CostSettingsRepository costSettings;
	private final AccessoryService accessoryService;
	private final UserRepository users;

	public SaleService(SaleRepository sales, ProductRepository products, CostSettingsRepository costSettings,
			AccessoryService accessoryService, UserRepository users) {
		this.sales = sales;
		this.products = products;
		this.costSettings = costSettings;
		this.accessoryService = accessoryService;
		this.users = users;
	}

	/** username: quien consulta. Un vendedor solo ve sus ventas, sin costos ni ganancias. */
	@Transactional(readOnly = true)
	public List<SaleResponse> list(Instant from, Instant to, String username) {
		User me = currentUser(username);
		Instant f = from == null ? MIN : from;
		Instant t = to == null ? MAX : to;
		if (isSeller(me)) {
			return sales.findBySoldByIgnoreCaseAndSoldAtGreaterThanEqualAndSoldAtLessThanOrderBySoldAtDescIdDesc(
					me.getUsername(), f, t).stream().map(SaleService::toSellerResponse).toList();
		}
		return sales.findBySoldAtGreaterThanEqualAndSoldAtLessThanOrderBySoldAtDescIdDesc(f, t)
				.stream().map(SaleService::toResponse).toList();
	}

	/**
	 * username: quien registra la venta. Un vendedor solo vende productos publicados, a precio de
	 * catálogo y con la fecha de hoy (no puede cambiar precio ni fecha).
	 */
	@Transactional
	public SaleResponse create(SaleRequest req, String username) {
		User me = currentUser(username);
		boolean seller = isSeller(me);
		Product product = products.findById(req.productId())
				.filter(p -> !seller || !p.isHidden())
				.orElseThrow(() -> ApiException.notFound("Producto no encontrado."));
		// Toda venta descuenta del stock: no se puede vender más de lo disponible
		if (req.quantity() > product.getStock()) {
			throw new ApiException(HttpStatus.CONFLICT, product.getStock() == 0
					? "\"" + product.getName() + "\" no tiene stock. Si imprimiste más unidades, súmalas al stock primero."
					: "Solo quedan " + product.getStock() + " unidades de \"" + product.getName() + "\".");
		}
		if (req.soldAt() != null && req.soldAt().isAfter(Instant.now().plusSeconds(24 * 3600))) {
			throw ApiException.badRequest("La fecha de la venta no puede ser futura.");
		}
		CostSettings settings = costSettings.findById(CostSettings.SINGLETON_ID).orElse(null);

		Sale s = new Sale();
		s.setProductId(product.getId());
		s.setProductName(product.getName());
		s.setCategory(product.getCategory());
		s.setColor(blankToNull(req.color()) == null ? null : req.color().toLowerCase());
		s.setQuantity(req.quantity());
		s.setUnitPrice(!seller && req.unitPrice() != null ? req.unitPrice() : product.getPrice());
		s.setCost(settings == null ? null : unitCost(product, settings, accessoryService::costs));
		s.setReservePercent(settings == null ? 0 : settings.getMachineReservePercent());
		s.setStockDiscounted(true);
		s.setChannel(blankToNull(req.channel()));
		s.setCustomer(blankToNull(req.customer()));
		s.setNote(blankToNull(req.note()));
		s.setSoldAt(seller ? null : req.soldAt()); // null = ahora
		s.setSoldBy(me);
		product.setStock(product.getStock() - req.quantity());
		Sale saved = sales.save(s);
		return seller ? toSellerResponse(saved) : toResponse(saved);
	}

	/** Anula una venta. Si había descontado stock y el producto sigue existiendo, lo devuelve. */
	@Transactional
	public void delete(Long id) {
		Sale s = sales.findById(id).orElseThrow(() -> ApiException.notFound("Venta no encontrada."));
		if (s.getClosingId() != null) {
			throw new ApiException(HttpStatus.CONFLICT,
					"Esta venta ya está en una cuadratura. Anula esa cuadratura primero si necesitas corregirla.");
		}
		if (s.isStockDiscounted() && s.getProductId() != null) {
			products.findById(s.getProductId()).ifPresent(p -> p.setStock(p.getStock() + s.getQuantity()));
		}
		sales.delete(s);
	}

	/** Materia prima de una unidad (filamento + accesorios), sin luz. null si el producto no tiene esos datos. */
	static SaleCost unitCost(Product p, CostSettings s, java.util.function.Supplier<java.util.Map<Long, Integer>> accessoryCosts) {
		double grams = p.getGrams() == null ? 0 : p.getGrams();
		Integer acc = AccessoryService.costOf(p, accessoryCosts.get());
		int accessories = acc == null ? 0 : acc;
		if (grams == 0 && accessories == 0) return null;
		double filament = grams * s.getFilamentPrice(p.getMaterial()) / 1000.0;
		return new SaleCost(filament, accessories);
	}

	private User currentUser(String username) {
		return users.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tu usuario ya no existe."));
	}

	private static boolean isSeller(User u) {
		return UserService.ROLE_SELLER.equals(u.getRole());
	}

	/** Lo que ve un vendedor: sin costos, % de máquina ni cuadratura. */
	private static SaleResponse toSellerResponse(Sale s) {
		return new SaleResponse(s.getId(), s.getProductId(), s.getProductName(), s.getCategory(), s.getColor(),
				s.getQuantity(), s.getUnitPrice(), null, null, null, 0, s.isStockDiscounted(),
				s.getChannel(), s.getCustomer(), s.getNote(), null, s.getSoldAt(), s.getCreatedAt(),
				s.getSoldBy(), s.getSoldByName());
	}

	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v.trim();
	}

	private static SaleResponse toResponse(Sale s) {
		return new SaleResponse(s.getId(), s.getProductId(), s.getProductName(), s.getCategory(), s.getColor(),
				s.getQuantity(), s.getUnitPrice(), s.getUnitCost(), s.getUnitFilamentCost(), s.getUnitAccessoriesCost(),
				s.getReservePercent(), s.isStockDiscounted(),
				s.getChannel(), s.getCustomer(), s.getNote(), s.getClosingId(), s.getSoldAt(), s.getCreatedAt(),
				s.getSoldBy(), s.getSoldByName());
	}
}
