package com.atserc.tutorialStore.controllers;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.atserc.tutorialStore.models.*;
import com.atserc.tutorialStore.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {
	
	@Autowired
	private ProductsRepository repo;
	
	@GetMapping({"", "/"})
	public String showProductList(Model model) {
		List<Product> products = repo.findAll();
		model.addAttribute("products", products);
		return "products/index";
	}
	
	@GetMapping({"/create"})
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/CreateProduct";
	}
	
	@PostMapping("/create")
	public String createProduct(
			@Valid @ModelAttribute ProductDto productDto,
			BindingResult result
			) {
		if(productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}
		
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		
		//save image file
		MultipartFile image = productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			
			try (InputStream inputStream = image.getInputStream()) {
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
			
		} catch (Exception ex) {
			System.out.println("Exception" + ex.getMessage());
		}
		
		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(createdAt);
		product.setImageFileName(storageFileName);
		
		repo.save(product);
		
		
		return "redirect:/products";
	}
	
	
	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id) {
		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			
			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());
			
			model.addAttribute("productDto",productDto);
			
			
		} catch (Exception ex) {
			System.out.println("Exception" + ex.getMessage());
			return "redirect:/products";
		}
		
		return "products/EditProduct";
	}
	
	@PostMapping("/edit")
	public String updatreProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {
		
		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			
			if(result.hasErrors()) {
				return "products/EditProduct";
			}
			
			if(!productDto.getImageFile().isEmpty()) {
				//delete old
				String uploadDir = "public/images/";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
				
				try {
					Files.delete(oldImagePath);
				} catch (Exception ex) {
					System.out.println("Exception" + ex.getMessage());
				}
				
				//save new
				MultipartFile image = productDto.getImageFile();
				Date createdAt = new Date();
				String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
				
				try (InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
				}
				
				product.setImageFileName(storageFileName);
			}
			
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			
			repo.save(product);
			
			
		} catch (Exception ex) {
			System.out.println("Exception" + ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {
		try {
			Product product = repo.findById(id).get();
			
			//delete image
			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
			try {
				Files.delete(imagePath);
			} catch (Exception ex) {
				System.out.println("Exception" + ex.getMessage());
			}
			
			repo.delete(product);
			
		} catch (Exception ex) {
			System.out.println("Exception" + ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
}