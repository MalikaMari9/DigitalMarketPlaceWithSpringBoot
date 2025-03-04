package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.AdminNotification;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.Item.ApprovalStatus;
import com.example.demo.entity.ItemApproval;
import com.example.demo.entity.ItemImage;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.tag.ItemTag;
import com.example.demo.entity.tag.Tag;
import com.example.demo.repository.AdminNotificationRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ViewRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {
	@Autowired
	ItemRepository itemRepo;

	@Autowired
	CategoryRepository categoryRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	AuctionRepository auctionRepo;
	@Autowired
	AuctionTrackRepository auctionTrackRepo;
	@Autowired
	ItemImageRepository itemImageRepo;
	@Autowired
	TagRepository tagRepo;
	@Autowired
	ItemTagRepository itemTagRepo;
	@Autowired
	ViewRepository viewRepo;
	@Autowired
	ItemApprovalRepository itemApprovalRepo;
	@Autowired
	NotificationRepository notificationRepo;
	@Autowired
	WishlistRepository wishlistRepo;
	@Autowired
	AdminNotificationRepository adminNotificationRepo;
	@Autowired
	SellerRepository sellerRepo;

	@Autowired
	CartRepository cartRepo;

	@GetMapping("/sell-item")
	public String showSellItemForm(Model model, HttpSession session) {
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage"; // Redirect if not logged in
		}
		List<Category> categories = categoryRepo.findByParentCategoryIsNull();
		model.addAttribute("categories", categories);
		return "sellItemNormal";
	}

	// Not necessarily needed since we already got other pages
	@GetMapping("/itemList")
	public String viewItems(Model model) {
		List<Item> itemList = itemRepo.findAll();
		List<Auction> auctionList = auctionRepo.findAll();
		Map<Long, Double> maxBids = new HashMap<>();
		for (Auction auction : auctionList) {
			Long auctionID = auction.getAuctionID();
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auctionID);
			maxBids.put(auctionID, maxBid != null ? maxBid : auction.getStartPrice()); // Default to startPrice if no
																						// bids
		}
		model.addAttribute("itemList", itemList);
		model.addAttribute("auctionList", auctionList);
		model.addAttribute("maxBids", maxBids); // Pass map of max bids

		return "itemList";

	}

	@GetMapping("itemList/{itemID}")
	public String viewItem(@PathVariable("itemID") Long itemID, Model model, HttpSession session) {
		Item item = itemRepo.getReferenceById(itemID);
		Auction auction = auctionRepo.findByItem_ItemID(itemID);

		Long auctionID = (auction != null) ? auction.getAuctionID() : null;
		int AuctionCount = auctionTrackRepo.countByAuction_AuctionID(auctionID);
		Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auctionID);

		// Ensure tagOutput is always a valid String (avoid null pointer issues)
		List<String> tagNames = itemRepo.findTagNamesByItemId(itemID);
		String tagOutput = (tagNames != null && !tagNames.isEmpty()) ? String.join(",", tagNames) : "";

		model.addAttribute("tagOutput", tagOutput);

		// ✅ Retrieve user from session
		User user = (User) session.getAttribute("user");

		// ✅ Check if user is an admin or seller
		Boolean isAdmin = (Boolean) session.getAttribute("admin");
		boolean isSeller = (user != null && "SELLER".equals(user.getRole()));

		model.addAttribute("isAdmin", isAdmin != null && isAdmin);
		model.addAttribute("isSeller", isSeller);

		// ✅ Fetch recommendations only for buyers (not for admins or sellers)
		List<Item> recommendedItems = new ArrayList<>();
		List<Long> wishlistedItemIds = new ArrayList<>();

		if (user != null && !isSeller && (isAdmin == null || !isAdmin)) {
			List<Item> byCategory = itemRepo.findRecommendedItemsByCategory(user.getUserID());
			List<Item> byTag = itemRepo.findRecommendedItemsByTag(user.getUserID());

			List<Item> cartItems = cartRepo.findByUser(user).stream().map(Cart::getItem).collect(Collectors.toList());

			// ✅ Remove items already in cart
			byCategory.removeAll(cartItems);
			byTag.removeAll(cartItems);

			// ✅ Add unique recommended items
			Set<Item> recommendedSet = new LinkedHashSet<>();
			recommendedSet.addAll(byCategory);
			recommendedSet.addAll(byTag);

			// ✅ If no recommendations found, show latest items
			if (recommendedSet.isEmpty()) {
				recommendedSet.addAll(itemRepo.findLatestItems(PageRequest.of(0, 8)).getContent());
			}

			recommendedItems.addAll(recommendedSet);
			wishlistedItemIds = wishlistRepo.findItemIdsByUser(user.getUserID());
		}

		model.addAttribute("recommendedItems", recommendedItems);
		model.addAttribute("wishlistedItemIds", wishlistedItemIds);

		model.addAttribute("item", item);
		model.addAttribute("auction", auction);
		model.addAttribute("auctionCount", AuctionCount);
		model.addAttribute("maxBid", maxBid);

		return "viewSale";
	}

	private final String BASE_DIR = "src/main/resources/static/Image/Item/";

	@PostMapping("/sell-item")
	public String saveItem(@RequestParam("name") String name, @RequestParam("desc") String desc,
			@RequestParam("price") double price, @RequestParam("stock") int stock, @RequestParam("cond") String cond,
			@RequestParam("categoryID") Long categoryID, @RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "itemImages", required = false) List<MultipartFile> images, HttpSession session)
			throws IOException {

		System.out.println("➡ Item Name: " + name);
		System.out.println("➡ Stock: " + stock);
		System.out.println("Seller:" + session.getId());

		// ✅ Get logged-in user
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/loginPage"; // Redirect if not logged in
		}

		// ✅ Ensure the user is a seller
		if (!"SELLER".equals(seller.getRole())) {
			return "redirect:/unauthorized"; // Redirect if not a seller
		}

		// ✅ Set up Item
		Item item = new Item();
		item.setItemName(name);
		item.setDescrip(desc);
		item.setPrice(price);
		item.setQuality(stock);
		item.setSellType(Item.SellType.NORMAL); // Defaulting to normal for now
		item.setCond(Item.Condition.valueOf(cond.toUpperCase()));
		item.setCreatedAt(LocalDateTime.now());
		item.setUpdatedAt(LocalDateTime.now());
		item.setStat(Item.Status.AVAILABLE);
		item.setApprove(ApprovalStatus.PENDING);
		item.setSeller(seller);

		// ✅ Set category
		item.setCategory(categoryRepo.findById(categoryID)
				.orElseThrow(() -> new RuntimeException("❌ Category ID not found: " + categoryID)));

		// ✅ Save Item first
		item = itemRepo.save(item);

		// ✅ Save item Approval
		ItemApproval itemApproval = new ItemApproval();
		itemApproval.setItem(item);
		itemApproval.setApprovalDate(null);
		itemApproval.setRejectionReason(null);
		itemApprovalRepo.save(itemApproval);

		// ✅ Validate Image Size Before Saving
		if (images != null && !images.isEmpty()) {
			final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
			for (MultipartFile image : images) {
				if (image.getSize() > MAX_FILE_SIZE) {
					throw new RuntimeException(
							"❌ Image " + image.getOriginalFilename() + " exceeds the 5MB size limit.");
				}
			}
			saveItemImages(item, images);
		} else {
			System.out.print("Images is empty");
		}

		// ✅ Save Tags (if any)
		if (tags != null && !tags.isEmpty()) {
			saveItemTags(item, tags);
		}

		// ✅ Notify Admin
		AdminNotification notification = new AdminNotification();
		notification.setType(AdminNotification.NotificationType.ITEM_APPROVAL);
		notification.setMessage("New item '" + name + "' awaiting approval.");
		notification.setStatus(AdminNotification.NotificationStatus.UNREAD);
		adminNotificationRepo.save(notification);

		return "redirect:/pending-sale?searchfield=&sortby=itemID";
	}

	// Auctions

	@GetMapping("/auction-item")
	public String showSellAuctionForm(Model model, HttpSession session) {

		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/loginPage"; // Redirect if not logged in
		}

		// Fetch all top-level categories (where parentCategory is NULL)
		List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();

		// Fetch all subcategories mapped by parent category
		Map<Category, List<Category>> categoryMap = parentCategories.stream()
				.collect(Collectors.toMap(parent -> parent, parent -> categoryRepo.findByParentCategory(parent)));

		// Add to model
		model.addAttribute("categoryMap", categoryMap);
		return "sellItemAuction";
	}

	@PostMapping("/auction-item")
	public String saveAuction(@RequestParam("name") String name, @RequestParam("desc") String desc,
			@RequestParam("price") double price, @RequestParam("raising") double raising,
			@RequestParam("createdDate") String createdDate, // ✅ New param
			@RequestParam("deadline") String deadline, @RequestParam("categoryID") Long categoryID,
			@RequestParam("cond") String cond, @RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "itemImages", required = false) List<MultipartFile> images, HttpSession session)
			throws IOException {

		System.out.println("➡ Auction Item: " + name);
		System.out.println("➡ Start Price: " + price);
		System.out.println("➡ Raising Amount: " + raising);
		System.out.println("➡ Created Date: " + createdDate);
		System.out.println("➡ Deadline: " + deadline);

		// ✅ Get logged-in user
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage";
		}

		// ✅ Ensure the user is a seller
		if (!"SELLER".equals(seller.getRole())) {
			return "redirect:/unauthorized";
		}

		// ✅ Convert createdDate and deadline
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime createdDateTime;
		LocalDateTime auctionEndTime;

		try {
			createdDateTime = LocalDate.parse(createdDate, formatter).atStartOfDay();
			auctionEndTime = LocalDate.parse(deadline, formatter).atStartOfDay().plusHours(23).plusMinutes(59);
		} catch (DateTimeParseException e) {
			return "redirect:/auction-item?error=InvalidDateFormat";
		}

		// ✅ Ensure createdDate is at most 3 days from today
		LocalDateTime maxAllowedDate = LocalDateTime.now().plusDays(5);
		if (createdDateTime.isAfter(maxAllowedDate)) {
			return "redirect:/auction-item?error=CreatedDateTooFar";
		}

		// ✅ Ensure createdDate is before deadline
		if (createdDateTime.isAfter(auctionEndTime)) {
			return "redirect:/auction-item?error=InvalidDateRange";
		}

		// ✅ Create & Save Item
		Item item = new Item();
		item.setItemName(name);
		item.setDescrip(desc);
		item.setPrice(price);
		item.setQuality(1);
		item.setSellType(Item.SellType.AUCTION);
		item.setCond(Item.Condition.valueOf(cond.toUpperCase()));
		item.setCreatedAt(createdDateTime);
		item.setUpdatedAt(LocalDateTime.now());
		item.setStat(Item.Status.AVAILABLE);
		item.setApprove(ApprovalStatus.PENDING);
		item.setSeller(seller);

		// ✅ Set category
		item.setCategory(categoryRepo.findById(categoryID)
				.orElseThrow(() -> new RuntimeException("❌ Category ID not found: " + categoryID)));

		// ✅ Save Item first
		item = itemRepo.save(item);
		System.out.println("✅ Item saved with ID: " + item.getItemID());

		// ✅ Save Approval Status
		ItemApproval itemApproval = new ItemApproval();
		itemApproval.setItem(item);
		itemApproval.setApprovalDate(null);
		itemApproval.setRejectionReason(null);
		itemApprovalRepo.save(itemApproval);

		// ✅ Create & Save Auction
		Auction auction = new Auction();
		auction.setItem(item);
		auction.setStartPrice(price);
		auction.setIncrementAmount(raising);
		auction.setStat(Auction.AuctionStatus.ACTIVE);
		auction.setCreatedAt(createdDateTime); // ✅ Store createdDate
		auction.setEndTime(auctionEndTime);

		auction = auctionRepo.save(auction);
		System.out.println("✅ Auction saved with ID: " + auction.getAuctionID());

		// ✅ Save Images
		if (images != null && !images.isEmpty()) {
			saveItemImages(item, images);
		}

		// ✅ Save Tags (if any)
		if (tags != null && !tags.isEmpty()) {
			saveItemTags(item, tags);
		}

		// ✅ Notify Admin
		AdminNotification notification = new AdminNotification();
		notification.setType(AdminNotification.NotificationType.ITEM_APPROVAL);
		notification.setMessage("New auction item '" + name + "' awaiting approval.");
		notification.setStatus(AdminNotification.NotificationStatus.UNREAD);
		adminNotificationRepo.save(notification);

		return "redirect:/pending-sale?searchfield=&sortby=itemID";
	}

	// Search Header
	@ModelAttribute("categories")
	public List<Category> loadCategories() {
		List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();
		parentCategories.forEach(this::loadSubcategories);
		return parentCategories;
	}

	@GetMapping("/search")
	public String searchItems(@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "categoryID", required = false) Long categoryID, HttpSession session, // ✅ Add session
																										// to check
																										// logged-in
																										// user
			Model model) {

		List<Item> searchResults;

		if (categoryID != null && query != null) {
			searchResults = itemRepo.searchItemsByCategory(query, categoryID);
		} else if (categoryID != null) {
			searchResults = itemRepo.findItemsByCategory(categoryID);
		} else if (query != null) {
			searchResults = itemRepo.searchItems(query);
		} else {
			searchResults = itemRepo.findAll();
		}

		List<Auction> auctionResults = auctionRepo.findAllByItemIn(searchResults);

		// ✅ Fetch category name if category is selected
		Category selectedCategory = categoryID != null ? categoryRepo.findById(categoryID).orElse(null) : null;

		// ✅ Fetch highest bids for auction items
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getAuctionID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Fetch wishlisted item IDs for the logged-in user
		User user = (User) session.getAttribute("user");
		List<Long> wishlistedItemIds = new ArrayList<>(); // Default empty list for guests

		if (user != null) {
			wishlistedItemIds = wishlistRepo.findWishlistedItemIdsByUser(user);
		}

		// ✅ Add data to the model
		model.addAttribute("searchResults", searchResults);
		model.addAttribute("auctionResults", auctionResults);
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("query", query);
		model.addAttribute("categoryID", categoryID);
		model.addAttribute("selectedCategory", selectedCategory);
		model.addAttribute("wishlistedItemIds", wishlistedItemIds); // ✅ Pass wishlist data to Thymeleaf

		return "searchResults";
	}

	@GetMapping("/category-search")
	public String categorySearch(@RequestParam("categoryID") Long categoryID, Model model, HttpSession session) {
		List<Item> searchResults = itemRepo.findItemsByCategory(categoryID);
		List<Auction> auctionResults = auctionRepo.findAllByItemIn(searchResults);

		// ✅ Fetch the selected category name
		Category selectedCategory = categoryRepo.findById(categoryID).orElse(null);

		// ✅ Fetch highest bids for auction items
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getAuctionID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Fetch wishlisted items for the logged-in user
		User user = (User) session.getAttribute("user");
		List<Long> wishlistedItemIds = new ArrayList<>();
		if (user != null) {
			wishlistedItemIds = wishlistRepo.findItemIdsByUser(user.getUserID());
		}

		// ✅ Add data to the model
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("searchResults", searchResults);
		model.addAttribute("auctionResults", auctionResults);
		model.addAttribute("categoryID", categoryID);
		model.addAttribute("selectedCategory", selectedCategory);
		model.addAttribute("wishlistedItemIds", wishlistedItemIds); // ✅ Pass wishlist items

		return "searchResults";
	}

	// Recommendations in HOME
	// ModelAttribute wasnt working if we call it so I had to forcced it from home
	// on dmpcontroller
	@ModelAttribute("recommendedItems")
	public List<Item> getRecommendations(HttpSession session) {
		User user = (User) session.getAttribute("user");
		List<Item> recommendedItems = new ArrayList<>();

		if (user != null) {
			System.out.println("Fetching recommendations for user ID: " + user.getUserID());

			List<Item> byCategory = itemRepo.findRecommendedItemsByCategory(user.getUserID());
			List<Item> byTag = itemRepo.findRecommendedItemsByTag(user.getUserID());

			System.out.println("Category-based recommendations: " + byCategory.size());
			System.out.println("Tag-based recommendations: " + byTag.size());

			recommendedItems.addAll(byCategory);
			recommendedItems.addAll(byTag);
		}

		return recommendedItems;
	}

	// Delete

	@DeleteMapping("/delete/{itemId}")
	@Transactional // ✅ Ensures everything is part of a transaction
	public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
		Optional<Item> itemOptional = itemRepo.findById(itemId);

		if (itemOptional.isPresent()) {
			Item item = itemOptional.get();

			try {
				System.out.println("🔹 Deleting item with ID: " + itemId);

				// ✅ Delete Related Notifications (notification_tbl)
				notificationRepo.deleteAllByItem(item);

				// ✅ Delete Related Views (view_tbl)
				viewRepo.deleteAll(viewRepo.findByItem(item));

				// ✅ Delete Item Approval Records (item_approval_tbl)
				itemApprovalRepo.deleteByItem(item);

				// ✅ Delete Related Item Tags
				itemTagRepo.deleteAll(itemTagRepo.findByItem(item));

				// ✅ Delete Related Item Images from Database and Filesystem
				deleteItemImages(item);

				// ✅ Delete Auction and Auction Tracks if the item was an auction
				Auction auction = auctionRepo.findByItem_ItemID(itemId);
				if (auction != null) {
					auctionTrackRepo.deleteAll(auctionTrackRepo.findByAuction(auction));
					auctionRepo.delete(auction);
				}

				// ✅ Delete Item from Database
				itemRepo.delete(item);

				return ResponseEntity.ok("✅ Item deleted successfully.");

			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(500).body("❌ Failed to delete item: " + e.getMessage());
			}
		} else {
			return ResponseEntity.status(404).body("❌ Item not found.");
		}
	}
	// Edit

	// methods

	private void saveItemImages(Item item, List<MultipartFile> images) throws IOException {
		Path itemPath = Paths.get(BASE_DIR + item.getItemID());
		System.out.print("I am at save item img");
		if (!Files.exists(itemPath)) {
			Files.createDirectories(itemPath);
			System.out.println("✅ Created directory: " + itemPath.toString());
		}

		for (MultipartFile file : images) {
			if (!file.isEmpty()) {
				String fileName = file.getOriginalFilename();
				Path filePath = itemPath.resolve(fileName);

				// ✅ Save file physically
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("✅ Image saved to folder: " + filePath.toString());

				// ✅ Now save file name to database
				ItemImage image = new ItemImage();
				image.setImagePath(fileName); // ✅ Only store file name
				image.setUploadedDate(LocalDateTime.now());
				image.setItem(item);

				itemImageRepo.save(image); // ✅ Save image entry in DB
				System.out.println("✅ Image saved to DB: " + fileName);
			}
		}
	}

	private void saveItemTags(Item item, String tags) {
		Set<String> tagNames = new HashSet<>(List.of(tags.split(","))).stream().map(String::trim)
				.filter(tag -> !tag.isEmpty()).collect(Collectors.toSet());

		for (String tagName : tagNames) {
			Tag tag = tagRepo.findByName(tagName).orElseGet(() -> {
				Tag newTag = new Tag();
				newTag.setName(tagName);
				return tagRepo.save(newTag);
			});

			// Check if item already has the tag
			boolean exists = itemTagRepo.existsByItemAndTag(item, tag);
			if (!exists) {
				ItemTag itemTag = new ItemTag();
				itemTag.setItem(item);
				itemTag.setTag(tag);
				itemTagRepo.save(itemTag);
			}
		}

	}

	private void loadSubcategories(Category category) {
		List<Category> subcategories = categoryRepo.findByParentCategory(category);
		subcategories.forEach(this::loadSubcategories); // Recursive call to load deeper levels
		category.setSubcategories(subcategories);
	}

	private void deleteItemImages(Item item) {
		List<ItemImage> images = itemImageRepo.findByItem(item);

		if (!images.isEmpty()) {
			for (ItemImage image : images) {
				Path imagePath = Paths.get(BASE_DIR + item.getItemID(), image.getImagePath());

				try {
					Files.deleteIfExists(imagePath); // ✅ Delete from filesystem
				} catch (IOException e) {
					System.err.println("❌ Failed to delete image file: " + imagePath);
					e.printStackTrace();
				}
			}
			itemImageRepo.deleteAll(images); // ✅ Delete from database
		}
	}

}
