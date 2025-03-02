package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemImage;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.tag.ItemTag;
import com.example.demo.entity.tag.Tag;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class EditAuctionController {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ItemImageRepository itemImageRepository;

	@Autowired
	private ItemTagRepository itemTagRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	private final String baseImageDir = "src/main/resources/static/Image/Item/";

	/** ✅ Show Edit Auction Item Page **/
	@GetMapping("/edit-auction/{itemId}")
	public String showEditAuctionForm(@PathVariable("itemId") Long itemId, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {

			return "redirect:/loginPage";
		}
		Optional<Item> itemOptional = itemRepository.findById(itemId);
		if (itemOptional.isEmpty()) {
			return "redirect:/auction-item?error=ItemNotFound";
		}

		Item item = itemOptional.get();
		Auction auction = auctionRepository.findByItem_ItemID(itemId);

		if (auction == null) {
			return "redirect:/auction-item?error=AuctionNotFound";
		}

		// Debugging prints
		System.out.println("Item Found: " + item.getItemName());
		System.out.println("Auction Found: " + auction.getAuctionID());

		// ✅ Fetch all categories and map parent → children → grandchildren
		List<Category> parentCategories = categoryRepository.findByParentCategoryIsNull();
		Map<Category, List<Category>> categoryMap = new HashMap<>();

		for (Category parent : parentCategories) {
			List<Category> children = categoryRepository.findByParentCategory(parent);
			for (Category child : children) {
				List<Category> grandchildren = categoryRepository.findByParentCategory(child);
				child.setSubcategories(grandchildren); // ✅ Set grandchildren inside children
			}
			categoryMap.put(parent, children);
		}

		List<String> itemImages = getItemImages(item);
		String itemTags = getTagsForItem(item);

		model.addAttribute("categoryMap", categoryMap);
		model.addAttribute("categories", parentCategories); // ✅ Send structured categories
		model.addAttribute("item", item);
		model.addAttribute("auction", auction);
		model.addAttribute("itemTags", itemTags);
		model.addAttribute("itemImages", itemImages);

		return "editItemAuction";
	}

	/** ✅ Handle Auction Item Update **/
	@PostMapping("/edit-auction")
	public String updateAuctionItem(@RequestParam("itemId") Long itemId, @RequestParam("name") String name,
			@RequestParam("price") double price, @RequestParam("desc") String description,
			@RequestParam("categoryID") Long categoryID, @RequestParam("cond") String cond, // ✅ Convert manually
			@RequestParam("raising") double raising, @RequestParam("deadline") String deadline,
			@RequestParam(value = "itemImages", required = false) List<MultipartFile> images,
			@RequestParam(value = "tags", required = false) String tags) throws IOException {

		Optional<Item> existingItemOptional = itemRepository.findById(itemId);
		if (existingItemOptional.isEmpty()) {
			return "redirect:/edit-auction?error=ItemNotFound&itemId=" + itemId;
		}

		Item existingItem = existingItemOptional.get();
		Auction auction = auctionRepository.findByItem_ItemID(itemId);

		if (auction == null) {
			return "redirect:/edit-auction?error=AuctionNotFound&itemId=" + itemId;
		}

		// ✅ Update item fields
		existingItem.setItemName(name);
		existingItem.setDescrip(description);
		existingItem.setPrice(price);
		existingItem.setQuality(1); // Auction items are always unique
		existingItem.setUpdatedAt(LocalDateTime.now());

		// ✅ Set Category
		Category category = categoryRepository.findById(categoryID).orElse(null);
		if (category != null) {
			existingItem.setCategory(category);
		}

		// ✅ Convert `cond` (String → Enum)
		try {
			existingItem.setCond(Item.Condition.valueOf(cond.toUpperCase()));
		} catch (IllegalArgumentException e) {
			return "redirect:/edit-auction?error=InvalidCondition&itemId=" + itemId;
		}

		// ✅ Convert `deadline` (String → LocalDateTime)
		try {
			auction.setEndTime(LocalDate.parse(deadline).atStartOfDay().plusHours(23).plusMinutes(59)); // End of the
																										// day
		} catch (Exception e) {
			return "redirect:/edit-auction?error=InvalidDeadline&itemId=" + itemId;
		}

		auction.setIncrementAmount(raising);

		// ✅ Get current image count from database
		List<ItemImage> existingImages = itemImageRepository.findByItem(existingItem);
		int existingImageCount = existingImages.size();
		int newImagesCount = (images != null) ? images.size() : 0;

		// ✅ Check if total images exceed limit (5)
		if (existingImageCount + newImagesCount > 6) {
			return "redirect:/edit-auction?error=TooManyImages&itemId=" + itemId;
		}

		// ✅ Handle Image Upload and Save to Database
		if (newImagesCount > 0) {
			saveItemImages(existingItem, images);
		}

		// ✅ Update Tags
		updateItemTags(existingItem, tags);

		// ✅ Save Updated Item & Auction
		existingItem = itemRepository.save(existingItem);
		auctionRepository.save(auction);

		return "redirect:/edit-auction?itemId=" + itemId + "&success=ItemUpdated";
	}

	// ✅

	/** ✅ Fetch Item Images from File System **/
	private List<String> getItemImages(Item item) {
		String imageDirPath = baseImageDir + item.getItemID();
		Path imageDir = Paths.get(imageDirPath);

		try {
			if (Files.exists(imageDir)) {
				return Files.list(imageDir).filter(Files::isRegularFile)
						.map(path -> "/Image/Item/" + item.getItemID() + "/" + path.getFileName().toString())
						.collect(Collectors.toList());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	/** ✅ Fetch Tags for an Item using `ItemTagRepository` **/
	private String getTagsForItem(Item item) {
		List<ItemTag> itemTags = itemTagRepository.findAll();
		return itemTags.stream().filter(itemTag -> itemTag.getItem().equals(item))
				.map(itemTag -> itemTag.getTag().getName()).collect(Collectors.joining(", "));
	}

	/** ✅ Save Item Images **/
	private void saveItemImages(Item item, List<MultipartFile> images) throws IOException {
		// ✅ Get existing images from database
		List<ItemImage> existingImages = itemImageRepository.findByItem(item);
		int existingImageCount = existingImages.size();
		int newImagesCount = (images != null) ? images.size() : 0;

		// ✅ If no more images can be added, return
		if (existingImageCount >= 5) {
			System.out.println("⚠️ Cannot upload more images. Max limit (5) reached.");
			return;
		}

		// ✅ Calculate how many new images can be added
		int remainingSlots = 5 - existingImageCount;
		List<MultipartFile> allowedImages = images.subList(0, Math.min(newImagesCount, remainingSlots));

		// ✅ Create directory if it doesn't exist
		Path itemPath = Paths.get(baseImageDir + item.getItemID());
		if (!Files.exists(itemPath)) {
			Files.createDirectories(itemPath);
			System.out.println("✅ Created directory: " + itemPath);
		}

		// ✅ Save new images
		for (MultipartFile file : allowedImages) {
			if (!file.isEmpty()) {
				String fileName = file.getOriginalFilename();
				Path filePath = itemPath.resolve(fileName);

				// ✅ Save file physically
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("✅ Image saved to folder: " + filePath);

				// ✅ Save file name to database
				ItemImage image = new ItemImage();
				image.setImagePath(fileName); // ✅ Only store file name in DB
				image.setUploadedDate(LocalDateTime.now());
				image.setItem(item);

				itemImageRepository.save(image); // ✅ Save to DB
				System.out.println("✅ Image saved to DB: " + fileName);
			}
		}
	}

	/** ✅ Delete Existing Item Images **/
	private void deleteExistingItemImages(Item item) {
		List<ItemImage> existingImages = item.getImages();
		if (!existingImages.isEmpty()) {
			for (ItemImage image : existingImages) {
				Path imagePath = Paths.get(baseImageDir + image.getImagePath());
				try {
					Files.deleteIfExists(imagePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			itemImageRepository.deleteAll(existingImages);
		}
	}

	private void updateItemTags(Item item, String tags) {
		if (tags != null && !tags.isEmpty()) {
			// ✅ Remove existing tags before saving new ones
			itemTagRepository.deleteAll(itemTagRepository.findByItem(item));

			// ✅ Convert tags to list
			List<String> tagList = Arrays.stream(tags.split(",")).map(String::trim).filter(tag -> !tag.isEmpty())
					.collect(Collectors.toList());

			List<ItemTag> newItemTags = tagList.stream().map(tagName -> {
				Optional<Tag> optionalTag = tagRepository.findByName(tagName);
				if (optionalTag.isPresent()) {
					ItemTag newItemTag = new ItemTag();
					newItemTag.setItem(item);
					newItemTag.setTag(optionalTag.get());
					return newItemTag;
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());

			itemTagRepository.saveAll(newItemTags); // ✅ Save new tags
		}
	}

}
