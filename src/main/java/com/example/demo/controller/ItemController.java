package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

import org.springframework.ui.Model;

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
	

	@GetMapping("/sell-item")
	public String showSellItemForm(Model model) {
	    // Fetch all top-level categories (where parentCategory is NULL)
	    List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();

	    // Fetch all subcategories mapped by parent category
	    Map<Category, List<Category>> categoryMap = parentCategories.stream()
	            .collect(Collectors.toMap(
	                    parent -> parent,
	                    parent -> categoryRepo.findByParentCategory(parent)
	            ));

	    // Add to model
	    model.addAttribute("categoryMap", categoryMap);
	    return "sellItemNormal";
	}
	

	@GetMapping("/itemList")
	public String viewItems(Model model) {
		List<Item> itemList = itemRepo.findAll();
		List<Auction> auctionList = auctionRepo.findAll();
		model.addAttribute("itemList", itemList);
		model.addAttribute("auctionList", auctionList);
		
		return "itemList";
		
	}
	
	@GetMapping("itemList/{itemID}")
	public String viewItem(@PathVariable("itemID") Long itemID, Model model) {
		Item item = itemRepo.getReferenceById(itemID);
		Auction auction = auctionRepo.findByItem_ItemID(itemID);
		
		Long auctionID = (auction != null) ? auction.getAuctionID() : null;
		int AuctionCount = auctionTrackRepo.countByAuction_AuctionID(auctionID);
		Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auctionID);
		
		//tags

		List<String> tagNames = itemRepo.findTagNamesByItemId(itemID);
		String tagOutput = String.join(",", tagNames);

		
		model.addAttribute("item", item);
		model.addAttribute("auction", auction);
		
		model.addAttribute("auctionCount",AuctionCount);
		model.addAttribute("maxBid",maxBid);
		
		model.addAttribute("tagOutput", tagOutput);
		return "viewSale";
	}
	
	
	
	
	private final String BASE_DIR = "src/main/resources/static/Image/Item/";

    @PostMapping("/sell-item")
    public String saveItem(
            @RequestParam("name") String name,
            @RequestParam("desc") String desc,
            @RequestParam("price") double price,
            @RequestParam("stock") int stock,
            @RequestParam("cond") String cond,
            @RequestParam("categoryID") Long categoryID,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "itemImages", required = false) List<MultipartFile> images
    ) throws IOException {

        System.out.println("➡ Item Name: " + name);
        System.out.println("➡ Stock: " + stock);

        // ✅ Create new Item
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

        // ✅ Set category
        item.setCategory(categoryRepo.findById(categoryID)
                .orElseThrow(() -> new RuntimeException("❌ Category ID not found: " + categoryID)));

        // ✅ Assign a static seller for now (Replace with logged-in user later)
        User seller = userRepo.findById(1L)
                .orElseThrow(() -> new RuntimeException("❌ Seller ID not found"));
        item.setSeller(seller);

        // ✅ Save Item first
        item = itemRepo.save(item);

        // ✅ Save Images
        if (images != null && !images.isEmpty()) {
            saveItemImages(item, images);
        }else {
        	System.out.print("Images is empty");
        }

        // ✅ Save Tags (if any)
        if (tags != null && !tags.isEmpty()) {
            saveItemTags(item, tags);
        }

        return "redirect:/itemList";
    }
    
    //Auctions
    
	@GetMapping("/auction-item")
	public String showSellAuctionForm(Model model) {
	    // Fetch all top-level categories (where parentCategory is NULL)
	    List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();

	    // Fetch all subcategories mapped by parent category
	    Map<Category, List<Category>> categoryMap = parentCategories.stream()
	            .collect(Collectors.toMap(
	                    parent -> parent,
	                    parent -> categoryRepo.findByParentCategory(parent)
	            ));

	    // Add to model
	    model.addAttribute("categoryMap", categoryMap);
	    return "sellItemAuction";
	}
	
	@PostMapping("/auction-item")
	public String saveAuction(
	        @RequestParam("name") String name,
	        @RequestParam("desc") String desc,
	        @RequestParam("price") double price, // Start price
	        @RequestParam("raising") double raising, // Minimum bid increment
	        @RequestParam("deadline") String deadline, // Auction end date (String for conversion)
	        @RequestParam("categoryID") Long categoryID,
	        @RequestParam("cond") String cond,
	        @RequestParam(value = "tags", required = false) String tags,
	        @RequestParam(value = "itemImages", required = false) List<MultipartFile> images
	) throws IOException {
	    
	    System.out.println("➡ Auction Item: " + name);
	    System.out.println("➡ Start Price: " + price);
	    System.out.println("➡ Raising Amount: " + raising);
	    System.out.println("➡ Deadline: " + deadline);

	    // ✅ Create & Save Item first
	    Item item = new Item();
	    item.setItemName(name);
	    item.setDescrip(desc);
	    item.setPrice(price);
	    item.setQuality(1); // Auction items are always unique
	    item.setSellType(Item.SellType.AUCTION);
	    item.setCond(Item.Condition.valueOf(cond.toUpperCase()));
	    item.setCreatedAt(LocalDateTime.now());
	    item.setUpdatedAt(LocalDateTime.now());
	    item.setStat(Item.Status.AVAILABLE);

	    // ✅ Set category
	    item.setCategory(categoryRepo.findById(categoryID)
	            .orElseThrow(() -> new RuntimeException("❌ Category ID not found: " + categoryID)));

	    // ✅ Assign seller (Currently static, replace with logged-in user later)
	    User seller = userRepo.findById(1L)
	            .orElseThrow(() -> new RuntimeException("❌ Seller ID not found"));
	    item.setSeller(seller);

	    // ✅ Save Item first
	    item = itemRepo.save(item);
	    System.out.println("✅ Item saved with ID: " + item.getItemID());

	    // ✅ Convert deadline to LocalDateTime
	    LocalDateTime auctionEndTime = LocalDate.parse(deadline).atStartOfDay().plusHours(23).plusMinutes(59);
	    
	    // ✅ Create & Save Auction
	    Auction auction = new Auction();
	    auction.setItem(item);
	    auction.setStartPrice(price);
	    auction.setIncrementAmount(raising);
	    auction.setStat(Auction.AuctionStatus.ACTIVE);
	    auction.setCreatedAt(LocalDateTime.now());
	    auction.setEndTime(auctionEndTime);

	    auction = auctionRepo.save(auction);
	    System.out.println("✅ Auction saved with ID: " + auction.getAuctionID());

	    // ✅ Save Images
	    if (images != null && !images.isEmpty()) {
	        System.out.println("➡ Images received: " + images.size());
	        saveItemImages(item, images);
	    } else {
	        System.out.println("⚠️ No images uploaded for this auction.");
	    }

	    // ✅ Save Tags (if any)
	    if (tags != null && !tags.isEmpty()) {
	        saveItemTags(item, tags);
	    }

	    return "redirect:/itemList";
	}

    
    
    //methods

    private void saveItemImages(Item item, List<MultipartFile> images) throws IOException {
        Path itemPath = Paths.get(BASE_DIR + item.getItemID());
System.out.print("I am at save item img");
        if (!Files.exists(itemPath)) {
            Files.createDirectories(itemPath);
            System.out.println("✅ Created directory: " + itemPath.toString());
        }

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String fileName =  file.getOriginalFilename();
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
        Set<String> tagNames = new HashSet<>(List.of(tags.split(",")))
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        for (String tagName : tagNames) {
            Tag tag = tagRepo.findByName(tagName)
                    .orElseGet(() -> {
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

}
