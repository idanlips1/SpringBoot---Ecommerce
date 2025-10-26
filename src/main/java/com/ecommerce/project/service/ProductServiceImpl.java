package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper;

    private final String path = System.getProperty("user.dir") + File.separator + "images";

    @Autowired
    private FileService fileService;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO){
        // Check if product already exists
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));
        Product product = modelMapper.map(productDTO, Product.class);
        List<Product> checkSizeProduct = productRepository.findByProductName(product.getProductName());
        if (!checkSizeProduct.isEmpty()){
            throw new APIException("Product already exists");
        }
        product.setCategory(category);
        product.setProductImage("default.png");
        double specialPrice = product.getProductPrice() -
                (product.getDiscount() * 0.01) * product.getProductPrice();
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> products = productPage.getContent();
        if (products.isEmpty()){
            throw new APIException("No products found, Please add some products");
        }
        List<ProductDTO> productDTOS = products.stream().
                map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getNumberOfElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId,Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByProductPriceAsc(category, pageDetails);
        List<Product> productsByCategoryId = productPage.getContent();
        if (productsByCategoryId.isEmpty()){
           throw new APIException("No products found under this category");
       }
       List<ProductDTO> productDTOSByCategoryId = productsByCategoryId.stream().
               map(product -> modelMapper
                       .map(product, ProductDTO.class)).toList();
       ProductResponse productResponse = new ProductResponse();
       productResponse.setContent(productDTOSByCategoryId);
       productResponse.setPageNumber(productPage.getNumber());
       productResponse.setPageSize(productPage.getSize());
       productResponse.setTotalPages(productPage.getTotalPages());
       productResponse.setTotalElements(productPage.getNumberOfElements());
       productResponse.setLastPage(productPage.isLast());

       return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword) {
        // product size is 0
        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');
        if (products.isEmpty()){
            throw new APIException("No products found for this keyword");
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper
                        .map(product, ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        // Get the existing product from the DB
        Product productFromDb = productRepository.findById(productId).
                orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));
        // Mapper
        Product product = modelMapper.map(productDTO, Product.class);

        // Update the product info with the one in the request body
        productFromDb.setProductName(product.getProductName());
        productFromDb.setProductDescription(product.getProductDescription());
        productFromDb.setProductPrice(product.getProductPrice());
        productFromDb.setProductQuantity(product.getProductQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        // save to database
        Product savedProduct = productRepository.save(productFromDb);

        // Map to DTO
        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductDTO deleteProductById(Long productId) {
        Optional<Product> productFromDb = productRepository.findById(productId);
        if (productFromDb.isEmpty()) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        Product productToDelete = productFromDb.get();
        productRepository.deleteById(productId);

        return modelMapper.map(productToDelete, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));
        // Upload the image to the server (slash image DIR)
        // Get the file name of uploaded image

        String fileName = fileService.uploadImage(path, image);
        // Updating of the new file name to the product
        productFromDB.setProductImage(fileName);
        // Save product
        Product updatedProduct = productRepository.save(productFromDB);

        // return productDTO
        return modelMapper.map(updatedProduct,ProductDTO.class);


    }


}
