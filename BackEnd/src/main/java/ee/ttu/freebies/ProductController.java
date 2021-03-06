package ee.ttu.freebies;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import ee.ttu.freebies.model.Product;
import ee.ttu.freebies.model.ProductRepository;


@RestController
@CrossOrigin //TODO: Remove in production
public class ProductController {
   private final ProductRepository repo;

   @Autowired
   public ProductController(ProductRepository repo) {
      this.repo = repo;
   }

   @RequestMapping("/getProducts")
   public Iterable<Product> getProducts(){
      return repo.findAll();
   }

   @RequestMapping("/addProduct")
   public void addProduct(@RequestBody Product product){
      repo.save(product);
   }
   
   @RequestMapping("/contactProductOwner")
   public String requestOwner(@RequestBody Long productID){
      final Optional<Product> found = repo.findByIdAndState(productID, Product.State.Available);
      if(! found.isPresent()) {
         throw new IllegalArgumentException("Product not found or already under negotiation");
      }
      final Product product = found.get();
      found.get().setState(Product.State.UnderNegotiation);
      repo.save(product);
      return product.getEmail();
   }

   @RequestMapping(value="/{category}", method=RequestMethod.GET)
  	public List<Product> getProductsByCategory(@PathVariable("category") Product.Category category) {
		return repo.findAllByCategory(category);
  	}
   
   @RequestMapping("/republishProduct")
   public Product republishProduct(@RequestBody Product.Ticket ticket){
      final Optional<Product> found = repo.findByIdAndState(ticket.getId(), Product.State.UnderNegotiation);
      if( ! found.isPresent()){
         throw new IllegalArgumentException("Product not found or not under negotiation");
      }
      final Product product = found.get();
      if( ! product.getPassword().equals(ticket.getPassword())){
         throw new IllegalArgumentException("Invalid password for product");
      }
      product.setState(Product.State.Available);
      repo.save(product);
      return product;
   }


   @RequestMapping("/removeProduct")
   public Product removeProduct(@RequestBody Product.Ticket ticket){
      final Optional<Product> found = repo.findByIdAndState(ticket.getId(), Product.State.UnderNegotiation);
      final Optional<Product> found2 = repo.findByIdAndState(ticket.getId(), Product.State.Available);
      if( ! (found.isPresent() || found2.isPresent())){
         throw new IllegalArgumentException("Product not found");
      }
      final Product product = found.orElseGet(found2::get);
      if( ! product.getPassword().equals(ticket.getPassword())){
         throw new IllegalArgumentException("Invalid password for product");
      }
      product.setState(Product.State.Archived);
      repo.save(product);
      return product;
   }
}