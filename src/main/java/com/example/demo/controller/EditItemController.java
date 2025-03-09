package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemImage;
import com.example.demo.entity.User;
import com.example.demo.entity.tag.ItemTag;
import com.example.demo.entity.tag.Tag;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class EditItemController {

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

	private final String baseImageDir = "src/main/resources/static/Image/Item/";

	/** ✅ Show Edit Item Page **/

	@GetMapping("/edit-item/{itemId}")
	public String showEditItemForm(@PathVariable("itemId") Long itemId, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {

			return "redirect:/login";
		}
		Optional<Item> itemOptional = itemRepository.findById(itemId);
		if (itemOptional.isEmpty()) {
			return "redirect:/sell-item?error=ItemNotFound";
		}

		Item item = itemOptional.get();
		Category selectedCategory = item.getCategory(); // ✅ Get selected category

		// ✅ Fetch all categories and map parent → children → grandchildren
		List<Category> parentCategories = categoryRepository.findByParentCategoryIsNull();

		for (Category parent : parentCategories) {
			List<Category> children = categoryRepository.findByParentCategory(parent);
			for (Category child : children) {
				List<Category> grandchildren = categoryRepository.findByParentCategory(child);
				child.setSubcategories(grandchildren); // ✅ Set grandchildren inside children
			}
			parent.setSubcategories(children); // ✅ Set children inside parents
		}

		// ✅ Fetch images for item
		List<String> itemImages = getItemImages(item);

		// ✅ Fetch tags as a comma-separated string
		String itemTags = getTagsForItem(item);

		// ✅ Pass data to Thymeleaf
		model.addAttribute("categories", parentCategories); // ✅ Send structured categories
		model.addAttribute("selectedCategory", selectedCategory); // ✅ Set selected category
		model.addAttribute("item", item);
		model.addAttribute("itemTags", itemTags);
		model.addAttribute("itemImages", itemImages);

		return "editItemNormal"; // ✅ Thymeleaf template
	}

	/** ✅ Handle Item Update **/
	@PostMapping("/edit-normal")
	public String updateItem(@RequestParam("itemId") Long itemId, @RequestParam("name") String name,
			@RequestParam("price") double price, @RequestParam("desc") String description,
			@RequestParam("quality") int quality, @RequestParam("categoryID") Long categoryID,
			@RequestParam("cond") String cond,
			@RequestParam(value = "itemImages", required = false) List<MultipartFile> images,
			@RequestParam(value = "tags", required = false) String tags) throws IOException {

		Optional<Item> existingItemOptional = itemRepository.findById(itemId);
		if (existingItemOptional.isEmpty()) {
			return "redirect:/edit-item?error=ItemNotFound&itemId=" + itemId;
		}

		Item existingItem = existingItemOptional.get();

		// ✅ Update item details
		existingItem.setItemName(name);
		existingItem.setDescrip(description);
		existingItem.setPrice(price);
		existingItem.setQuality(quality);
		existingItem.setUpdatedAt(LocalDateTime.now());

		// ✅ Update category
		Category category = categoryRepository.findById(categoryID).orElse(null);
		if (category != null) {
			existingItem.setCategory(category);
		}

		// ✅ Convert condition safely
		try {
			existingItem.setCond(Item.Condition.valueOf(cond.toUpperCase()));
		} catch (IllegalArgumentException e) {
			return "redirect:/edit-item?error=InvalidCondition&itemId=" + itemId;
		}

		// ✅ Get current image count from database
		List<ItemImage> existingImages = itemImageRepository.findByItem(existingItem);
		int existingImageCount = existingImages.size();
		int newImagesCount = (images != null) ? images.size() : 0;

		// ✅ Check if total images exceed limit (5)
		if (existingImageCount + newImagesCount > 5) {
			return "redirect:/edit-auction?error=TooManyImages&itemId=" + itemId;
		}

		// ✅ Handle Image Upload and Save to Database
		if (newImagesCount > 0) {
			saveItemImages(existingItem, images);
		}

		// ✅ Save item BEFORE images (to ensure ID exists)
		existingItem = itemRepository.save(existingItem);

		// ✅ Update tags
		updateItemTags(existingItem, tags);

		return "redirect:/itemList/" + itemId + "?success=ItemUpdated";

	}

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

	/** ✅ Delete Image from Database and Filesystem **/
	@PostMapping("/delete-item-image")
	@ResponseBody
	public ResponseEntity<String> deleteItemImage(@RequestBody Map<String, String> request) {
		String fileName = request.get("imagePath"); // Now it contains only file name
		Long itemId = request.containsKey("itemId") ? Long.parseLong(request.get("itemId")) : null;

		if (fileName == null || fileName.isEmpty()) {
			return ResponseEntity.badRequest().body("Invalid file name");
		}

		System.out.println(
				"🟡 Attempting to delete image: " + fileName + (itemId != null ? " for itemId: " + itemId : ""));

		// ✅ Find the image in the database by file name (optionally filter by itemId)
		Optional<ItemImage> imageOptional = itemImageRepository.findAll().stream()
				.filter(img -> img.getImagePath().endsWith(fileName)) // ✅ Match by file name only
				.filter(img -> itemId == null || img.getItem().getItemID().equals(itemId)) // ✅ Optional itemId check
				.findFirst();

		if (imageOptional.isPresent()) {
			ItemImage image = imageOptional.get();

			// ✅ Remove from database
			itemImageRepository.delete(image);
			System.out.println("✅ Image deleted from database: " + fileName);

			// ✅ Construct full path from stored item's folder
			Path filePath = Paths.get("src/main/resources/static/Image/Item/" + image.getItem().getItemID(), fileName);
			try {
				if (Files.exists(filePath)) {
					Files.delete(filePath);
					System.out.println("✅ Image deleted from filesystem: " + filePath);
				} else {
					System.out.println("⚠️ File not found in filesystem: " + filePath);
				}
				return ResponseEntity.ok("Image deleted successfully");
			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Failed to delete image from filesystem");
			}
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found in database");
		}
	}

	/** ✅ Fetch Tags for an Item using ItemTagRepository **/
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
		if (tags != null && !tags.trim().isEmpty()) {
			System.out.println("🟡 Updating tags for item: " + item.getItemID());

			// ✅ Remove existing tags ONLY IF there are new tags to replace them
			List<ItemTag> existingTags = itemTagRepository.findByItem(item);
			if (!existingTags.isEmpty()) {
				System.out.println("🟠 Deleting existing tags...");
				itemTagRepository.deleteAll(existingTags);
			}

			// ✅ Convert tags to a cleaned list
			List<String> tagList = Arrays.stream(tags.split(",")).map(String::trim) // ✅ Remove spaces
					.filter(tag -> !tag.isEmpty()) // ✅ Remove empty tags
					.collect(Collectors.toList());

			List<ItemTag> newItemTags = new ArrayList<>();

			for (String tagName : tagList) {
				Tag tag = tagRepository.findByName(tagName).orElse(null);

				// ✅ If tag does not exist, create it
				if (tag == null) {
					tag = new Tag();
					tag.setName(tagName);
					tagRepository.save(tag); // ✅ Save new tag to DB
					System.out.println("✅ Created new tag: " + tagName);
				}

				// ✅ Create ItemTag relation
				ItemTag newItemTag = new ItemTag();
				newItemTag.setItem(item);
				newItemTag.setTag(tag);
				newItemTags.add(newItemTag);
			}

			// ✅ Save all new item tags in one batch
			if (!newItemTags.isEmpty()) {
				itemTagRepository.saveAll(newItemTags);
				System.out.println("✅ Saved new tags for item: " + item.getItemID());
			}
		} else {
			System.out.println("⚠️ No tags provided, skipping update.");
		}
	}
}