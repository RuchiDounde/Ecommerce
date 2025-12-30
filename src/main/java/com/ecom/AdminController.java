package com.ecom;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpSession;
//here we call the methods
@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
    private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@GetMapping("/")
	public String index()
	{
		return"admin/index";
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(org.springframework.ui.Model m)
	{
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return"admin/add";
	}
	
	@GetMapping("/category")//here load all category page
	public String category(org.springframework.ui.Model m)
	{
		m.addAttribute("categorys", categoryService.getAllCategory());
		return"admin/category";
	}

	
	//when we not doing redirect old object is save
	//redirect is used to remove the old data and new data save
	//for security purpose we use postMapping
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, 
			HttpSession session) throws IOException//HttpSession is used for display the msg
	{
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);//used to add the image
		
		Boolean existCategory = categoryService.existCategory(category.getName());
		
		if (existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		}
		else {
			Category saveCategory = categoryService.saveCategory(category);
			
			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Not saved ! internal server error");
			}else {
				
				File savefile=new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+"LatestProduct" + File.separator+file.getOriginalFilename());
				
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("successMsg", "Saved Successfully");
			}
		}
		
		return "redirect:/admin/category";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session){
		Boolean deleteCategory = categoryService.deleteCategory(id);
		
		if (deleteCategory) {
			session.setAttribute("successMsg", "category delete success");
		}else {
			session.setAttribute("errorMsg", "something wrong on server");
		}
		return "redirect:/admin/category";
	}
	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, org.springframework.ui.Model m)
	{
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "/admin/edit";
	}
	
	@PostMapping("/updateCategory")
	public String UpdateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		Category oldCategory=categoryService.getCategoryById(category.getId());
		String imageName=file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
		
		if (!ObjectUtils.isEmpty(category)) {
			
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}
		
		Category updateCategory = categoryService.saveCategory(oldCategory);
		if (!ObjectUtils.isEmpty(updateCategory)) 
		{
			
			if (!file.isEmpty()) 
			{
                 File savefile=new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+"LatestProduct" + File.separator+file.getOriginalFilename());
				
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("successMsg", "Category update success");
		}else {
			session.setAttribute("errorMsg", "something wrong on server");
		}
		return "redirect:/admin/loadEditCategory/" + category.getId();
		
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) throws IOException {//@RequestParam("file")MultipartFile  image---we get the image
		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
				Product saveProduct = productService.saveProduct(product);
		
		if (!ObjectUtils.isEmpty(saveProduct)) {
			
			 File savefile=new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+"product_img" + File.separator+image.getOriginalFilename());
				
				//System.out.println(path);
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			session.setAttribute("successMsg", "Product saved Success");
		}else {
			session.setAttribute("errorMsg", "Something wrong on Server");
		}
		return "redirect:/admin/loadAddProduct";
		
	}
	  
	@GetMapping("/products")
	  public String loadViewProduct(org.springframework.ui.Model m) {//addAtribute is used for see in UI
		m.addAttribute("products", productService.getAllProducts());
		  return"admin/products";
	  }
	
	@GetMapping("/deleteProduct/{id}")
	  public String deleteProduct(@PathVariable int id, HttpSession session) {
	   Boolean deleteProduct = productService.deleteProduct(id);
	   if (deleteProduct) {
		session.setAttribute("successMsg", "Product delete Success");
	}else {
		session.setAttribute("errorMsg", "Something wrong on server");
	}
		  return"redirect:/admin/products";
	  }
	
	@GetMapping("/editProduct/{id}")
	  public String editProduct(@PathVariable int id, org.springframework.ui.Model m) {
	  m.addAttribute("product", productService.getProductById(id));
	  m.addAttribute("categories", categoryService.getAllCategory());
		  return"admin/eproduct";
	  }
	
	@PostMapping("/updateProduct")
	  public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			  HttpSession session ,org.springframework.ui.Model m) {
		
		session.removeAttribute("successMsg");
		session.removeAttribute("errorMsg");
		
		if (product.getDiscount()<0 || product.getDiscount()>100)
		{
			session.setAttribute("errorMsg", "Invalid Discount");
			
          }else {
	
		Product updateProduct = productService.updateProduct(product, image);
		if (!ObjectUtils.isEmpty(updateProduct)) {
			session.setAttribute("successMsg", "Product update Success");
			}else {
				session.setAttribute("errorMsg", "Something wrong on server");

			}
          }
		
	  		  return"redirect:/admin/editProduct/" + product.getId();
	  }
}


